package com.themis.test.unit;

import com.themis.core.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 规则定义单元测试
 * 
 * 测试覆盖:
 * - 规则创建和验证
 * - 规则类型转换
 * - 规则元数据管理
 */
public class RuleDefinitionTest {
    
    private ExpressionRule testRule;
    
    @BeforeEach
    public void setUp() {
        testRule = new ExpressionRule("TEST_001", "测试规则");
        testRule.setDescription("这是一个测试规则");
        testRule.setPriority(10);
    }
    
    @Test
    public void testRuleCreation() {
        assertNotNull(testRule);
        assertEquals("TEST_001", testRule.getRuleId());
        assertEquals("测试规则", testRule.getRuleName());
        assertEquals(RuleType.EXPRESSION, testRule.getRuleType());
    }
    
    @Test
    public void testRuleValidation() {
        // 设置条件和动作
        testRule.setCondition(new SimpleCondition("amount > 1000"));
        testRule.addAction(new SetVariableAction("result", "PASS"));
        
        ValidationResult result = testRule.validate();
        assertTrue(result.isValid());
    }
    
    @Test
    public void testRuleValidationFailure() {
        // 不设置条件,应该验证失败
        ValidationResult result = testRule.validate();
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() > 0);
    }
    
    @Test
    public void testRuleCopy() {
        testRule.setCondition(new SimpleCondition("amount > 1000"));
        
        RuleDefinition copy = testRule.copy();
        
        assertNotNull(copy);
        assertEquals(testRule.getRuleId(), copy.getRuleId());
        assertEquals(testRule.getRuleName(), copy.getRuleName());
        assertNotSame(testRule, copy); // 应该是不同的对象
    }
    
    @Test
    public void testRuleStatus() {
        testRule.setStatus(RuleStatus.ACTIVE);
        assertEquals(RuleStatus.ACTIVE, testRule.getStatus());
        
        testRule.setStatus(RuleStatus.INACTIVE);
        assertEquals(RuleStatus.INACTIVE, testRule.getStatus());
    }
    
    @Test
    public void testRulePriority() {
        testRule.setPriority(5);
        assertEquals(5, testRule.getPriority());
        
        // 优先级越小,优先级越高
        ExpressionRule highPriority = new ExpressionRule("TEST_002", "高优先级");
        highPriority.setPriority(1);
        
        assertTrue(highPriority.getPriority() < testRule.getPriority());
    }
    
    @Test
    public void testRuleMetadata() {
        testRule.setMetadata(new java.util.HashMap<>());
        testRule.getMetadata().put("category", "风控");
        testRule.getMetadata().put("version", "1.0");
        
        assertEquals("风控", testRule.getMetadata().get("category"));
        assertEquals("1.0", testRule.getMetadata().get("version"));
    }
    
    @Test
    public void testRuleTags() {
        testRule.setTags(new java.util.HashMap<>());
        testRule.getTags().put("type", "amount_check");
        testRule.getTags().put("level", "high");
        
        assertEquals("amount_check", testRule.getTags().get("type"));
        assertEquals("high", testRule.getTags().get("level"));
    }
}
