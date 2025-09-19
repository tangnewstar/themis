package com.themis.app;

import com.themis.engine.RuleEngine;
import com.themis.metadata.storage.MetadataStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ThemisApplicationTest {

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private MetadataStorage metadataStorage;

    @Test
    void contextLoads() {
        assertThat(ruleEngine).isNotNull();
        assertThat(metadataStorage).isNotNull();
    }

    @Test
    void applicationStarts() {
        // Test that the application context loads successfully
        assertThat(true).isTrue();
    }
}