# API Documentation

## Overview

The Themis Rule Engine provides both REST API and Fluent Java API for rule management and execution. This document covers all available endpoints and usage examples.

## Authentication

Currently, the API uses basic authentication. Include the Authorization header in your requests:

```
Authorization: Basic <base64-encoded-credentials>
```

## REST API Endpoints

### Rule Management

#### Get All Rules
```http
GET /api/rules
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "rule-1",
      "name": "Sample Rule",
      "type": "expression",
      "description": "A sample expression rule",
      "created": "2024-01-01T10:00:00Z",
      "modified": "2024-01-01T10:00:00Z"
    }
  ]
}
```

#### Get Rule by ID
```http
GET /api/rules/{ruleId}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "rule-1",
    "name": "Sample Rule",
    "type": "expression",
    "condition": "age > 18",
    "action": "eligible = true",
    "description": "Check age eligibility"
  }
}
```

#### Create Rule
```http
POST /api/rules
Content-Type: application/json

{
  "id": "new-rule",
  "name": "New Rule",
  "type": "expression",
  "condition": "score > 80",
  "action": "status = 'approved'"
}
```

#### Update Rule
```http
PUT /api/rules/{ruleId}
Content-Type: application/json

{
  "name": "Updated Rule",
  "condition": "score > 85",
  "action": "status = 'approved'"
}
```

#### Delete Rule
```http
DELETE /api/rules/{ruleId}
```

### Rule Execution

#### Execute Single Rule
```http
POST /api/rules/execute
Content-Type: application/json

{
  "ruleId": "rule-1",
  "context": {
    "age": 25,
    "score": 90
  }
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "ruleId": "rule-1",
    "result": true,
    "context": {
      "age": 25,
      "score": 90,
      "eligible": true
    },
    "executionTime": 15
  }
}
```

#### Execute Multiple Rules
```http
POST /api/rules/execute/batch
Content-Type: application/json

{
  "ruleIds": ["rule-1", "rule-2"],
  "context": {
    "age": 25,
    "income": 50000
  }
}
```

#### Execute Rules by Group
```http
POST /api/rules/execute/group/{groupId}
Content-Type: application/json

{
  "context": {
    "age": 25,
    "income": 50000
  }
}
```

### AI Translation

#### Translate Natural Language to Rule
```http
POST /api/ai/translate
Content-Type: application/json

{
  "text": "if customer age is greater than 18 then set eligible to true",
  "ruleType": "expression"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "generated-rule-123456",
    "name": "if customer age is",
    "type": "expression",
    "condition": "age > 18",
    "action": "eligible = true",
    "description": "Generated from: if customer age is greater than 18 then set eligible to true"
  }
}
```

#### Generate Rule Candidates
```http
POST /api/ai/candidates
Content-Type: application/json

{
  "text": "calculate total price including tax",
  "maxCandidates": 3
}
```

#### Validate Natural Language Input
```http
POST /api/ai/validate
Content-Type: application/json

{
  "text": "if age is greater than 18"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "confidence": 0.85,
    "suggestedRuleType": "expression",
    "issues": []
  }
}
```

### Workflow Management

#### Create Workflow
```http
POST /api/workflows
Content-Type: application/json

{
  "id": "order-processing",
  "name": "Order Processing Workflow",
  "description": "Process customer orders",
  "steps": [
    {
      "id": "validate",
      "name": "Validate Order",
      "type": "RULE_EXECUTION",
      "configuration": {
        "ruleId": "order-validation"
      }
    }
  ]
}
```

#### Execute Workflow
```http
POST /api/workflows/{workflowId}/execute
Content-Type: application/json

{
  "context": {
    "orderId": "ORD-123",
    "customerId": "CUST-456"
  }
}
```

## Java Fluent API

### Rule Builder Examples

#### Expression Rule
```java
ExpressionRule rule = RuleBuilder.expression()
    .id("age-check")
    .name("Age Verification")
    .description("Check if person is adult")
    .condition("age >= 18")
    .action("isAdult = true")
    .metadata("category", "validation")
    .build();
```

#### Formula Rule
```java
FormulaRule rule = RuleBuilder.formula()
    .id("tax-calculation")
    .name("Tax Calculation")
    .outputVariable("totalTax")
    .formula("baseAmount * taxRate + additionalFees")
    .inputVariables("baseAmount", "taxRate", "additionalFees")
    .build();
```

#### Table Rule
```java
TableRule rule = RuleBuilder.table()
    .id("shipping-rates")
    .name("Shipping Rate Table")
    .inputColumns("weight", "zone")
    .outputColumns("shippingCost")
    .addRow("0-5", "domestic", "10.00")
    .addRow("5-10", "domestic", "15.00")
    .addRow("0-5", "international", "25.00")
    .defaultValue("0.00")
    .build();
```

#### Scorecard Rule
```java
ScorecardRule rule = RuleBuilder.scorecard()
    .id("credit-assessment")
    .name("Credit Risk Scorecard")
    .addCharacteristic("income")
        .weight(0.4)
        .baseScore(600)
        .addBin("< 30000", -50)
        .addBin("30000-60000", 0)
        .addBin("> 60000", 50)
        .and()
    .addCharacteristic("creditHistory")
        .weight(0.3)
        .baseScore(650)
        .addBin("poor", -100)
        .addBin("fair", -25)
        .addBin("good", 25)
        .addBin("excellent", 75)
        .and()
    .build();
```

### Workflow Builder Examples

```java
WorkflowDefinition workflow = WorkflowBuilder.create()
    .id("loan-approval")
    .name("Loan Approval Process")
    .description("Automated loan approval workflow")
    
    // Step 1: Validate application
    .addRuleStep("validation", "Application Validation")
        .ruleId("loan-validation-rule")
        .failOnError(true)
        .outputVariable("validationResult")
        .and()
    
    // Step 2: Credit check (depends on validation)
    .addRuleStep("credit-check", "Credit Assessment")
        .ruleId("credit-scorecard")
        .dependsOn("validation")
        .condition("validationResult == true")
        .outputVariable("creditScore")
        .and()
    
    // Step 3: Decision branch
    .addDecisionStep("approval-decision", "Approval Decision")
        .conditionExpression("creditScore >= 650")
        .outputVariable("isApproved")
        .dependsOn("credit-check")
        .and()
    
    // Step 4a: Approve loan (parallel with rejection)
    .addRuleStep("approve-loan", "Approve Loan")
        .ruleId("loan-approval-rule")
        .condition("isApproved == true")
        .dependsOn("approval-decision")
        .and()
    
    // Step 4b: Reject loan
    .addRuleStep("reject-loan", "Reject Loan")
        .ruleId("loan-rejection-rule")
        .condition("isApproved == false")
        .dependsOn("approval-decision")
        .and()
    
    .build();
```

### Engine Usage Examples

#### Basic Execution
```java
@Autowired
private RuleEngine ruleEngine;

@Autowired
private MetadataStorage metadataStorage;

// Save rule
ExpressionRule rule = RuleBuilder.expression()
    .id("discount-rule")
    .condition("orderAmount > 100")
    .action("discount = orderAmount * 0.1")
    .build();

metadataStorage.saveRule(rule);

// Execute rule
Map<String, Object> context = new HashMap<>();
context.put("orderAmount", 150.0);

Object result = ruleEngine.execute("discount-rule", context);
// context now contains: {orderAmount=150.0, discount=15.0}
```

#### Batch Execution
```java
List<String> ruleIds = Arrays.asList("rule1", "rule2", "rule3");
Map<String, Object> context = new HashMap<>();
context.put("age", 25);
context.put("income", 50000);

Map<String, Object> results = ruleEngine.executeBatch(ruleIds, context);
```

#### Parallel Execution
```java
CompletableFuture<Object> future = ruleEngine.executeAsync("rule-id", context);
Object result = future.get(5, TimeUnit.SECONDS);
```

## Error Handling

### HTTP Status Codes

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Access denied
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `422 Unprocessable Entity` - Validation failed
- `500 Internal Server Error` - Server error

### Error Response Format

```json
{
  "success": false,
  "error": {
    "code": "RULE_NOT_FOUND",
    "message": "Rule with ID 'xyz' not found",
    "details": {
      "ruleId": "xyz",
      "timestamp": "2024-01-01T10:00:00Z"
    }
  }
}
```

### Common Error Codes

- `RULE_NOT_FOUND` - Rule with specified ID doesn't exist
- `INVALID_RULE_DEFINITION` - Rule definition is malformed
- `EXECUTION_FAILED` - Rule execution encountered an error
- `VALIDATION_FAILED` - Request validation failed
- `STORAGE_ERROR` - Storage backend error
- `AI_TRANSLATION_FAILED` - Natural language translation failed

## Rate Limiting

API requests are rate-limited to prevent abuse:

- **Default**: 100 requests per minute per IP
- **Authenticated**: 1000 requests per minute per user
- **Enterprise**: Custom limits available

Rate limit headers:
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200
```

## Pagination

List endpoints support pagination:

```http
GET /api/rules?page=1&size=20&sort=name,asc
```

Response includes pagination metadata:
```json
{
  "success": true,
  "data": [...],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 150,
    "totalPages": 8
  }
}
```

## WebSocket API

Real-time rule execution monitoring:

```javascript
const ws = new WebSocket('ws://localhost:8080/themis/ws/executions');

ws.onmessage = function(event) {
  const execution = JSON.parse(event.data);
  console.log('Rule executed:', execution);
};
```

## SDK and Client Libraries

### Java Client
```java
ThemisClient client = ThemisClient.builder()
    .baseUrl("http://localhost:8080/themis")
    .apiKey("your-api-key")
    .build();

RuleExecutionResult result = client.executeRule("rule-id", context);
```

### JavaScript/Node.js Client
```javascript
const ThemisClient = require('@themis/client');

const client = new ThemisClient({
  baseUrl: 'http://localhost:8080/themis',
  apiKey: 'your-api-key'
});

const result = await client.executeRule('rule-id', context);
```

### Python Client
```python
from themis_client import ThemisClient

client = ThemisClient(
    base_url='http://localhost:8080/themis',
    api_key='your-api-key'
)

result = client.execute_rule('rule-id', context)
```