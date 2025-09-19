package com.themis.metadata.storage.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.themis.core.model.RuleDefinition;
import com.themis.core.model.RuleStatus;
import com.themis.core.model.RuleType;
import com.themis.metadata.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Database implementation of MetadataStorage using JDBC.
 * Provides persistent storage for rule metadata with SQL database backends.
 */
@Repository
public class DatabaseMetadataStorage implements MetadataStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMetadataStorage.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private boolean initialized = false;
    
    @Autowired
    public DatabaseMetadataStorage(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }
    
    @Override
    public void initialize(StorageConfiguration config) throws MetadataStorageException {
        try {
            createTablesIfNotExists();
            this.initialized = true;
            logger.info("Database metadata storage initialized successfully");
        } catch (Exception e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.CONFIGURATION_ERROR,
                "initialize",
                "Failed to initialize database metadata storage",
                e
            );
        }
    }
    
    @Override
    public void save(RuleDefinition rule) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "INSERT INTO rule_metadata (" +
                    "rule_id, rule_name, rule_type, description, status, version," +
                    "created_at, updated_at, created_by, updated_by, priority," +
                    "metadata_json, tags_json, rule_data_json" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            jdbcTemplate.update(sql,
                rule.getRuleId(),
                rule.getRuleName(),
                rule.getRuleType().name(),
                rule.getDescription(),
                rule.getStatus().name(),
                rule.getVersion(),
                rule.getCreatedAt(),
                rule.getUpdatedAt(),
                rule.getCreatedBy(),
                rule.getUpdatedBy(),
                rule.getPriority(),
                serializeToJson(rule.getMetadata()),
                serializeToJson(rule.getTags()),
                serializeToJson(rule)
            );
            
            logger.debug("Saved rule: {}", rule.getRuleId());
            
        } catch (DuplicateKeyException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.RESOURCE_ALREADY_EXISTS,
                "save",
                "Rule with ID " + rule.getRuleId() + " already exists"
            );
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "save",
                "Failed to save rule: " + rule.getRuleId(),
                e
            );
        }
    }
    
    @Override
    public Optional<RuleDefinition> load(String ruleId) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "SELECT * FROM rule_metadata WHERE rule_id = ?";
            
            RuleDefinition rule = jdbcTemplate.queryForObject(sql, new RuleDefinitionRowMapper(), ruleId);
            return Optional.ofNullable(rule);
            
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "load",
                "Failed to load rule: " + ruleId,
                e
            );
        }
    }
    
    @Override
    public List<RuleDefinition> loadByType(RuleType ruleType) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "SELECT * FROM rule_metadata WHERE rule_type = ? ORDER BY priority, rule_name";
            
            return jdbcTemplate.query(sql, new RuleDefinitionRowMapper(), ruleType.name());
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "loadByType",
                "Failed to load rules by type: " + ruleType,
                e
            );
        }
    }
    
    @Override
    public List<RuleDefinition> loadByStatus(RuleStatus status) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "SELECT * FROM rule_metadata WHERE status = ? ORDER BY updated_at DESC";
            
            return jdbcTemplate.query(sql, new RuleDefinitionRowMapper(), status.name());
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "loadByStatus",
                "Failed to load rules by status: " + status,
                e
            );
        }
    }
    
    @Override
    public boolean delete(String ruleId) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "DELETE FROM rule_metadata WHERE rule_id = ?";
            
            int rowsAffected = jdbcTemplate.update(sql, ruleId);
            boolean deleted = rowsAffected > 0;
            
            if (deleted) {
                logger.debug("Deleted rule: {}", ruleId);
            }
            
            return deleted;
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "delete",
                "Failed to delete rule: " + ruleId,
                e
            );
        }
    }
    
    @Override
    public List<RuleDefinition> search(MetadataQuery query) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM rule_metadata WHERE 1=1");
            List<Object> params = new ArrayList<>();
            
            // Build WHERE clause based on query criteria
            buildWhereClause(sql, params, query);
            
            // Add ORDER BY clause
            if (query.getSortCriteria() != null && !query.getSortCriteria().isEmpty()) {
                sql.append(" ORDER BY ");
                for (int i = 0; i < query.getSortCriteria().size(); i++) {
                    if (i > 0) sql.append(", ");
                    MetadataQuery.SortCriteria criteria = query.getSortCriteria().get(i);
                    sql.append(criteria.getField()).append(" ").append(criteria.getDirection());
                }
            } else {
                sql.append(" ORDER BY updated_at DESC");
            }
            
            // Add LIMIT and OFFSET
            sql.append(" LIMIT ? OFFSET ?");
            params.add(query.getLimit());
            params.add(query.getOffset());
            
            return jdbcTemplate.query(sql.toString(), new RuleDefinitionRowMapper(), params.toArray());
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "search",
                "Failed to search rules",
                e
            );
        }
    }
    
    @Override
    public List<String> listAllRuleIds() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "SELECT rule_id FROM rule_metadata ORDER BY rule_id";
            
            return jdbcTemplate.queryForList(sql, String.class);
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "listAllRuleIds",
                "Failed to list rule IDs",
                e
            );
        }
    }
    
    @Override
    public boolean exists(String ruleId) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "SELECT COUNT(*) FROM rule_metadata WHERE rule_id = ?";
            
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ruleId);
            return count != null && count > 0;
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "exists",
                "Failed to check rule existence: " + ruleId,
                e
            );
        }
    }
    
    @Override
    public void update(RuleDefinition rule) throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql =
                "UPDATE rule_metadata SET" +
                "    rule_name = ?, rule_type = ?, description = ?, status = ?, version = ?," +
                "    updated_at = ?, updated_by = ?, priority = ?," +
                "    metadata_json = ?, tags_json = ?, rule_data_json = ?" +
                "WHERE rule_id = ?";
            
            int rowsAffected = jdbcTemplate.update(sql,
                rule.getRuleName(),
                rule.getRuleType().name(),
                rule.getDescription(),
                rule.getStatus().name(),
                rule.getVersion(),
                rule.getUpdatedAt(),
                rule.getUpdatedBy(),
                rule.getPriority(),
                serializeToJson(rule.getMetadata()),
                serializeToJson(rule.getTags()),
                serializeToJson(rule),
                rule.getRuleId()
            );
            
            if (rowsAffected == 0) {
                throw new MetadataStorageException(
                    MetadataStorageException.StorageErrorCode.RESOURCE_NOT_FOUND,
                    "update",
                    "Rule not found for update: " + rule.getRuleId()
                );
            }
            
            logger.debug("Updated rule: {}", rule.getRuleId());
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "update",
                "Failed to update rule: " + rule.getRuleId(),
                e
            );
        }
    }
    
    @Override
    public long count() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "SELECT COUNT(*) FROM rule_metadata";
            
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0L;
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "count",
                "Failed to count rules",
                e
            );
        }
    }
    
    @Override
    public Map<RuleType, Long> countByType() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "SELECT rule_type, COUNT(*) as count FROM rule_metadata GROUP BY rule_type";
            
            Map<RuleType, Long> result = new HashMap<>();
            
            jdbcTemplate.query(sql, rs -> {
                String typeString = rs.getString("rule_type");
                Long count = rs.getLong("count");
                try {
                    RuleType type = RuleType.valueOf(typeString);
                    result.put(type, count);
                } catch (IllegalArgumentException e) {
                    logger.warn("Unknown rule type in database: {}", typeString);
                }
            });
            
            return result;
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "countByType",
                "Failed to count rules by type",
                e
            );
        }
    }
    
    @Override
    public Map<RuleStatus, Long> countByStatus() throws MetadataStorageException {
        if (!initialized) {
            throw new MetadataStorageException("Storage not initialized");
        }
        
        try {
            String sql = "SELECT status, COUNT(*) as count FROM rule_metadata GROUP BY status";
            
            Map<RuleStatus, Long> result = new HashMap<>();
            
            jdbcTemplate.query(sql, rs -> {
                String statusString = rs.getString("status");
                Long count = rs.getLong("count");
                try {
                    RuleStatus status = RuleStatus.valueOf(statusString);
                    result.put(status, count);
                } catch (IllegalArgumentException e) {
                    logger.warn("Unknown rule status in database: {}", statusString);
                }
            });
            
            return result;
            
        } catch (DataAccessException e) {
            throw new MetadataStorageException(
                MetadataStorageException.StorageErrorCode.UNKNOWN_ERROR,
                "countByStatus",
                "Failed to count rules by status",
                e
            );
        }
    }
    
    @Override
    public boolean healthCheck() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            logger.warn("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getStorageMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            metrics.put("type", "DATABASE");
            metrics.put("initialized", initialized);
            metrics.put("healthy", healthCheck());
            
            if (initialized) {
                metrics.put("totalRules", count());
                metrics.put("rulesByType", countByType());
                metrics.put("rulesByStatus", countByStatus());
            }
            
        } catch (Exception e) {
            logger.warn("Failed to collect storage metrics: {}", e.getMessage());
            metrics.put("error", e.getMessage());
        }
        
        return metrics;
    }
    
    @Override
    public void shutdown() throws MetadataStorageException {
        this.initialized = false;
        logger.info("Database metadata storage shutdown completed");
    }
    
    private void createTablesIfNotExists() {
        String createTableSql =
            "CREATE TABLE IF NOT EXISTS rule_metadata (" +
            "    rule_id VARCHAR(255) PRIMARY KEY," +
            "    rule_name VARCHAR(500) NOT NULL," +
            "    rule_type VARCHAR(50) NOT NULL," +
            "    description TEXT," +
            "    status VARCHAR(50) NOT NULL," +
            "    version VARCHAR(50) NOT NULL," +
            "    created_at TIMESTAMP," +
            "    updated_at TIMESTAMP," +
            "    created_by VARCHAR(255)," +
            "    updated_by VARCHAR(255)," +
            "    priority INTEGER DEFAULT 100," +
            "    metadata_json TEXT," +
            "    tags_json TEXT," +
            "    rule_data_json TEXT NOT NULL," +
            "    INDEX idx_rule_type (rule_type)," +
            "    INDEX idx_status (status)," +
            "    INDEX idx_updated_at (updated_at)," +
            "    INDEX idx_priority (priority)" +
            ")";
        
        jdbcTemplate.execute(createTableSql);
        logger.debug("Rule metadata table created or verified");
    }
    
    private void buildWhereClause(StringBuilder sql, List<Object> params, MetadataQuery query) {
        if (query.getRuleIds() != null && !query.getRuleIds().isEmpty()) {
            sql.append(" AND rule_id IN (");
            for (int i = 0; i < query.getRuleIds().size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
                params.add(query.getRuleIds().get(i));
            }
            sql.append(")");
        }
        
        if (query.getRuleTypes() != null && !query.getRuleTypes().isEmpty()) {
            sql.append(" AND rule_type IN (");
            for (int i = 0; i < query.getRuleTypes().size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
                params.add(query.getRuleTypes().get(i).name());
            }
            sql.append(")");
        }
        
        if (query.getRuleStatuses() != null && !query.getRuleStatuses().isEmpty()) {
            sql.append(" AND status IN (");
            for (int i = 0; i < query.getRuleStatuses().size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
                params.add(query.getRuleStatuses().get(i).name());
            }
            sql.append(")");
        }
        
        if (query.getRuleNamePattern() != null) {
            sql.append(" AND rule_name LIKE ?");
            params.add("%" + query.getRuleNamePattern() + "%");
        }
        
        if (query.getDescriptionPattern() != null) {
            sql.append(" AND description LIKE ?");
            params.add("%" + query.getDescriptionPattern() + "%");
        }
        
        if (query.getCreatedAfter() != null) {
            sql.append(" AND created_at >= ?");
            params.add(query.getCreatedAfter());
        }
        
        if (query.getCreatedBefore() != null) {
            sql.append(" AND created_at <= ?");
            params.add(query.getCreatedBefore());
        }
        
        if (query.getUpdatedAfter() != null) {
            sql.append(" AND updated_at >= ?");
            params.add(query.getUpdatedAfter());
        }
        
        if (query.getUpdatedBefore() != null) {
            sql.append(" AND updated_at <= ?");
            params.add(query.getUpdatedBefore());
        }
        
        if (query.getCreatedBy() != null) {
            sql.append(" AND created_by = ?");
            params.add(query.getCreatedBy());
        }
        
        if (query.getUpdatedBy() != null) {
            sql.append(" AND updated_by = ?");
            params.add(query.getUpdatedBy());
        }
        
        if (query.getPriorityMin() != null) {
            sql.append(" AND priority >= ?");
            params.add(query.getPriorityMin());
        }
        
        if (query.getPriorityMax() != null) {
            sql.append(" AND priority <= ?");
            params.add(query.getPriorityMax());
        }
        
        if (query.getVersion() != null) {
            sql.append(" AND version = ?");
            params.add(query.getVersion());
        }
    }
    
    private String serializeToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }
    
    private <T> T deserializeFromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeToMap(String json) {
        return deserializeFromJson(json, Map.class);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, String> deserializeToStringMap(String json) {
        return deserializeFromJson(json, Map.class);
    }
    
    /**
     * Row mapper for converting database rows to RuleDefinition objects.
     */
    private class RuleDefinitionRowMapper implements RowMapper<RuleDefinition> {
        
        @Override
        public RuleDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                String ruleDataJson = rs.getString("rule_data_json");
                RuleDefinition rule = deserializeFromJson(ruleDataJson, RuleDefinition.class);
                
                if (rule == null) {
                    // Fallback: create basic rule definition from metadata
                    logger.warn("Failed to deserialize rule data, creating from metadata for rule: {}", 
                              rs.getString("rule_id"));
                    rule = createRuleFromMetadata(rs);
                }
                
                return rule;
                
            } catch (Exception e) {
                logger.error("Error mapping rule from database row: {}", e.getMessage(), e);
                throw new SQLException("Failed to map rule definition", e);
            }
        }
        
        private RuleDefinition createRuleFromMetadata(ResultSet rs) throws SQLException {
            // This is a simplified fallback - in practice, you'd need to create specific rule types
            // For now, returning null to indicate the record couldn't be processed
            logger.error("Cannot create rule from metadata without proper deserialization");
            return null;
        }
    }
}