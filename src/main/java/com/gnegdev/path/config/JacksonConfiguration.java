package com.gnegdev.path.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary // Mark as primary if other ObjectMappers might exist
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // You can add custom configurations here
        // e.g., mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
