package com.armaan.vectordb.config;

import com.armaan.vectordb.core.VectorDB;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration providing the singleton {@link VectorDB} instance.
 *
 * <p>Notice that the {@link VectorDB} class itself does not contain any Spring annotations,
 * keeping the core engine completely pure and framework-agnostic.
 */
@Configuration
public class VectorDBConfig {

    @Bean
    public VectorDB vectorDB(@Value("${vectordb.dimensions:16}") int dimensions) {
        return new VectorDB(dimensions);
    }
}
