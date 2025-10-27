package com.themis.engine.selector;

import com.themis.core.engine.RuleEngine;
import com.themis.core.model.RuleDefinition;

import java.util.List;
import java.util.Map;

/**
 * 引擎选择结果
 */
public class EngineSelectionResult {
    
    private final RuleEngine selectedEngine;
    private final List<RuleDefinition> rules;
    private final Map<EngineSelector.EngineType, List<RuleDefinition>> groupedRules;
    
    public EngineSelectionResult(RuleEngine selectedEngine, List<RuleDefinition> rules) {
        this(selectedEngine, rules, null);
    }
    
    public EngineSelectionResult(RuleEngine selectedEngine, List<RuleDefinition> rules,
                                Map<EngineSelector.EngineType, List<RuleDefinition>> groupedRules) {
        this.selectedEngine = selectedEngine;
        this.rules = rules;
        this.groupedRules = groupedRules;
    }
    
    public RuleEngine getSelectedEngine() {
        return selectedEngine;
    }
    
    public List<RuleDefinition> getRules() {
        return rules;
    }
    
    public Map<EngineSelector.EngineType, List<RuleDefinition>> getGroupedRules() {
        return groupedRules;
    }
}
