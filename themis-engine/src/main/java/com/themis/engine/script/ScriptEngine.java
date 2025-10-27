package com.themis.engine.script;

import com.themis.core.engine.RuleEngine;
import com.themis.core.context.RuleContext;
import com.themis.core.model.RuleDefinition;
import com.themis.core.model.RuleExecutionResult;

import java.util.Map;

/**
 * 脚本引擎接口
 * 支持多种脚本语言：Groovy、JavaScript(GraalVM)、Python等
 */
public interface ScriptEngine extends RuleEngine {
    
    /**
     * 执行脚本
     * 
     * @param script 脚本内容
     * @param context 上下文
     * @return 执行结果
     */
    Object executeScript(String script, RuleContext context);
    
    /**
     * 编译脚本(预编译优化)
     * 
     * @param scriptId 脚本ID
     * @param script 脚本内容
     */
    void compileScript(String scriptId, String script);
    
    /**
     * 执行预编译脚本
     * 
     * @param scriptId 脚本ID
     * @param context 上下文
     * @return 执行结果
     */
    Object executeCompiledScript(String scriptId, RuleContext context);
    
    /**
     * 获取脚本引擎类型
     */
    ScriptEngineType getEngineType();
    
    /**
     * 脚本引擎类型
     */
    enum ScriptEngineType {
        /**
         * Groovy脚本引擎
         */
        GROOVY,
        
        /**
         * GraalVM JavaScript引擎
         */
        GRAALVM_JS,
        
        /**
         * GraalVM Python引擎
         */
        GRAALVM_PYTHON,
        
        /**
         * Lua脚本引擎
         */
        LUA
    }
}
