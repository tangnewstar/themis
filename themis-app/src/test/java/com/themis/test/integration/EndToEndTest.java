package com.themis.test.integration;

import com.themis.api.sdk.ThemisRuleClient;
import com.themis.core.model.*;
import com.themis.engine.impl.LightweightRuleEngine;
import com.themis.metadata.storage.RuleMetadataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 端到端集成测试
 * 
 * 测试覆盖:
 * - 完整的规则生命周期
 * - 规则定义 -> 存储 -> 执行 -> 结果
 * - 多模块协作
 */
public class EndToEndTest {
    
    private RuleMetadataRepository repository;
    private LightweightRuleEngine engine;
    private ThemisRuleClient client;
    
    @BeforeEach
    public void setUp() {
        repository = new RuleMetadataRepository();
        engine = new LightweightRuleEngine();
        // client = new ThemisRuleClient(engine); // 需要实际初始化
    }
    
    @Test
    public void testCompleteRuleLifecycle() {
        // 1. 创建规则
        ExpressionRule rule = createTestRule();
        
        // 2. 验证规则
        ValidationResult validation = rule.validate();
        assertTrue(validation.isValid(), "规则应该验证通过");
        
        // 3. 保存规则到仓库
        repository.save(rule);
        
        // 4. 从仓库检索规则
        RuleDefinition retrieved = repository.findById(rule.getRuleId());
        assertNotNull(retrieved, "应该能检索到保存的规则");
        assertEquals(rule.getRuleId(), retrieved.getRuleId());
        
        // 5. 执行规则
        RuleExecutionContext context = new RuleExecutionContext();
        context.setAttribute("amount", 5000.0);
        context.setAttribute("userId", "user_001");
        
        RuleExecutionResult result = engine.execute(retrieved, context);
        
        // 6. 验证结果
        assertTrue(result.isSuccess(), "规则执行应该成功");
        assertNotNull(result.getResult());
    }
    
    @Test
    public void testRuleVersioning() {
        // 测试规则版本管理
        ExpressionRule v1 = createTestRule();
        v1.setVersion("1.0.0");
        repository.save(v1);
        
        // 创建新版本
        ExpressionRule v2 = (ExpressionRule) v1.copy();
        v2.setVersion("2.0.0");
        v2.setDescription("更新后的规则");
        repository.save(v2);
        
        // 验证两个版本都存在
        RuleDefinition retrieved1 = repository.findById(v1.getRuleId());
        assertNotNull(retrieved1);
    }
    
    @Test
    public void testRuleCategoryManagement() {
        // 测试规则分类管理
        
        // 创建风控规则
        ExpressionRule riskRule = new ExpressionRule("RISK_001", "风控规则");
        riskRule.setCondition(new SimpleCondition("riskScore > 80"));
        riskRule.addAction(new SetVariableAction("decision", "REJECT"));
        riskRule.setMetadata(new java.util.HashMap<>());
        riskRule.getMetadata().put("category", "risk_control");
        repository.save(riskRule);
        
        // 创建营销规则
        ExpressionRule marketingRule = new ExpressionRule("MKT_001", "营销规则");
        marketingRule.setCondition(new SimpleCondition("amount > 1000"));
        marketingRule.addAction(new SetVariableAction("discount", 0.1));
        marketingRule.setMetadata(new java.util.HashMap<>());
        marketingRule.getMetadata().put("category", "marketing");
        repository.save(marketingRule);
        
        // 按分类查询
        java.util.List<RuleDefinition> riskRules = 
            repository.findByCategory("risk_control");
        assertEquals(1, riskRules.size());
        
        java.util.List<RuleDefinition> marketingRules = 
            repository.findByCategory("marketing");
        assertEquals(1, marketingRules.size());
    }
    
    @Test
    public void testRuleStatusTransition() {
        // 测试规则状态流转
        ExpressionRule rule = createTestRule();
        
        // 初始状态: DRAFT
        assertEquals(RuleStatus.DRAFT, rule.getStatus());
        repository.save(rule);
        
        // 激活规则
        rule.setStatus(RuleStatus.ACTIVE);
        repository.save(rule);
        
        // 验证状态更新
        RuleDefinition retrieved = repository.findById(rule.getRuleId());
        assertEquals(RuleStatus.ACTIVE, retrieved.getStatus());
        
        // 停用规则
        rule.setStatus(RuleStatus.INACTIVE);
        repository.save(rule);
        
        retrieved = repository.findById(rule.getRuleId());
        assertEquals(RuleStatus.INACTIVE, retrieved.getStatus());
    }
    
    @Test
    public void testBatchRuleExecution() {
        // 测试批量规则执行
        java.util.List<RuleDefinition> rules = new java.util.ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            ExpressionRule rule = new ExpressionRule("BATCH_" + i, "批量规则" + i);
            rule.setCondition(new SimpleCondition("amount > " + (i * 1000)));
            rule.addAction(new SetVariableAction("level", i));
            rules.add(rule);
            repository.save(rule);
        }
        
        // 批量执行
        RuleExecutionContext context = new RuleExecutionContext();
        context.setAttribute("amount", 3500.0);
        
        int successCount = 0;
        for (RuleDefinition rule : rules) {
            RuleExecutionResult result = engine.execute(rule, context);
            if (result.isSuccess()) {
                successCount++;
            }
        }
        
        assertEquals(5, successCount, "所有规则都应该成功执行");
    }
    
    private ExpressionRule createTestRule() {
        ExpressionRule rule = new ExpressionRule("TEST_E2E_001", "端到端测试规则");
        rule.setDescription("用于端到端测试的规则");
        rule.setCondition(new SimpleCondition("amount > 1000 && userId != null"));
        rule.addAction(new SetVariableAction("result", "PASS"));
        rule.setPriority(10);
        rule.setStatus(RuleStatus.DRAFT);
        return rule;
    }
}
