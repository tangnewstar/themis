package com.themis.engine.executor;

import com.themis.core.engine.RuleContext;
import com.themis.core.model.RuleAction;

import java.util.Map;

/**
 * Interface for executing rule actions.
 */
public interface ActionExecutor {
    
    /**
     * Executes a rule action with the given context.
     * 
     * @param context The rule execution context
     * @param action The action to execute
     * @return Map of output values produced by the action
     */
    Map<String, Object> execute(RuleContext context, RuleAction action);
    
    /**
     * Checks if this executor supports the given action type.
     * 
     * @param actionType The action type to check
     * @return true if supported, false otherwise
     */
    boolean supports(RuleAction.ActionType actionType);
}