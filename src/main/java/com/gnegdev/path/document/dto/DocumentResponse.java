package com.gnegdev.path.document.dto;

import com.gnegdev.path.document.entity.PatientDocument;
import com.gnegdev.path.extraction.entity.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DocumentResponse {

    private Long id;
    private String status;
    private String errorMessage;
    private String medicalHistoryFilename;
    private String treatmentPlanFilename;
    private LocalDateTime createdAt;
    private PatientDataResponse extractedData;

    public static DocumentResponse from(PatientDocument doc) {
        return DocumentResponse.builder()
                .id(doc.getId())
                .status(doc.getStatus().name())
                .errorMessage(doc.getErrorMessage())
                .medicalHistoryFilename(doc.getMedicalHistoryFilename())
                .treatmentPlanFilename(doc.getTreatmentPlanFilename())
                .createdAt(doc.getCreatedAt())
                .extractedData(doc.getExtractedData() != null ? PatientDataResponse.from(doc.getExtractedData()) : null)
                .build();
    }

    @Data
    @Builder
    public static class PatientDataResponse {
        private Long id;
        private String fioInitials;
        private String dateOfBirth;
        private String diagnosisPrimary;
        private String stage;
        private String subtype;
        private List<TreatmentHistoryResponse> treatmentHistory;
        private List<BiopsyResultResponse> biopsyResults;
        private List<ConsultationResponse> consultations;
        private List<ImagingResultResponse> imagingResults;

        public static PatientDataResponse from(PatientData data) {
            return PatientDataResponse.builder()
                    .id(data.getId())
                    .fioInitials(data.getFioInitials())
                    .dateOfBirth(data.getDateOfBirth())
                    .diagnosisPrimary(data.getDiagnosisPrimary())
                    .stage(data.getStage())
                    .subtype(data.getSubtype())
                    .treatmentHistory(data.getTreatmentHistory().stream()
                            .map(TreatmentHistoryResponse::from).toList())
                    .biopsyResults(data.getBiopsyResults().stream()
                            .map(BiopsyResultResponse::from).toList())
                    .consultations(data.getConsultations().stream()
                            .map(ConsultationResponse::from).toList())
                    .imagingResults(data.getImagingResults().stream()
                            .map(ImagingResultResponse::from).toList())
                    .build();
        }
    }

    @Data
    @Builder
    public static class TreatmentHistoryResponse {
        private Long id;
        private String treatmentType;
        private String description;
        private String startDate;
        private String endDate;
        private String outcomeDynamic;
        private String outcomeDate;
        private String details;

        public static TreatmentHistoryResponse from(TreatmentHistoryEntry e) {
            return TreatmentHistoryResponse.builder()
                    .id(e.getId())
                    .treatmentType(e.getTreatmentType())
                    .description(e.getDescription())
                    .startDate(e.getStartDate())
                    .endDate(e.getEndDate())
                    .outcomeDynamic(e.getOutcomeDynamic())
                    .outcomeDate(e.getOutcomeDate())
                    .details(e.getDetails())
                    .build();
        }
    }

    @Data
    @Builder
    public static class BiopsyResultResponse {
        private Long id;
        private String date;
        private String type;
        private String resultSummary;

        public static BiopsyResultResponse from(BiopsyResultEntry e) {
            return BiopsyResultResponse.builder()
                    .id(e.getId())
                    .date(e.getDate())
                    .type(e.getType())
                    .resultSummary(e.getResultSummary())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ConsultationResponse {
        private Long id;
        private String date;
        private String recommendation;

        public static ConsultationResponse from(ConsultationEntry e) {
            return ConsultationResponse.builder()
                    .id(e.getId())
                    .date(e.getDate())
                    .recommendation(e.getRecommendation())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ImagingResultResponse {
        private Long id;
        private String date;
        private String type;
        private String findings;

        public static ImagingResultResponse from(ImagingResultEntry e) {
            return ImagingResultResponse.builder()
                    .id(e.getId())
                    .date(e.getDate())
                    .type(e.getType())
                    .findings(e.getFindings())
                    .build();
        }
    }
}
