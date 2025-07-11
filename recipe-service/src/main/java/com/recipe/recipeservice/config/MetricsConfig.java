package com.recipe.recipeservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter recipeCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("recipe.created")
                .description("Number of recipes created")
                .register(meterRegistry);
    }

    @Bean
    public Counter recipeUpdatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("recipe.updated")
                .description("Number of recipes updated")
                .register(meterRegistry);
    }

    @Bean
    public Counter recipeDeletedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("recipe.deleted")
                .description("Number of recipes deleted")
                .register(meterRegistry);
    }

    @Bean
    public Timer recipeFilterTimer(MeterRegistry meterRegistry) {
        return Timer.builder("recipe.filter")
                .description("Time taken to filter recipes")
                .register(meterRegistry);
    }
}