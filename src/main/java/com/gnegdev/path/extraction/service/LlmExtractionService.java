package com.gnegdev.path.extraction.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnegdev.path.extraction.dto.ExtractedDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class LlmExtractionService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${yandex.cloud.api-key}")
    private String apiKey;

    @Value("${yandex.cloud.project}")
    private String project;

    @Value("${yandex.cloud.extraction-prompt-id}")
    private String promptId;

    public LlmExtractionService(
            @Qualifier("yandexCloudRestClient") RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public ExtractedDataDto extract(String medicalHistoryText) {
        Map<String, Object> requestBody = Map.of(
                "prompt", Map.of("id", promptId),
                "input", medicalHistoryText,
                "stream", false
        );

        log.info("Sending medical history text ({} chars) to Yandex Cloud agent, prompt-id: {}",
                medicalHistoryText.length(), promptId);

        String rawResponse = restClient.post()
                .uri("/responses")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("OpenAI-Project", project)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String content = extractOutputText(root);
            content = stripMarkdownCodeBlock(content);
            log.info("LLM extraction completed, parsing JSON response");
            return objectMapper.readValue(content, ExtractedDataDto.class);
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", rawResponse, e);
            throw new RuntimeException("Failed to parse LLM extraction response: " + e.getMessage(), e);
        }
    }

    private String extractOutputText(JsonNode root) {
        // output[0].content[0].text  (Yandex Cloud / OpenAI Responses API)
        JsonNode output = root.path("output");
        if (output.isArray() && !output.isEmpty()) {
            JsonNode content = output.get(0).path("content");
            if (content.isArray() && !content.isEmpty()) {
                String text = content.get(0).path("text").asText(null);
                if (text != null && !text.isEmpty()) return text;
            }
            String text = output.get(0).path("text").asText(null);
            if (text != null && !text.isEmpty()) return text;
        }
        // Fallback: top-level output_text
        String outputText = root.path("output_text").asText(null);
        if (outputText != null && !outputText.isEmpty()) return outputText;
        throw new RuntimeException("Cannot extract output text from LLM response");
    }

    private String stripMarkdownCodeBlock(String content) {
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            content = content.substring(start, end + 1);
        }
        return content.trim();
    }
}
