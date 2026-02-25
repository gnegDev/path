package com.gnegdev.path.extraction.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractedDataDto {

    @JsonProperty("fio_initials")
    private String fioInitials;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("diagnosis_primary")
    private String diagnosisPrimary;

    private String stage;
    private String subtype;

    @JsonProperty("treatment_history")
    private List<TreatmentHistoryDto> treatmentHistory;

    @JsonProperty("biopsy_results")
    private List<BiopsyResultDto> biopsyResults;

    private List<ConsultationDto> consultations;

    @JsonProperty("imaging_results")
    private List<ImagingResultDto> imagingResults;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TreatmentHistoryDto {
        @JsonProperty("treatment_type")
        private String treatmentType;
        private String description;
        @JsonProperty("start_date")
        private String startDate;
        @JsonProperty("end_date")
        private String endDate;
        @JsonProperty("outcome_dynamic")
        private String outcomeDynamic;
        @JsonProperty("outcome_date")
        private String outcomeDate;
        private String details;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BiopsyResultDto {
        private String date;
        private String type;
        @JsonProperty("result_summary")
        private String resultSummary;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConsultationDto {
        private String date;
        private String recommendation;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImagingResultDto {
        private String date;
        private String type;
        private String findings;
    }
}
