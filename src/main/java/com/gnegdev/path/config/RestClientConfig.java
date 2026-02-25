package com.gnegdev.path.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${openrouter.base-url}")
    private String openRouterBaseUrl;

    @Value("${yandex.cloud.base-url}")
    private String yandexCloudBaseUrl;

    @Bean(name = "openRouterRestClient")
    public RestClient openRouterRestClient() {
        return RestClient.builder()
                .baseUrl(openRouterBaseUrl)
                .build();
    }

    @Bean(name = "yandexCloudRestClient")
    public RestClient yandexCloudRestClient() {
        return RestClient.builder()
                .baseUrl(yandexCloudBaseUrl)
                .build();
    }
}
