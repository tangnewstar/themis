package com.themis.core.protocol.impl;

import com.themis.core.model.*;
import com.themis.core.protocol.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DSL格式的规则定义协议实现
 * 支持类似自然语言的DSL语法
 */
public class DslRuleDefinitionProtocol implements RuleDefinitionProtocol {
    
    private static final Pattern RULE_PATTERN = Pattern.compile(
        "RULE\\s+\"([^\"]+)\"\\s*\\{([^}]+)\\}", Pattern.DOTALL);
    private static final Pattern WHEN_PATTERN = Pattern.compile(
        "WHEN\\s+(.+?)\\s+THEN", Pattern.DOTALL);
    private static final Pattern THEN_PATTERN = Pattern.compile(
        "THEN\\s+(.+?)(?:$|\\})", Pattern.DOTALL);
    
    @Override
    public RuleDefinition parse(String source) throws RuleParseException {
        if (source == null || source.trim().isEmpty()) {
            throw new RuleParseException("Source cannot be null or empty");
        }
        
        try {
            Matcher ruleMatcher = RULE_PATTERN.matcher(source);
            if (!ruleMatcher.find()) {
                throw new RuleParseException("Invalid DSL format: missing RULE declaration");
            }
            
            String ruleName = ruleMatcher.group(1);
            String ruleBody = ruleMatcher.group(2);
            
            // 解析WHEN部分
            Matcher whenMatcher = WHEN_PATTERN.matcher(ruleBody);
            if (!whenMatcher.find()) {
                throw new RuleParseException("Invalid DSL format: missing WHEN clause");
            }
            String whenClause = whenMatcher.group(1).trim();
            
            // 解析THEN部分
            Matcher thenMatcher = THEN_PATTERN.matcher(ruleBody);
            if (!thenMatcher.find()) {
                throw new RuleParseException("Invalid DSL format: missing THEN clause");
            }
            String thenClause = thenMatcher.group(1).trim();
            
            // 创建表达式规则
            ExpressionRule rule = new ExpressionRule();
            rule.setRuleId(generateRuleId(ruleName));
            rule.setRuleName(ruleName);
            rule.setRuleType(RuleType.EXPRESSION);
            
            // 解析条件
            RuleCondition condition = parseCondition(whenClause);
            rule.setCondition(condition);
            
            // 解析动作
            RuleAction action = parseAction(thenClause);
            rule.setAction(action);
            
            return rule;
            
        } catch (Exception e) {
            throw new RuleParseException("Failed to parse DSL rule definition: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析条件表达式
     */
    private RuleCondition parseCondition(String conditionText) {
        RuleCondition condition = new RuleCondition();
        
        // 简单解析：支持AND连接的多个条件
        if (conditionText.contains(" AND ")) {
            condition.setOperator(LogicalOperator.AND);
            String[] parts = conditionText.split(" AND ");
            List<RuleCondition> subConditions = new ArrayList<>();
            for (String part : parts) {
                subConditions.add(parseSingleCondition(part.trim()));
            }
            condition.setSubConditions(subConditions);
        } else if (conditionText.contains(" OR ")) {
            condition.setOperator(LogicalOperator.OR);
            String[] parts = conditionText.split(" OR ");
            List<RuleCondition> subConditions = new ArrayList<>();
            for (String part : parts) {
                subConditions.add(parseSingleCondition(part.trim()));
            }
            condition.setSubConditions(subConditions);
        } else {
            return parseSingleCondition(conditionText);
        }
        
        return condition;
    }
    
    /**
     * 解析单个条件
     */
    private RuleCondition parseSingleCondition(String text) {
        RuleCondition condition = new RuleCondition();
        
        // 支持的比较操作符
        if (text.contains(" > ")) {
            String[] parts = text.split(" > ");
            condition.setField(parts[0].trim());
            condition.setOperator(ComparisonOperator.GREATER_THAN);
            condition.setValue(parseValue(parts[1].trim()));
        } else if (text.contains(" < ")) {
            String[] parts = text.split(" < ");
            condition.setField(parts[0].trim());
            condition.setOperator(ComparisonOperator.LESS_THAN);
            condition.setValue(parseValue(parts[1].trim()));
        } else if (text.contains(" == ")) {
            String[] parts = text.split(" == ");
            condition.setField(parts[0].trim());
            condition.setOperator(ComparisonOperator.EQUALS);
            condition.setValue(parseValue(parts[1].trim()));
        } else if (text.contains(" != ")) {
            String[] parts = text.split(" != ");
            condition.setField(parts[0].trim());
            condition.setOperator(ComparisonOperator.NOT_EQUALS);
            condition.setValue(parseValue(parts[1].trim()));
        } else if (text.contains(" >= ")) {
            String[] parts = text.split(" >= ");
            condition.setField(parts[0].trim());
            condition.setOperator(ComparisonOperator.GREATER_THAN_OR_EQUAL);
            condition.setValue(parseValue(parts[1].trim()));
        } else if (text.contains(" <= ")) {
            String[] parts = text.split(" <= ");
            condition.setField(parts[0].trim());
            condition.setOperator(ComparisonOperator.LESS_THAN_OR_EQUAL);
            condition.setValue(parseValue(parts[1].trim()));
        } else {
            // 默认作为表达式
            condition.setExpression(text);
        }
        
        return condition;
    }
    
    /**
     * 解析值
     */
    private Object parseValue(String valueText) {
        // 移除引号
        if (valueText.startsWith("\"") && valueText.endsWith("\"")) {
            return valueText.substring(1, valueText.length() - 1);
        }
        
        // 尝试解析为数字
        try {
            if (valueText.contains(".")) {
                return Double.parseDouble(valueText);
            } else {
                return Integer.parseInt(valueText);
            }
        } catch (NumberFormatException e) {
            return valueText;
        }
    }
    
    /**
     * 解析动作
     */
    private RuleAction parseAction(String actionText) {
        RuleAction action = new RuleAction();
        Map<String, Object> outputs = new HashMap<>();
        
        // 解析多个动作（用AND连接）
        String[] actionParts = actionText.split(" AND ");
        for (String part : actionParts) {
            part = part.trim();
            
            // 解析赋值操作：SET variable = value
            if (part.startsWith("SET ")) {
                String assignment = part.substring(4).trim();
                String[] assignParts = assignment.split("=");
                if (assignParts.length == 2) {
                    String var = assignParts[0].trim();
                    Object value = parseValue(assignParts[1].trim());
                    outputs.put(var, value);
                }
            }
            // 解析执行操作：EXECUTE action_name
            else if (part.startsWith("EXECUTE ")) {
                String actionName = part.substring(8).trim();
                action.setActionType("execute");
                action.setActionName(actionName);
            }
        }
        
        if (!outputs.isEmpty()) {
            action.setOutputs(outputs);
        }
        
        return action;
    }
    
    /**
     * 生成规则ID
     */
    private String generateRuleId(String ruleName) {
        return "rule_" + ruleName.replaceAll("\\s+", "_").toLowerCase();
    }
    
    @Override
    public ValidationResult validate(String source) {
        ValidationResult result = new ValidationResult();
        
        if (source == null || source.trim().isEmpty()) {
            result.addError("Source cannot be null or empty");
            return result;
        }
        
        try {
            RuleDefinition rule = parse(source);
            return rule.validate();
        } catch (RuleParseException e) {
            result.addError("DSL parse error: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public String serialize(RuleDefinition rule) throws RuleSerializationException {
        if (rule == null) {
            throw new RuleSerializationException("Rule cannot be null");
        }
        
        if (!(rule instanceof ExpressionRule)) {
            throw new RuleSerializationException("Only ExpressionRule can be serialized to DSL format");
        }
        
        ExpressionRule expRule = (ExpressionRule) rule;
        StringBuilder dsl = new StringBuilder();
        
        dsl.append("RULE \"").append(rule.getRuleName()).append("\" {\n");
        dsl.append("  WHEN ");
        
        // 序列化条件
        if (expRule.getCondition() != null) {
            dsl.append(serializeCondition(expRule.getCondition()));
        }
        
        dsl.append("\n  THEN ");
        
        // 序列化动作
        if (expRule.getAction() != null) {
            dsl.append(serializeAction(expRule.getAction()));
        }
        
        dsl.append("\n}");
        
        return dsl.toString();
    }
    
    /**
     * 序列化条件
     */
    private String serializeCondition(RuleCondition condition) {
        if (condition.getExpression() != null) {
            return condition.getExpression();
        }
        
        if (condition.getSubConditions() != null && !condition.getSubConditions().isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (RuleCondition sub : condition.getSubConditions()) {
                parts.add(serializeCondition(sub));
            }
            String operator = condition.getOperator() == LogicalOperator.AND ? " AND " : " OR ";
            return String.join(operator, parts);
        }
        
        // 单个条件
        StringBuilder sb = new StringBuilder();
        sb.append(condition.getField());
        
        if (condition.getOperator() instanceof ComparisonOperator) {
            ComparisonOperator op = (ComparisonOperator) condition.getOperator();
            switch (op) {
                case EQUALS:
                    sb.append(" == ");
                    break;
                case NOT_EQUALS:
                    sb.append(" != ");
                    break;
                case GREATER_THAN:
                    sb.append(" > ");
                    break;
                case LESS_THAN:
                    sb.append(" < ");
                    break;
                case GREATER_THAN_OR_EQUAL:
                    sb.append(" >= ");
                    break;
                case LESS_THAN_OR_EQUAL:
                    sb.append(" <= ");
                    break;
            }
        }
        
        Object value = condition.getValue();
        if (value instanceof String) {
            sb.append("\"").append(value).append("\"");
        } else {
            sb.append(value);
        }
        
        return sb.toString();
    }
    
    /**
     * 序列化动作
     */
    private String serializeAction(RuleAction action) {
        List<String> parts = new ArrayList<>();
        
        if (action.getOutputs() != null) {
            for (Map.Entry<String, Object> entry : action.getOutputs().entrySet()) {
                Object value = entry.getValue();
                String valueStr = value instanceof String ? 
                    "\"" + value + "\"" : String.valueOf(value);
                parts.add("SET " + entry.getKey() + " = " + valueStr);
            }
        }
        
        if (action.getActionName() != null) {
            parts.add("EXECUTE " + action.getActionName());
        }
        
        return String.join(" AND ", parts);
    }
    
    @Override
    public RuleFormatType getFormat() {
        return RuleFormatType.DSL;
    }
    
    @Override
    public String getProtocolName() {
        return "DSL Rule Definition Protocol";
    }
    
    @Override
    public String getProtocolVersion() {
        return "1.0.0";
    }
}
