package com.themis.metadata.storage.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.themis.core.model.RuleDefinition;
import com.themis.core.model.RuleStatus;
import com.themis.core.model.RuleType;
import com.themis.metadata.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis implementation of MetadataStorage.
 * Provides fast caching and distributed storage for rule metadata.
 */
public class RedisMetadataStorage implements MetadataStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisMetadataStorage.class);
    
    private static final String RULE_KEY_PREFIX = "themis:rule:";
    private static final String RULE_INDEX_KEY = "themis:rules:index";
    private static final String RULE_TYPE_PREFIX = "themis:rules:type:";
    private static final String RULE_STATUS_PREFIX = "themis:rules:status:";
    
    private JedisPool jedisPool;
    private ObjectMapper objectMapper;
    private boolean initialized = false;
    private long defaultExpirationSeconds = 3600; // 1 hour
    
    public RedisMetadataStorage() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }
    
    @Override
    public void initialize(StorageConfiguration config) throws MetadataStorageException {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(config.getMaxConnections());
            poolConfig.setMaxIdle(config.getMaxConnections() / 2);
            poolConfig.setMinIdle(1);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            
            if (config.getConnectionUrl() != null) {
                URI redisUri = URI.create(config.getConnectionUrl());
                this.jedisPool = new JedisPool(poolConfig, redisUri, (int) config.getConnectionTimeoutMillis());
            } else {
                String host = config.getProperty("redis.host", "localhost");
                int port = Integer.parseInt(config.getProperty("redis.port", "6379"));
                String password = config.getProperty("redis.password");
                
                if (password != null && !password.trim().isEmpty()) {
                    this.jedisPool = new JedisPool(poolConfig, host, port, (int) config.getConnectionTimeoutMillis(), password);
                } else {
                    this.jedisPool = new JedisPool(poolConfig, host, port, (int) config.getConnectionTimeoutMillis());
                }
            }
            
            // Set expiration time if configured
            if (config.getCacheExpirationMillis() > 0) {
                this.defaultExpirationSeconds = config.getCacheExpirationMillis() / 1000;
            }
            
            this.initialized = true;
            logger.info("Redis metadata storage initialized successfully");
            
        } catch (Exception e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.CONFIGURATION_ERROR,
                "initialize",
                "Failed to initialize Redis metadata storage",
                e
            );
        }
    }
    
    @Override
    public void save(RuleDefinition rule) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String ruleKey = RULE_KEY_PREFIX + rule.getRuleId();
            String ruleJson = serializeToJson(rule);
            
            // Check if rule already exists
            if (jedis.exists(ruleKey)) {
                throw new MetadataStorageException(
                    MetadataStorageException.StorageErrorCode.RESOURCE_ALREADY_EXISTS,
                    "save",
                    "Rule with ID " + rule.getRuleId() + " already exists"
                );
            }
            
            // Save rule data
            jedis.setex(ruleKey, defaultExpirationSeconds, ruleJson);
            
            // Update indexes
            updateIndexes(jedis, rule, true);
            
            logger.debug("Saved rule to Redis: {}", rule.getRuleId());
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "save",
                "Failed to save rule to Redis: " + rule.getRuleId(),
                e
            );
        }
    }
    
    @Override
    public Optional<RuleDefinition> load(String ruleId) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String ruleKey = RULE_KEY_PREFIX + ruleId;
            String ruleJson = jedis.get(ruleKey);
            
            if (ruleJson == null) {
                return Optional.empty();
            }
            
            RuleDefinition rule = deserializeFromJson(ruleJson, RuleDefinition.class);
            return Optional.ofNullable(rule);
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "load",
                "Failed to load rule from Redis: " + ruleId,
                e
            );
        }
    }
    
    @Override
    public List<RuleDefinition> loadByType(RuleType ruleType) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String typeKey = RULE_TYPE_PREFIX + ruleType.name();
            Set<String> ruleIds = jedis.smembers(typeKey);
            
            return loadRulesByIds(jedis, ruleIds);
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "loadByType",
                "Failed to load rules by type from Redis: " + ruleType,
                e
            );
        }
    }
    
    @Override
    public List<RuleDefinition> loadByStatus(RuleStatus status) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String statusKey = RULE_STATUS_PREFIX + status.name();
            Set<String> ruleIds = jedis.smembers(statusKey);
            
            return loadRulesByIds(jedis, ruleIds);
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "loadByStatus",
                "Failed to load rules by status from Redis: " + status,
                e
            );
        }
    }
    
    @Override
    public boolean delete(String ruleId) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String ruleKey = RULE_KEY_PREFIX + ruleId;
            
            // Load rule before deletion to update indexes
            String ruleJson = jedis.get(ruleKey);
            if (ruleJson == null) {
                return false;
            }
            
            RuleDefinition rule = deserializeFromJson(ruleJson, RuleDefinition.class);
            
            // Delete rule data
            long deleted = jedis.del(ruleKey);
            
            if (deleted > 0 && rule != null) {
                // Update indexes
                updateIndexes(jedis, rule, false);
                logger.debug("Deleted rule from Redis: {}", ruleId);
                return true;
            }
            
            return false;
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "delete",
                "Failed to delete rule from Redis: " + ruleId,
                e
            );
        }
    }
    
    @Override
    public List<RuleDefinition> search(MetadataQuery query) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> candidateIds = new HashSet<>();
            
            // Get candidate rule IDs based on query criteria
            if (query.getRuleIds() != null && !query.getRuleIds().isEmpty()) {
                candidateIds.addAll(query.getRuleIds());
            } else {
                // Start with all rules
                candidateIds.addAll(jedis.smembers(RULE_INDEX_KEY));
            }
            
            // Filter by type
            if (query.getRuleTypes() != null && !query.getRuleTypes().isEmpty()) {
                Set<String> typeFilteredIds = new HashSet<>();
                for (RuleType type : query.getRuleTypes()) {
                    String typeKey = RULE_TYPE_PREFIX + type.name();
                    typeFilteredIds.addAll(jedis.smembers(typeKey));
                }
                candidateIds.retainAll(typeFilteredIds);
            }
            
            // Filter by status
            if (query.getRuleStatuses() != null && !query.getRuleStatuses().isEmpty()) {
                Set<String> statusFilteredIds = new HashSet<>();
                for (RuleStatus status : query.getRuleStatuses()) {
                    String statusKey = RULE_STATUS_PREFIX + status.name();
                    statusFilteredIds.addAll(jedis.smembers(statusKey));
                }
                candidateIds.retainAll(statusFilteredIds);
            }
            
            // Load and filter rules
            List<RuleDefinition> rules = loadRulesByIds(jedis, candidateIds);
            
            // Apply additional filtering (name pattern, dates, etc.)
            rules = rules.stream()
                .filter(rule -> matchesQuery(rule, query))
                .collect(Collectors.toList());
            
            // Apply sorting
            if (query.getSortCriteria() != null && !query.getSortCriteria().isEmpty()) {
                rules.sort(createComparator(query.getSortCriteria()));
            } else {
                rules.sort(Comparator.comparing(RuleDefinition::getUpdatedAt, Comparator.reverseOrder()));
            }
            
            // Apply pagination
            int start = query.getOffset();
            int end = Math.min(start + query.getLimit(), rules.size());
            
            if (start >= rules.size()) {
                return new ArrayList<>();
            }
            
            return rules.subList(start, end);
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "search",
                "Failed to search rules in Redis",
                e
            );
        }
    }
    
    @Override
    public List<String> listAllRuleIds() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return new ArrayList<>(jedis.smembers(RULE_INDEX_KEY));
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "listAllRuleIds",
                "Failed to list rule IDs from Redis",
                e
            );
        }
    }
    
    @Override
    public boolean exists(String ruleId) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String ruleKey = RULE_KEY_PREFIX + ruleId;
            return jedis.exists(ruleKey);
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "exists",
                "Failed to check rule existence in Redis: " + ruleId,
                e
            );
        }
    }
    
    @Override
    public void update(RuleDefinition rule) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            String ruleKey = RULE_KEY_PREFIX + rule.getRuleId();
            
            // Check if rule exists
            if (!jedis.exists(ruleKey)) {
                throw new MetadataStorageException(
                    MetadataStorageException.StorageErrorCode.RESOURCE_NOT_FOUND,
                    "update",
                    "Rule not found for update: " + rule.getRuleId()
                );
            }
            
            // Load old rule for index updates
            String oldRuleJson = jedis.get(ruleKey);
            RuleDefinition oldRule = null;
            if (oldRuleJson != null) {
                oldRule = deserializeFromJson(oldRuleJson, RuleDefinition.class);
            }
            
            // Update rule data
            String ruleJson = serializeToJson(rule);
            jedis.setex(ruleKey, defaultExpirationSeconds, ruleJson);
            
            // Update indexes
            if (oldRule != null) {
                updateIndexes(jedis, oldRule, false); // Remove old indexes
            }
            updateIndexes(jedis, rule, true); // Add new indexes
            
            logger.debug("Updated rule in Redis: {}", rule.getRuleId());
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "update",
                "Failed to update rule in Redis: " + rule.getRuleId(),
                e
            );
        }
    }
    
    @Override
    public long count() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scard(RULE_INDEX_KEY);
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "count",
                "Failed to count rules in Redis",
                e
            );
        }
    }
    
    @Override
    public Map<RuleType, Long> countByType() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            Map<RuleType, Long> result = new HashMap<>();
            
            for (RuleType type : RuleType.values()) {
                String typeKey = RULE_TYPE_PREFIX + type.name();
                long count = jedis.scard(typeKey);
                result.put(type, count);
            }
            
            return result;
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "countByType",
                "Failed to count rules by type in Redis",
                e
            );
        }
    }
    
    @Override
    public Map<RuleStatus, Long> countByStatus() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            Map<RuleStatus, Long> result = new HashMap<>();
            
            for (RuleStatus status : RuleStatus.values()) {
                String statusKey = RULE_STATUS_PREFIX + status.name();
                long count = jedis.scard(statusKey);
                result.put(status, count);
            }
            
            return result;
            
        } catch (JedisException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "countByStatus",
                "Failed to count rules by status in Redis",
                e
            );
        }
    }
    
    @Override
    public boolean healthCheck() {
        if (!initialized || jedisPool == null) {
            return false;
        }
        
        try (Jedis jedis = jedisPool.getResource()) {
            return "PONG".equals(jedis.ping());
        } catch (Exception e) {
            logger.warn("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getStorageMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            metrics.put("type", "REDIS");
            metrics.put("initialized", initialized);
            metrics.put("healthy", healthCheck());
            
            if (initialized) {
                try (Jedis jedis = jedisPool.getResource()) {
                    metrics.put("totalRules", jedis.scard(RULE_INDEX_KEY));
                    metrics.put("rulesByType", countByType());
                    metrics.put("rulesByStatus", countByStatus());
                    
                    // Redis-specific metrics
                    String info = jedis.info("memory");
                    if (info != null) {
                        metrics.put("redisMemoryInfo", parseRedisInfo(info));
                    }
                }
            }
            
        } catch (Exception e) {
            logger.warn("Failed to collect Redis storage metrics: {}", e.getMessage());
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }
    
    @Override
    public void shutdown() throws MetadataStorageException {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
        this.initialized = false;
        logger.info("Redis metadata storage shutdown completed");
    }
    
    private void updateIndexes(Jedis jedis, RuleDefinition rule, boolean add) {
        String ruleId = rule.getRuleId();
        
        if (add) {
            // Add to main index
            jedis.sadd(RULE_INDEX_KEY, ruleId);
            
            // Add to type index
            String typeKey = RULE_TYPE_PREFIX + rule.getRuleType().name();
            jedis.sadd(typeKey, ruleId);
            
            // Add to status index
            String statusKey = RULE_STATUS_PREFIX + rule.getStatus().name();
            jedis.sadd(statusKey, ruleId);
            
        } else {
            // Remove from main index
            jedis.srem(RULE_INDEX_KEY, ruleId);
            
            // Remove from type index
            String typeKey = RULE_TYPE_PREFIX + rule.getRuleType().name();
            jedis.srem(typeKey, ruleId);
            
            // Remove from status index
            String statusKey = RULE_STATUS_PREFIX + rule.getStatus().name();
            jedis.srem(statusKey, ruleId);
        }
    }
    
    private List<RuleDefinition> loadRulesByIds(Jedis jedis, Set<String> ruleIds) {
        List<RuleDefinition> rules = new ArrayList<>();
        
        for (String ruleId : ruleIds) {
            String ruleKey = RULE_KEY_PREFIX + ruleId;
            String ruleJson = jedis.get(ruleKey);
            
            if (ruleJson != null) {
                RuleDefinition rule = deserializeFromJson(ruleJson, RuleDefinition.class);
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }
        
        return rules;
    }
    
    private boolean matchesQuery(RuleDefinition rule, MetadataQuery query) {
        // Apply additional filtering that can't be done at the index level
        
        if (query.getRuleNamePattern() != null) {
            if (rule.getRuleName() == null || 
                !rule.getRuleName().toLowerCase().contains(query.getRuleNamePattern().toLowerCase())) {
                return false;
            }
        }
        
        if (query.getDescriptionPattern() != null) {
            if (rule.getDescription() == null || 
                !rule.getDescription().toLowerCase().contains(query.getDescriptionPattern().toLowerCase())) {
                return false;
            }
        }
        
        if (query.getCreatedAfter() != null) {
            if (rule.getCreatedAt() == null || rule.getCreatedAt().isBefore(query.getCreatedAfter())) {
                return false;
            }
        }
        
        if (query.getCreatedBefore() != null) {
            if (rule.getCreatedAt() == null || rule.getCreatedAt().isAfter(query.getCreatedBefore())) {
                return false;
            }
        }
        
        if (query.getUpdatedAfter() != null) {
            if (rule.getUpdatedAt() == null || rule.getUpdatedAt().isBefore(query.getUpdatedAfter())) {
                return false;
            }
        }
        
        if (query.getUpdatedBefore() != null) {
            if (rule.getUpdatedAt() == null || rule.getUpdatedAt().isAfter(query.getUpdatedBefore())) {
                return false;
            }
        }
        
        if (query.getCreatedBy() != null) {
            if (!query.getCreatedBy().equals(rule.getCreatedBy())) {
                return false;
            }
        }
        
        if (query.getUpdatedBy() != null) {
            if (!query.getUpdatedBy().equals(rule.getUpdatedBy())) {
                return false;
            }
        }
        
        if (query.getPriorityMin() != null) {
            if (rule.getPriority() == null || rule.getPriority() < query.getPriorityMin()) {
                return false;
            }
        }
        
        if (query.getPriorityMax() != null) {
            if (rule.getPriority() == null || rule.getPriority() > query.getPriorityMax()) {
                return false;
            }
        }
        
        if (query.getVersion() != null) {
            if (!query.getVersion().equals(rule.getVersion())) {
                return false;
            }
        }
        
        return true;
    }
    
    private Comparator<RuleDefinition> createComparator(List<MetadataQuery.SortCriteria> sortCriteria) {
        Comparator<RuleDefinition> comparator = null;
        
        for (MetadataQuery.SortCriteria criteria : sortCriteria) {
            Comparator<RuleDefinition> fieldComparator = createFieldComparator(criteria.getField());
            
            if (criteria.getDirection() == MetadataQuery.SortDirection.DESC) {
                fieldComparator = fieldComparator.reversed();
            }
            
            if (comparator == null) {
                comparator = fieldComparator;
            } else {
                comparator = comparator.thenComparing(fieldComparator);
            }
        }
        
        return comparator != null ? comparator : Comparator.comparing(RuleDefinition::getRuleId);
    }
    
    private Comparator<RuleDefinition> createFieldComparator(String field) {
        switch (field.toLowerCase()) {
            case "ruleid":
            case "rule_id":
                return Comparator.comparing(RuleDefinition::getRuleId, Comparator.nullsLast(Comparator.naturalOrder()));
            case "rulename":
            case "rule_name":
                return Comparator.comparing(RuleDefinition::getRuleName, Comparator.nullsLast(Comparator.naturalOrder()));
            case "ruletype":
            case "rule_type":
                return Comparator.comparing(rule -> rule.getRuleType().name(), Comparator.nullsLast(Comparator.naturalOrder()));
            case "status":
                return Comparator.comparing(rule -> rule.getStatus().name(), Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdat":
            case "created_at":
                return Comparator.comparing(RuleDefinition::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "updatedat":
            case "updated_at":
                return Comparator.comparing(RuleDefinition::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "priority":
                return Comparator.comparing(RuleDefinition::getPriority, Comparator.nullsLast(Comparator.naturalOrder()));
            case "version":
                return Comparator.comparing(RuleDefinition::getVersion, Comparator.nullsLast(Comparator.naturalOrder()));
            default:
                return Comparator.comparing(RuleDefinition::getRuleId);
        }
    }
    
    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object to JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Serialization failed", e);
        }
    }
    
    private <T> T deserializeFromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
    
    private Map<String, String> parseRedisInfo(String info) {
        Map<String, String> result = new HashMap<>();
        
        for (String line : info.split("\r\n")) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                result.put(parts[0], parts[1]);
            }
        }
        
        return result;
    }
}