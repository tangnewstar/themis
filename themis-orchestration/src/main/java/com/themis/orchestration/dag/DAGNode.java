package com.themis.orchestration.dag;

import java.util.Map;

/**
 * DAG节点接口
 */
public interface DAGNode {
    
    /**
     * 获取节点ID
     */
    String getId();
    
    /**
     * 获取节点名称
     */
    String getName();
    
    /**
     * 获取节点类型
     */
    NodeType getType();
    
    /**
     * 执行节点逻辑
     * 
     * @param context 执行上下文
     * @param previousResults 前置节点的执行结果
     * @return 节点执行结果
     */
    Object execute(Map<String, Object> context, Map<String, Object> previousResults);
    
    /**
     * 节点类型
     */
    enum NodeType {
        RULE("rule", "规则节点"),
        DECISION("decision", "决策节点"),
        PARALLEL("parallel", "并行节点"),
        SUBPROCESS("subprocess", "子流程节点"),
        SCRIPT("script", "脚本节点"),
        SERVICE("service", "服务节点");
        
        private final String code;
        private final String description;
        
        NodeType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
