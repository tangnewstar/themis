package com.themis.metadata.storage;

import com.themis.core.model.RuleDefinition;
import com.themis.core.model.RuleStatus;
import com.themis.core.model.RuleType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for rule metadata storage operations.
 * Provides abstraction over different storage backends (Database, Etcd, Redis, File System).
 */
public interface MetadataStorage {
    
    /**
     * Saves a rule definition to storage.
     * 
     * @param rule The rule definition to save
     * @throws MetadataStorageException if save operation fails
     */
    void save(RuleDefinition rule) throws MetadataStorageException;
    
    /**
     * Loads a rule definition by ID.
     * 
     * @param ruleId The rule ID to load
     * @return Optional containing the rule if found, empty otherwise
     * @throws MetadataStorageException if load operation fails
     */
    Optional<RuleDefinition> load(String ruleId) throws MetadataStorageException;
    
    /**
     * Loads all rules of a specific type.
     * 
     * @param ruleType The type of rules to load
     * @return List of rules of the specified type
     * @throws MetadataStorageException if load operation fails
     */
    List<RuleDefinition> loadByType(RuleType ruleType) throws MetadataStorageException;
    
    /**
     * Loads all rules with a specific status.
     * 
     * @param status The status of rules to load
     * @return List of rules with the specified status
     * @throws MetadataStorageException if load operation fails
     */
    List<RuleDefinition> loadByStatus(RuleStatus status) throws MetadataStorageException;
    
    /**
     * Deletes a rule definition from storage.
     * 
     * @param ruleId The rule ID to delete
     * @return true if the rule was deleted, false if it didn't exist
     * @throws MetadataStorageException if delete operation fails
     */
    boolean delete(String ruleId) throws MetadataStorageException;
    
    /**
     * Searches for rules using flexible query criteria.
     * 
     * @param query The search query criteria
     * @return List of rules matching the query
     * @throws MetadataStorageException if search operation fails
     */
    List<RuleDefinition> search(MetadataQuery query) throws MetadataStorageException;
    
    /**
     * Lists all rule IDs in storage.
     * 
     * @return List of all rule IDs
     * @throws MetadataStorageException if list operation fails
     */
    List<String> listAllRuleIds() throws MetadataStorageException;
    
    /**
     * Checks if a rule exists in storage.
     * 
     * @param ruleId The rule ID to check
     * @return true if the rule exists, false otherwise
     * @throws MetadataStorageException if check operation fails
     */
    boolean exists(String ruleId) throws MetadataStorageException;
    
    /**
     * Updates a rule definition in storage.
     * 
     * @param rule The rule definition with updated values
     * @throws MetadataStorageException if update operation fails
     */
    void update(RuleDefinition rule) throws MetadataStorageException;
    
    /**
     * Gets the total count of rules in storage.
     * 
     * @return Total number of rules
     * @throws MetadataStorageException if count operation fails
     */
    long count() throws MetadataStorageException;
    
    /**
     * Gets the count of rules by type.
     * 
     * @return Map of rule type to count
     * @throws MetadataStorageException if count operation fails
     */
    Map<RuleType, Long> countByType() throws MetadataStorageException;
    
    /**
     * Gets the count of rules by status.
     * 
     * @return Map of rule status to count
     * @throws MetadataStorageException if count operation fails
     */
    Map<RuleStatus, Long> countByStatus() throws MetadataStorageException;
    
    /**
     * Performs a health check on the storage backend.
     * 
     * @return true if storage is healthy, false otherwise
     */
    boolean healthCheck();
    
    /**
     * Gets storage-specific statistics and metrics.
     * 
     * @return Map of storage metrics
     */
    Map<String, Object> getStorageMetrics();
    
    /**
     * Initializes the storage backend with configuration.
     * 
     * @param config Storage configuration
     * @throws MetadataStorageException if initialization fails
     */
    void initialize(StorageConfiguration config) throws MetadataStorageException;
    
    /**
     * Shuts down the storage backend and releases resources.
     * 
     * @throws MetadataStorageException if shutdown fails
     */
    void shutdown() throws MetadataStorageException;
}