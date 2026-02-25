package com.gnegdev.path.analysis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for parsing the JSON returned by the Yandex Cloud LLM.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResultDto {

    private String optimal;

    private List<MismatchDto> mismatches;

    // Accept both spellings the LLM might produce
    @JsonProperty("recomendations")
    private List<String> recommendations;

    private List<String> sources;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MismatchDto {
        private String type;
        private String current;
        private String recommended;
    }
}
