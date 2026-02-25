package com.gnegdev.path.extraction.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gnegdev.path.extraction.dto.ExtractedDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LlmExtractionService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    public LlmExtractionService(
            @Qualifier("openRouterRestClient") RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    private static final String PROMPT_TEMPLATE = """
            Твоя задача — извлечь ключевую медицинскую информацию из предоставленного ниже текста медицинской истории и преобразовать его в структурированный объект JSON.
            Ты должен строго следовать определенной схеме JSON. Если поле неприменимо или если для него нет информации в тексте, используй значение `null`.

            ### JSON SCHEME:

            ```json
            {
              "fio_initials": "Initials of the patient's full name (e.g., N)",
              "date_of_birth": "Date of birth in the format DD.MM.YYYY",
              "diagnosis_primary": "Primary diagnosis (full text)",
              "stage": "Stage of the disease",
              "subtype": "Molecular subtype (e.g., Triple negative)",
              "treatment_history": [
                {
                  "treatment_type": "Type of treatment (e.g., NAC, CT, CHT, IT, SLT, surgery)",
                  "description": "Description of the regimen or procedure (e.g., 4AC + 12P, paclitaxel+carboplatin)",
                  "start_date": "Start date in the format MM.YYYY or DD.MM.YYYY",
                  "end_date": "End date in the format MM.YYYY or DD.MM.YYYY",
                  "outcome_dynamic": "Dynamics based on the results of examinations (e.g., Positive dynamics, Progression, Negative dynamics)",
                  "outcome_date": "Date of dynamics assessment (e.g., 08.2021)",
                  "details": "Additional details, if any"
                }
              ],
              "biopsy_results": [
                {
                  "date": "Date of biopsy/histology/IHC",
                  "type": "Type of research (e.g., GI, IHC, MGI, TAB)",
                  "result_summary": "Brief description of the results, including G-status, ER, PR, Her2, Ki67, mutations"
                }
              ],
              "consultations": [
                {
                  "date": "Date of the consultation",
                  "recommendation": "Recommended treatment or examination"
                }
              ],
              "imaging_results": [
                {
                  "date": "Date of examination (e.g., PET-CT, MRI GM, CT OGC)",
                  "type": "Type of imaging",
                  "findings": "Key results and dynamics"
                }
              ]
            }
            ```

            Верни ТОЛЬКО валидный JSON объект, начиная с '{' и заканчивая '}'. Не добавляй пояснения, markdown-блоки или любой другой текст.

            ### МЕДИЦИНСКАЯ ИСТОРИЯ:

            %s
            """;

    public ExtractedDataDto extract(String medicalHistoryText) {
        String prompt = PROMPT_TEMPLATE.formatted(medicalHistoryText);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        log.info("Sending medical history text ({} chars) to LLM model: {}", medicalHistoryText.length(), model);

        String rawResponse = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            content = stripMarkdownCodeBlock(content);
            log.info("LLM extraction completed, parsing JSON response");
            return objectMapper.readValue(content, ExtractedDataDto.class);
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", rawResponse, e);
            throw new RuntimeException("Failed to parse LLM extraction response: " + e.getMessage(), e);
        }
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
        // Find first '{' in case model added text before the JSON
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            content = content.substring(start, end + 1);
        }
        return content.trim();
    }
}
