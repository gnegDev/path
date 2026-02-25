package com.gnegdev.path.analysis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnegdev.path.analysis.dto.AnalysisResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Calls the Yandex Cloud Assistant Responses API.
 *
 * Equivalent Python code:
 *   client = openai.OpenAI(api_key=..., base_url="https://rest-assistant.api.cloud.yandex.net/v1", project="...")
 *   response = client.responses.create(prompt={"id": "..."}, input="some message")
 *   print(response.output_text)
 *
 * HTTP mapping:
 *   POST /responses
 *   Authorization: Bearer {api_key}
 *   OpenAI-Project: {project}
 *   Body: {"prompt": {"id": "..."}, "input": "..."}
 */
@Service
@Slf4j
public class YandexLlmService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${yandex.cloud.api-key}")
    private String apiKey;

    @Value("${yandex.cloud.project}")
    private String project;

    @Value("${yandex.cloud.prompt-id}")
    private String promptId;

    public YandexLlmService(
            @Qualifier("yandexCloudRestClient") RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public AnalysisResultDto analyze(String medicalHistoryText, String treatmentPlanText) {
        String input = buildInput(medicalHistoryText, treatmentPlanText);
        log.info("Sending {} chars to Yandex Cloud LLM (prompt: {})", input.length(), promptId);

        Map<String, Object> body = Map.of(
                "prompt", Map.of("id", promptId),
                "input", input
        );

        String rawResponse = restClient.post()
                .uri("/responses")
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Project", project)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);

        log.debug("Yandex Cloud raw response: {}", rawResponse);
        return parseResponse(rawResponse);
    }

    private String buildInput(String medicalHistory, String treatmentPlan) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ИСТОРИЯ БОЛЕЗНИ ===\n").append(medicalHistory.strip());
        if (treatmentPlan != null && !treatmentPlan.isBlank()) {
            sb.append("\n\n=== ПЛАН ЛЕЧЕНИЯ ===\n").append(treatmentPlan.strip());
        }
        return sb.toString();
    }

    /**
     * Extracts output text from the OpenAI Responses API response, then parses it as JSON.
     *
     * Response structure:
     * {
     *   "output": [{ "type": "message", "content": [{ "type": "output_text", "text": "..." }] }]
     * }
     */
    private AnalysisResultDto parseResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String text = extractOutputText(root);
            text = stripMarkdownCodeBlock(text);
            log.info("Yandex LLM analysis completed, parsing JSON");
            return objectMapper.readValue(text, AnalysisResultDto.class);
        } catch (Exception e) {
            log.error("Failed to parse Yandex LLM response: {}", rawResponse, e);
            throw new RuntimeException("Failed to parse Yandex LLM response: " + e.getMessage(), e);
        }
    }

    private String extractOutputText(JsonNode root) {
        // Try output[0].content[0].text  (OpenAI Responses API format)
        JsonNode output = root.path("output");
        if (output.isArray() && !output.isEmpty()) {
            JsonNode content = output.get(0).path("content");
            if (content.isArray() && !content.isEmpty()) {
                String text = content.get(0).path("text").asText(null);
                if (text != null && !text.isEmpty()) return text;
            }
            // Fallback: output[0].text
            String text = output.get(0).path("text").asText(null);
            if (text != null && !text.isEmpty()) return text;
        }
        // Fallback: choices[0].message.content (Chat Completions format)
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            return choices.get(0).path("message").path("content").asText();
        }
        throw new RuntimeException("Cannot extract output text from Yandex LLM response");
    }

    private String stripMarkdownCodeBlock(String text) {
        text = text.trim();
        if (text.startsWith("```json")) text = text.substring(7);
        else if (text.startsWith("```")) text = text.substring(3);
        if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) text = text.substring(start, end + 1);
        return text.trim();
    }
}
