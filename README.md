# Themis Rule Engine

A comprehensive, enterprise-grade rule engine designed for scalability, flexibility, and ease of use. Themis provides a complete solution for business rule management with support for multiple rule types, execution engines, and storage backends.

## Features

### 🎯 Core Capabilities
- **Multiple Rule Types**: Expression, Branching, Formula, Table, and Scorecard rules
- **Flexible Execution Engines**: Default sequential engine and RETE-based engine for complex scenarios
- **Pluggable Storage Backends**: Database (JDBC), Redis, In-Memory, and Etcd support
- **REST API**: Comprehensive REST endpoints for rule management and execution
- **Fluent API**: Intuitive Java API for programmatic rule creation and management

### 🚀 Advanced Features
- **Workflow Orchestration**: Complex rule workflow execution with state management
- **AI-Powered Translation**: Natural language to rule conversion using Stanford CoreNLP
- **Spring Boot Integration**: Ready-to-use Spring Boot application with auto-configuration
- **Comprehensive Testing**: Unit tests, integration tests, and test containers
- **Monitoring & Metrics**: Built-in metrics and health checks

## Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- (Optional) Redis, MySQL, or Etcd for persistent storage

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/your-org/themis-rule-engine.git
cd themis-rule-engine
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
cd themis-app
mvn spring-boot:run
```

The application will start on `http://localhost:8080/themis`

### API Documentation
Once running, access the interactive API documentation at:
- Swagger UI: `http://localhost:8080/themis/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/themis/api-docs`

## Architecture

### Module Structure
```
themis-rule-engine/
├── themis-core/           # Core interfaces and models
├── themis-engine/         # Rule execution engines
├── themis-metadata/       # Metadata storage implementations
├── themis-api/            # REST API and Fluent API
├── themis-orchestration/  # Workflow orchestration
├── themis-ai/             # AI-powered natural language translation
└── themis-app/            # Spring Boot application
```

### Rule Types

#### 1. Expression Rules
Simple condition-action rules for basic business logic.

```java
ExpressionRule rule = RuleBuilder.expression()
    .id("eligibility-check")
    .name("Age Eligibility")
    .condition("age >= 18")
    .action("eligible = true")
    .build();
```

#### 2. Formula Rules
Mathematical calculations and variable assignments.

```java
FormulaRule rule = RuleBuilder.formula()
    .id("total-calculation")
    .name("Calculate Total")
    .outputVariable("total")
    .formula("price * quantity * (1 + taxRate)")
    .build();
```

#### 3. Branching Rules
Complex conditional logic with multiple execution paths.

```java
BranchingRule rule = RuleBuilder.branching()
    .id("discount-logic")
    .name("Discount Calculation")
    .condition("customerType == 'PREMIUM'")
    .trueBranch("discount = 0.15")
    .falseBranch("discount = 0.05")
    .build();
```

#### 4. Table Rules
Decision tables for lookup-based logic.

```java
TableRule rule = RuleBuilder.table()
    .id("shipping-table")
    .name("Shipping Cost Table")
    .inputColumns(Arrays.asList("weight", "zone"))
    .outputColumns(Arrays.asList("cost"))
    .addRow(Arrays.asList("0-5", "domestic"), Arrays.asList("10.00"))
    .addRow(Arrays.asList("5-10", "domestic"), Arrays.asList("15.00"))
    .build();
```

#### 5. Scorecard Rules
Credit scoring and risk assessment rules.

```java
ScorecardRule rule = RuleBuilder.scorecard()
    .id("credit-score")
    .name("Credit Risk Assessment")
    .addCharacteristic("income", 0.4, 850)
    .addCharacteristic("creditHistory", 0.3, 750)
    .addCharacteristic("debtRatio", 0.3, 650)
    .build();
```

## Usage Examples

### Basic Rule Execution

```java
// Create and save a rule
ExpressionRule rule = RuleBuilder.expression()
    .id("discount-rule")
    .condition("orderAmount > 100")
    .action("discount = 0.1")
    .build();

metadataStorage.saveRule(rule);

// Execute the rule
Map<String, Object> context = Map.of("orderAmount", 150);
Object result = ruleEngine.execute("discount-rule", context);
```

### REST API Usage

**Create a rule:**
```bash
curl -X POST http://localhost:8080/themis/api/rules/execute \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "discount-rule",
    "context": {
      "orderAmount": 150
    }
  }'
```

**Execute a rule:**
```bash
curl -X POST http://localhost:8080/themis/api/rules/execute \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "discount-rule",
    "context": {
      "orderAmount": 150
    }
  }'
```

### AI-Powered Rule Generation

```java
// Natural language to rule translation
String description = "if customer age is greater than 18 then set eligible to true";
RuleDefinition rule = aiRuleService.generateRule(description);

// Generate multiple candidates
List<RuleDefinition> candidates = aiRuleService.generateRuleCandidates(
    "calculate total as price plus tax", 3);
```

### Workflow Orchestration

```java
// Build a workflow
WorkflowDefinition workflow = WorkflowBuilder.create()
    .id("order-processing")
    .name("Order Processing Workflow")
    .addRuleStep("validation", "Validate Order")
        .ruleId("order-validation-rule")
        .and()
    .addDecisionStep("discount-check", "Check Discount Eligibility")
        .conditionExpression("orderAmount > 100")
        .and()
    .addRuleStep("discount-calc", "Calculate Discount")
        .ruleId("discount-calculation-rule")
        .dependsOn("discount-check")
        .and()
    .build();

// Execute the workflow
WorkflowExecutionResult result = workflowOrchestrator.executeWorkflow(workflow, context);
```

## Configuration

### Storage Backends

#### In-Memory Storage (Default)
```properties
themis.metadata.storage.type=in-memory
```

#### Database Storage
```properties
themis.metadata.storage.type=database
spring.datasource.url=jdbc:mysql://localhost:3306/themis
spring.datasource.username=your_username
spring.datasource.password=your_password
```

#### Redis Storage
```properties
themis.metadata.storage.type=redis
spring.redis.host=localhost
spring.redis.port=6379
```

#### Etcd Storage
```properties
themis.metadata.storage.type=etcd
themis.etcd.endpoints=http://localhost:2379
```

### Engine Configuration
```properties
# Rule engine settings
themis.engine.default-execution-mode=sequential
themis.engine.max-concurrent-executions=10
themis.engine.enable-caching=true
themis.engine.cache-size=1000

# AI translation settings
themis.ai.enabled=true
themis.ai.confidence-threshold=0.3
themis.ai.max-candidates=5
```

## Development

### Building from Source

```bash
# Clone the repository
git clone https://github.com/your-org/themis-rule-engine.git
cd themis-rule-engine

# Build all modules
mvn clean install

# Run tests
mvn test

# Run integration tests
mvn verify
```

### Module Dependencies
```
themis-app
├── themis-api
│   ├── themis-core
│   └── themis-engine
├── themis-orchestration
│   ├── themis-core
│   └── themis-engine
├── themis-ai
│   ├── themis-core
│   └── themis-api
├── themis-engine
│   ├── themis-core
│   └── themis-metadata
└── themis-metadata
    └── themis-core
```

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn failsafe:integration-test

# Specific module tests
mvn test -pl themis-engine

# Test with coverage
mvn test jacoco:report
```

## Monitoring and Operations

### Health Checks
- Application health: `http://localhost:8080/themis/actuator/health`
- Database connectivity, storage backend status

### Metrics
- Rule execution metrics
- Performance counters
- Error rates
- Cache hit ratios

### Prometheus Integration
```properties
management.metrics.export.prometheus.enabled=true
```
Access metrics at: `http://localhost:8080/themis/actuator/prometheus`

## Best Practices

### Rule Design
1. **Keep rules simple**: Each rule should have a single responsibility
2. **Use meaningful names**: Rule IDs and names should be descriptive
3. **Version your rules**: Use semantic versioning for rule changes
4. **Test thoroughly**: Write unit tests for complex rules

### Performance
1. **Use caching**: Enable rule caching for frequently executed rules
2. **Choose the right engine**: Use RETE engine for complex rule sets
3. **Optimize storage**: Choose appropriate storage backend for your use case
4. **Monitor execution times**: Set up alerts for slow rule execution

### Security
1. **Validate inputs**: Always validate rule context data
2. **Sanitize expressions**: Be careful with dynamic expression evaluation
3. **Use authentication**: Secure API endpoints appropriately
4. **Audit rule changes**: Log all rule modifications

## Troubleshooting

### Common Issues

#### Rule Not Found
```
Error: Rule with ID 'xyz' not found
Solution: Check rule ID spelling and ensure rule is saved to metadata storage
```

#### Expression Evaluation Failed
```
Error: Failed to evaluate expression 'age > 18'
Solution: Ensure context contains required variables and expressions are valid
```

#### Storage Connection Issues
```
Error: Cannot connect to Redis/Database
Solution: Check connection settings and ensure storage backend is running
```

### Debug Mode
Enable debug logging to troubleshoot issues:
```properties
logging.level.com.themis=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- **Documentation**: [Wiki](https://github.com/your-org/themis-rule-engine/wiki)
- **Issues**: [GitHub Issues](https://github.com/your-org/themis-rule-engine/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-org/themis-rule-engine/discussions)
- **Email**: support@themis-engine.com

## Roadmap

### Version 1.1
- [ ] GraphQL API support
- [ ] Rule versioning and rollback
- [ ] Advanced caching strategies
- [ ] Performance optimizations

### Version 1.2
- [ ] Rule debugging and profiling tools
- [ ] Visual rule designer
- [ ] Advanced AI features
- [ ] Cloud-native deployment options

### Version 2.0
- [ ] Distributed rule execution
- [ ] Event-driven rule processing
- [ ] Machine learning integration
- [ ] Enterprise security features