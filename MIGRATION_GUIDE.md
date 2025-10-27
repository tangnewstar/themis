# Themis规则引擎迁移指南

## 概述

本文档提供从旧版规则引擎迁移到Themis新一代规则引擎的完整指南,包括迁移策略、步骤、注意事项和最佳实践。

## 迁移策略

### 1. 双引擎并行策略

在迁移期间,同时运行旧引擎和新引擎,通过灰度发布逐步切换流量。

**优势:**
- 风险可控,可随时回滚
- 可以对比两个引擎的执行结果
- 平滑过渡,不影响业务

**适用场景:**
- 核心业务规则
- 高流量场景
- 对稳定性要求高的场景

### 2. 直接替换策略

停止旧引擎,直接切换到新引擎。

**优势:**
- 迁移速度快
- 维护成本低

**适用场景:**
- 非核心业务
- 低流量场景
- 测试环境

## 迁移步骤

### 第一阶段:准备(1-2周)

#### 1. 环境准备

```bash
# 安装Themis规则引擎
git clone https://github.com/your-org/themis.git
cd themis
mvn clean install
```

#### 2. 规则清单

梳理现有规则:
- 统计规则数量
- 识别规则类型
- 评估复杂度
- 标记核心规则

#### 3. 依赖分析

检查规则依赖:
- 外部服务依赖
- 数据源依赖
- 第三方库依赖

### 第二阶段:规则转换(2-4周)

#### 1. 使用适配器自动转换

```java
// 创建适配器
LegacyRuleAdapter adapter = new LegacyRuleAdapter();

// 加载旧版规则
List<Map<String, Object>> legacyRules = loadLegacyRules();

// 批量转换
List<RuleDefinition> newRules = adapter.adaptLegacyRules(legacyRules);

// 保存新规则
for (RuleDefinition rule : newRules) {
    ruleRepository.save(rule);
}
```

#### 2. 手动调整复杂规则

对于自动转换失败的规则,需要手动调整:

```java
// 旧版规则(示例)
Map<String, Object> legacyRule = Map.of(
    "id", "RISK_001",
    "condition", "amount > 10000 && riskScore > 80",
    "action", "REJECT"
);

// 新版规则
ExpressionRule newRule = new ExpressionRule("RISK_001", "高风险交易拦截");
newRule.setCondition(new SimpleCondition("amount > 10000 && riskScore > 80"));
newRule.addAction(new SetVariableAction("decision", "REJECT"));
newRule.setPriority(1);
```

#### 3. 规则验证

```java
// 验证转换后的规则
ValidationResult validation = newRule.validate();
if (!validation.isValid()) {
    System.out.println("Validation errors: " + validation.getErrors());
}
```

### 第三阶段:灰度发布(2-4周)

#### 1. 初始化灰度管理器

```java
// 创建灰度管理器
GrayReleaseManager grayManager = new GrayReleaseManager(
    legacyEngine,  // 旧引擎
    newEngine      // 新引擎
);

// 配置灰度策略
GrayConfig config = new GrayConfig();
config.setStrategy(GrayStrategy.PERCENTAGE);
config.setNewEnginePercentage(1);  // 初始1%流量
config.setCompareMode(true);       // 启用对比模式

grayManager.updateGrayConfig(config);
```

#### 2. 逐步提升流量

```bash
# 周1: 1%流量
# 观察指标,无异常则继续

# 周2: 10%流量
grayManager.increaseNewEngineTraffic(9);

# 周3: 50%流量
grayManager.increaseNewEngineTraffic(40);

# 周4: 100%流量
grayManager.switchToNewEngine();
```

#### 3. 监控关键指标

```java
// 获取灰度统计
GrayStats stats = grayManager.getStats();

System.out.println("旧引擎执行次数: " + stats.getLegacyCount());
System.out.println("新引擎执行次数: " + stats.getNewCount());
System.out.println("旧引擎成功率: " + stats.getLegacySuccessRate() + "%");
System.out.println("新引擎成功率: " + stats.getNewSuccessRate() + "%");
System.out.println("结果一致性: " + stats.getComparisonMatchRate() + "%");
```

### 第四阶段:完全切换(1周)

#### 1. 关闭对比模式

```java
config.setCompareMode(false);
grayManager.updateGrayConfig(config);
```

#### 2. 完全切换到新引擎

```java
grayManager.switchToNewEngine();
```

#### 3. 下线旧引擎

- 停止旧引擎服务
- 归档旧规则数据
- 清理旧依赖

## 回滚方案

### 快速回滚

```java
// 立即回滚到旧引擎
grayManager.rollbackToLegacyEngine();
```

### 逐步回滚

```java
// 降低新引擎流量
config.setNewEnginePercentage(50);  // 降至50%
grayManager.updateGrayConfig(config);

// 继续观察,必要时完全回滚
```

## 注意事项

### 1. 性能优化

- 预编译常用规则
- 配置合理的缓存策略
- 使用对象池减少GC压力

```java
// 启用表达式预编译
ExpressionCompiler compiler = new ExpressionCompiler();
compiler.compile(rule.getCondition());

// 配置缓存
FunctionResultCache.CacheConfig cacheConfig = new FunctionResultCache.CacheConfig();
cacheConfig.setMaxSize(10000);
cacheConfig.setTtlMillis(TimeUnit.MINUTES.toMillis(30));
```

### 2. 数据一致性

- 确保新旧引擎使用相同的数据源
- 同步更新规则配置
- 验证因子计算结果一致性

### 3. 异常处理

```java
try {
    RuleExecutionResult result = engine.execute(rule, context);
    if (!result.isSuccess()) {
        // 记录错误日志
        logger.error("Rule execution failed: {}", result.getError());
        
        // 降级处理
        return fallbackResult();
    }
} catch (Exception e) {
    logger.error("Exception in rule execution", e);
    // 触发告警
    alertService.sendAlert("Rule execution exception: " + e.getMessage());
}
```

## 最佳实践

### 1. 分批迁移

按业务线或规则类型分批迁移:

- **第一批**: 简单规则,低流量场景
- **第二批**: 中等复杂度规则
- **第三批**: 核心业务规则

### 2. 完整测试

```java
// 单元测试
@Test
public void testRuleMigration() {
    RuleDefinition newRule = adapter.adaptLegacyRule(legacyRule);
    assertNotNull(newRule);
    assertTrue(newRule.validate().isValid());
}

// 集成测试
@Test
public void testEndToEnd() {
    RuleContext context = createTestContext();
    RuleExecutionResult result = engine.execute(rule, context);
    assertEquals(expectedResult, result.getResult());
}
```

### 3. 监控告警

配置关键指标监控:
- 规则执行成功率 < 99% 告警
- 平均执行时间 > 100ms 告警
- 结果差异率 > 1% 告警

### 4. 文档维护

- 记录所有规则变更
- 更新业务文档
- 培训业务人员

## 常见问题

### Q1: 迁移过程中发现结果不一致怎么办?

**A**: 
1. 开启对比模式记录差异
2. 分析差异原因(可能是浮点数精度、时区等)
3. 调整规则配置或适配器逻辑
4. 重新验证

### Q2: 性能不如旧引擎怎么办?

**A**:
1. 检查是否启用了预编译
2. 调整缓存配置
3. 使用性能监控定位瓶颈
4. 考虑使用更高效的引擎类型(如RETE引擎)

### Q3: 如何处理依赖外部服务的规则?

**A**:
1. 使用插件机制封装外部服务调用
2. 配置合理的超时和重试策略
3. 提供降级方案

## 技术支持

- 技术文档: https://github.com/your-org/themis/wiki
- Issue提交: https://github.com/your-org/themis/issues
- 技术社区: https://community.your-org.com/themis

## 总结

迁移到Themis新一代规则引擎是一个系统工程,需要充分的准备、详细的计划和持续的监控。遵循本指南的步骤和最佳实践,可以确保平滑、安全地完成迁移。

祝迁移顺利!
