package com.themis.app.config;

import com.themis.ai.translator.NaturalLanguageTranslator;
import com.themis.ai.translator.impl.DefaultNaturalLanguageTranslator;
import com.themis.core.engine.RuleEngine;
import com.themis.engine.impl.DefaultRuleEngine;
import com.themis.metadata.storage.MetadataStorage;
import com.themis.metadata.storage.impl.InMemoryMetadataStorage;
import com.themis.orchestration.workflow.WorkflowOrchestrator;
import com.themis.orchestration.workflow.impl.DefaultWorkflowOrchestrator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Main configuration for Themis Rule Engine
 */
@Configuration
public class ThemisConfiguration {
    
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public MetadataStorage metadataStorage() {
        return new InMemoryMetadataStorage();
    }
    
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public RuleEngine ruleEngine(MetadataStorage metadataStorage) {
        return new DefaultRuleEngine();
    }
    
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public WorkflowOrchestrator workflowOrchestrator(RuleEngine ruleEngine) {
        return new DefaultWorkflowOrchestrator(ruleEngine);
    }
    
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public NaturalLanguageTranslator naturalLanguageTranslator() {
        return new DefaultNaturalLanguageTranslator();
    }
}