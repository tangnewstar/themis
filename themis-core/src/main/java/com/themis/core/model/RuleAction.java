package com.themis.core.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an action to be executed when a rule condition is met.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "actionType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SetFieldAction.class, name = "SET_FIELD"),
    @JsonSubTypes.Type(value = CalculateAction.class, name = "CALCULATE"),
    @JsonSubTypes.Type(value = CallServiceAction.class, name = "CALL_SERVICE"),
    @JsonSubTypes.Type(value = LogAction.class, name = "LOG")
})
public abstract class RuleAction {
    
    @NotNull(message = "Action type cannot be null")
    private ActionType actionType;
    
    private String description;
    
    private Integer priority = 100;
    
    private Map<String, Object> parameters;
    
    private boolean continueOnError = true;
    
    // Default constructor
    protected RuleAction() {}
    
    protected RuleAction(ActionType actionType) {
        this.actionType = actionType;
    }
    
    // Getters and Setters
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    
    public boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(boolean continueOnError) { this.continueOnError = continueOnError; }
    
    /**
     * Validates this action for correctness.
     */
    public abstract ValidationResult validate();
    
    /**
     * Creates a copy of this action.
     */
    public abstract RuleAction copy();
    
    /**
     * Gets a human-readable representation of this action.
     */
    public abstract String toHumanReadable();
    
    /**
     * Enumeration of action types.
     */
    public enum ActionType {
        SET_FIELD("Set Field", "Set a field value"),
        CALCULATE("Calculate", "Perform a calculation"),
        CALL_SERVICE("Call Service", "Call an external service"),
        LOG("Log", "Log a message"),
        SEND_EMAIL("Send Email", "Send an email notification"),
        SEND_SMS("Send SMS", "Send an SMS notification"),
        CREATE_TASK("Create Task", "Create a task or workflow"),
        UPDATE_DATABASE("Update Database", "Update database records"),
        CALL_API("Call API", "Call an external API"),
        EXECUTE_SCRIPT("Execute Script", "Execute a custom script");
        
        private final String displayName;
        private final String description;
        
        ActionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}

/**
 * Action to set a field value in the context.
 */
class SetFieldAction extends RuleAction {
    
    @NotBlank(message = "Field name cannot be blank")
    private String fieldName;
    
    private Object value;
    
    private String expression;
    
    // Default constructor
    public SetFieldAction() {
        super(ActionType.SET_FIELD);
    }
    
    public SetFieldAction(String fieldName, Object value) {
        super(ActionType.SET_FIELD);
        this.fieldName = fieldName;
        this.value = value;
    }
    
    public SetFieldAction(String fieldName, String expression) {
        super(ActionType.SET_FIELD);
        this.fieldName = fieldName;
        this.expression = expression;
    }
    
    // Getters and Setters
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
    
    @Override
    public ValidationResult validate() {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        
        if (fieldName == null || fieldName.trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_FIELD_NAME", 
                "Field name is required", "fieldName"));
        }
        
        if (value == null && (expression == null || expression.trim().isEmpty())) {
            errors.add(new ValidationResult.ValidationError("MISSING_VALUE_OR_EXPRESSION", 
                "Either value or expression must be provided", "value"));
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    @Override
    public RuleAction copy() {
        SetFieldAction copy = new SetFieldAction(fieldName, value);
        copy.setExpression(expression);
        copy.setDescription(getDescription());
        copy.setPriority(getPriority());
        copy.setContinueOnError(isContinueOnError());
        
        if (getParameters() != null) {
            copy.setParameters(new java.util.HashMap<>(getParameters()));
        }
        
        return copy;
    }
    
    @Override
    public String toHumanReadable() {
        StringBuilder sb = new StringBuilder();
        sb.append("Set ").append(fieldName).append(" = ");
        
        if (expression != null && !expression.trim().isEmpty()) {
            sb.append(expression);
        } else {
            sb.append(value);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("SetFieldAction{fieldName='%s', value=%s, expression='%s'}", 
                           fieldName, value, expression);
    }
}

/**
 * Action to perform a calculation and store the result.
 */
class CalculateAction extends RuleAction {
    
    @NotBlank(message = "Result field name cannot be blank")
    private String resultField;
    
    @NotBlank(message = "Formula cannot be blank")
    private String formula;
    
    private Map<String, Object> variables;
    
    // Default constructor
    public CalculateAction() {
        super(ActionType.CALCULATE);
    }
    
    public CalculateAction(String resultField, String formula) {
        super(ActionType.CALCULATE);
        this.resultField = resultField;
        this.formula = formula;
    }
    
    // Getters and Setters
    public String getResultField() { return resultField; }
    public void setResultField(String resultField) { this.resultField = resultField; }
    
    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }
    
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    
    @Override
    public ValidationResult validate() {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        
        if (resultField == null || resultField.trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_RESULT_FIELD", 
                "Result field name is required", "resultField"));
        }
        
        if (formula == null || formula.trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_FORMULA", 
                "Formula is required", "formula"));
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    @Override
    public RuleAction copy() {
        CalculateAction copy = new CalculateAction(resultField, formula);
        copy.setDescription(getDescription());
        copy.setPriority(getPriority());
        copy.setContinueOnError(isContinueOnError());
        
        if (variables != null) {
            copy.setVariables(new java.util.HashMap<>(variables));
        }
        
        if (getParameters() != null) {
            copy.setParameters(new java.util.HashMap<>(getParameters()));
        }
        
        return copy;
    }
    
    @Override
    public String toHumanReadable() {
        return String.format("Calculate %s = %s", resultField, formula);
    }
    
    @Override
    public String toString() {
        return String.format("CalculateAction{resultField='%s', formula='%s'}", resultField, formula);
    }
}

/**
 * Action to call an external service.
 */
class CallServiceAction extends RuleAction {
    
    @NotBlank(message = "Service name cannot be blank")
    private String serviceName;
    
    @NotBlank(message = "Method name cannot be blank")
    private String methodName;
    
    private Map<String, Object> inputParameters;
    
    private String resultField;
    
    // Default constructor
    public CallServiceAction() {
        super(ActionType.CALL_SERVICE);
    }
    
    public CallServiceAction(String serviceName, String methodName) {
        super(ActionType.CALL_SERVICE);
        this.serviceName = serviceName;
        this.methodName = methodName;
    }
    
    // Getters and Setters
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    
    public Map<String, Object> getInputParameters() { return inputParameters; }
    public void setInputParameters(Map<String, Object> inputParameters) { this.inputParameters = inputParameters; }
    
    public String getResultField() { return resultField; }
    public void setResultField(String resultField) { this.resultField = resultField; }
    
    @Override
    public ValidationResult validate() {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        
        if (serviceName == null || serviceName.trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_SERVICE_NAME", 
                "Service name is required", "serviceName"));
        }
        
        if (methodName == null || methodName.trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_METHOD_NAME", 
                "Method name is required", "methodName"));
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    @Override
    public RuleAction copy() {
        CallServiceAction copy = new CallServiceAction(serviceName, methodName);
        copy.setResultField(resultField);
        copy.setDescription(getDescription());
        copy.setPriority(getPriority());
        copy.setContinueOnError(isContinueOnError());
        
        if (inputParameters != null) {
            copy.setInputParameters(new java.util.HashMap<>(inputParameters));
        }
        
        if (getParameters() != null) {
            copy.setParameters(new java.util.HashMap<>(getParameters()));
        }
        
        return copy;
    }
    
    @Override
    public String toHumanReadable() {
        StringBuilder sb = new StringBuilder();
        sb.append("Call ").append(serviceName).append(".").append(methodName).append("()");
        
        if (resultField != null && !resultField.trim().isEmpty()) {
            sb.append(" -> ").append(resultField);
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("CallServiceAction{serviceName='%s', methodName='%s'}", serviceName, methodName);
    }
}

/**
 * Action to log a message.
 */
class LogAction extends RuleAction {
    
    @NotBlank(message = "Message cannot be blank")
    private String message;
    
    private LogLevel level = LogLevel.INFO;
    
    // Default constructor
    public LogAction() {
        super(ActionType.LOG);
    }
    
    public LogAction(String message) {
        super(ActionType.LOG);
        this.message = message;
    }
    
    public LogAction(String message, LogLevel level) {
        super(ActionType.LOG);
        this.message = message;
        this.level = level;
    }
    
    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LogLevel getLevel() { return level; }
    public void setLevel(LogLevel level) { this.level = level; }
    
    @Override
    public ValidationResult validate() {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        
        if (message == null || message.trim().isEmpty()) {
            errors.add(new ValidationResult.ValidationError("MISSING_MESSAGE", 
                "Message is required", "message"));
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    @Override
    public RuleAction copy() {
        LogAction copy = new LogAction(message, level);
        copy.setDescription(getDescription());
        copy.setPriority(getPriority());
        copy.setContinueOnError(isContinueOnError());
        
        if (getParameters() != null) {
            copy.setParameters(new java.util.HashMap<>(getParameters()));
        }
        
        return copy;
    }
    
    @Override
    public String toHumanReadable() {
        return String.format("Log [%s]: %s", level, message);
    }
    
    @Override
    public String toString() {
        return String.format("LogAction{message='%s', level=%s}", message, level);
    }
    
    /**
     * Log levels for log actions.
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}