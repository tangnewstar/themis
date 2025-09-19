package com.themis.core.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rule table implementation for decision tables with multiple dimensions.
 * Example: Tax rate calculation based on income and region
 */
@JsonTypeName("TABLE")
public class RuleTable extends RuleDefinition {
    
    @NotNull(message = "Table structure cannot be null")
    private TableStructure tableStructure;
    
    @NotNull(message = "Table data cannot be null")
    private List<Map<String, Object>> tableData = new ArrayList<>();
    
    private String defaultValue;
    
    // Default constructor for Jackson
    public RuleTable() {
        super();
        setRuleType(RuleType.TABLE);
    }
    
    public RuleTable(String ruleId, String ruleName) {
        super(ruleId, ruleName, RuleType.TABLE);
    }
    
    // Getters and Setters
    public TableStructure getTableStructure() { return tableStructure; }
    public void setTableStructure(TableStructure tableStructure) { this.tableStructure = tableStructure; }
    
    public List<Map<String, Object>> getTableData() { return tableData; }
    public void setTableData(List<Map<String, Object>> tableData) { 
        this.tableData = tableData != null ? tableData : new ArrayList<>(); 
    }
    
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    
    @Override
    public ValidationResult validate() {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        
        // Basic validation - placeholder implementation
        if (getRuleId() == null || getRuleId().trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_RULE_ID", "Rule ID is required", "ruleId"));
        }
        
        if (tableStructure == null) {
            errors.add(new ValidationResult.ValidationError("MISSING_TABLE_STRUCTURE", 
                "Table structure is required", "tableStructure"));
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    @Override
    public RuleDefinition copy() {
        RuleTable copy = new RuleTable(getRuleId(), getRuleName());
        
        // Copy basic properties
        copy.setDescription(getDescription());
        copy.setStatus(getStatus());
        copy.setVersion(getVersion());
        copy.setCreatedBy(getCreatedBy());
        copy.setUpdatedBy(getUpdatedBy());
        copy.setPriority(getPriority());
        
        if (getMetadata() != null) {
            copy.setMetadata(new java.util.HashMap<>(getMetadata()));
        }
        
        if (getTags() != null) {
            copy.setTags(new java.util.HashMap<>(getTags()));
        }
        
        // Copy table-specific data
        if (tableStructure != null) {
            copy.setTableStructure(tableStructure.copy());
        }
        
        if (tableData != null) {
            List<Map<String, Object>> copiedData = new ArrayList<>();
            for (Map<String, Object> row : tableData) {
                if (row != null) {
                    copiedData.add(new java.util.HashMap<>(row));
                }
            }
            copy.setTableData(copiedData);
        }
        
        copy.setDefaultValue(defaultValue);
        
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("RuleTable{ruleId='%s', rows=%d}", getRuleId(), tableData.size());
    }
    
    /**
     * Represents the structure of a decision table.
     */
    public static class TableStructure {
        private List<String> inputColumns;
        private List<String> outputColumns;
        private Map<String, String> columnTypes;
        
        // Getters and Setters
        public List<String> getInputColumns() { return inputColumns; }
        public void setInputColumns(List<String> inputColumns) { this.inputColumns = inputColumns; }
        
        public List<String> getOutputColumns() { return outputColumns; }
        public void setOutputColumns(List<String> outputColumns) { this.outputColumns = outputColumns; }
        
        public Map<String, String> getColumnTypes() { return columnTypes; }
        public void setColumnTypes(Map<String, String> columnTypes) { this.columnTypes = columnTypes; }
        
        public TableStructure copy() {
            TableStructure copy = new TableStructure();
            
            if (inputColumns != null) {
                copy.setInputColumns(new ArrayList<>(inputColumns));
            }
            
            if (outputColumns != null) {
                copy.setOutputColumns(new ArrayList<>(outputColumns));
            }
            
            if (columnTypes != null) {
                copy.setColumnTypes(new java.util.HashMap<>(columnTypes));
            }
            
            return copy;
        }
    }
}