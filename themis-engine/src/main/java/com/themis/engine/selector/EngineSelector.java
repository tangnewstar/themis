package com.themis.engine.selector;

import com.themis.core.engine.RuleEngine;
import com.themis.core.model.RuleDefinition;

import java.util.List;

/**
 * 引擎选择器接口
 * 根据规则特征自动选择最优执行引擎
 */
public interface EngineSelector {
    
    /**
     * 选择最优引擎
     * 
     * @param rule 规则定义
     * @return 选择的引擎
     */
    RuleEngine selectEngine(RuleDefinition rule);
    
    /**
     * 批量选择引擎
     * 
     * @param rules 规则列表
     * @return 引擎选择结果
     */
    EngineSelectionResult selectEngineForBatch(List<RuleDefinition> rules);
    
    /**
     * 注册引擎
     * 
     * @param engineType 引擎类型
     * @param engine 引擎实例
     */
    void registerEngine(EngineType engineType, RuleEngine engine);
    
    /**
     * 引擎类型
     */
    enum EngineType {
        LIGHTWEIGHT("lightweight", "轻量级引擎"),
        RETE("rete", "RETE引擎"),
        SCRIPT("script", "脚本引擎"),
        STREAMING("streaming", "流式引擎");
        
        private final String code;
        private final String description;
        
        EngineType(String code, String description) {
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
