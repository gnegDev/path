package com.gnegdev.path.document.service;

import com.gnegdev.path.auth.entity.User;
import com.gnegdev.path.auth.repository.UserRepository;
import com.gnegdev.path.document.dto.DocumentResponse;
import com.gnegdev.path.document.entity.PatientDocument;
import com.gnegdev.path.document.repository.PatientDocumentRepository;
import com.gnegdev.path.extraction.dto.ExtractedDataDto;
import com.gnegdev.path.extraction.entity.*;
import com.gnegdev.path.extraction.repository.PatientDataRepository;
import com.gnegdev.path.extraction.service.LlmExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final UserRepository userRepository;
    private final PatientDocumentRepository documentRepository;
    private final PatientDataRepository patientDataRepository;
    private final MinioStorageService minioStorage;
    private final TextExtractionService textExtraction;
    private final LlmExtractionService llmExtraction;

    @Transactional
    public DocumentResponse upload(String username, MultipartFile medicalHistory, MultipartFile treatmentPlan) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Upload medical history to MinIO
        String historyKey = buildObjectKey(username, "medical-history", medicalHistory.getOriginalFilename());
        try {
            minioStorage.uploadFile(historyKey, medicalHistory.getInputStream(),
                    medicalHistory.getSize(), medicalHistory.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read medical history file", e);
        }

        // Upload treatment plan to MinIO (optional)
        String planKey = null;
        if (treatmentPlan != null && !treatmentPlan.isEmpty()) {
            planKey = buildObjectKey(username, "treatment-plan", treatmentPlan.getOriginalFilename());
            try {
                minioStorage.uploadFile(planKey, treatmentPlan.getInputStream(),
                        treatmentPlan.getSize(), treatmentPlan.getContentType());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read treatment plan file", e);
            }
        }

        // Create document record with PROCESSING status
        PatientDocument doc = PatientDocument.builder()
                .user(user)
                .medicalHistoryKey(historyKey)
                .medicalHistoryFilename(medicalHistory.getOriginalFilename())
                .medicalHistoryContentType(medicalHistory.getContentType())
                .treatmentPlanKey(planKey)
                .treatmentPlanFilename(treatmentPlan != null && !treatmentPlan.isEmpty()
                        ? treatmentPlan.getOriginalFilename() : null)
                .treatmentPlanContentType(treatmentPlan != null && !treatmentPlan.isEmpty()
                        ? treatmentPlan.getContentType() : null)
                .status(PatientDocument.ProcessingStatus.PROCESSING)
                .build();
        doc = documentRepository.save(doc);

        // Extract text from medical history and call LLM
        try {
            String text = textExtraction.extractText(medicalHistory);
            ExtractedDataDto dto = llmExtraction.extract(text);
            PatientData patientData = buildPatientData(doc, dto);
            patientDataRepository.save(patientData);
            doc.setExtractedData(patientData);
            doc.setStatus(PatientDocument.ProcessingStatus.COMPLETED);
            log.info("Document {} processed successfully", doc.getId());
        } catch (Exception e) {
            log.error("Failed to process document {}: {}", doc.getId(), e.getMessage(), e);
            doc.setStatus(PatientDocument.ProcessingStatus.FAILED);
            doc.setErrorMessage(e.getMessage());
        }

        doc = documentRepository.save(doc);
        return DocumentResponse.from(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listDocuments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return documentRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        PatientDocument doc = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));
        return DocumentResponse.from(doc);
    }

    private String buildObjectKey(String username, String type, String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return username + "/" + type + "/" + UUID.randomUUID() + ext;
    }

    private PatientData buildPatientData(PatientDocument doc, ExtractedDataDto dto) {
        PatientData data = PatientData.builder()
                .document(doc)
                .fioInitials(dto.getFioInitials())
                .dateOfBirth(dto.getDateOfBirth())
                .diagnosisPrimary(dto.getDiagnosisPrimary())
                .stage(dto.getStage())
                .subtype(dto.getSubtype())
                .build();

        if (dto.getTreatmentHistory() != null) {
            dto.getTreatmentHistory().forEach(t -> {
                TreatmentHistoryEntry entry = TreatmentHistoryEntry.builder()
                        .treatmentType(t.getTreatmentType())
                        .description(t.getDescription())
                        .startDate(t.getStartDate())
                        .endDate(t.getEndDate())
                        .outcomeDynamic(t.getOutcomeDynamic())
                        .outcomeDate(t.getOutcomeDate())
                        .details(t.getDetails())
                        .build();
                entry.setPatientData(data);
                data.getTreatmentHistory().add(entry);
            });
        }

        if (dto.getBiopsyResults() != null) {
            dto.getBiopsyResults().forEach(b -> {
                BiopsyResultEntry entry = BiopsyResultEntry.builder()
                        .date(b.getDate())
                        .type(b.getType())
                        .resultSummary(b.getResultSummary())
                        .build();
                entry.setPatientData(data);
                data.getBiopsyResults().add(entry);
            });
        }

        if (dto.getConsultations() != null) {
            dto.getConsultations().forEach(c -> {
                ConsultationEntry entry = ConsultationEntry.builder()
                        .date(c.getDate())
                        .recommendation(c.getRecommendation())
                        .build();
                entry.setPatientData(data);
                data.getConsultations().add(entry);
            });
        }

        if (dto.getImagingResults() != null) {
            dto.getImagingResults().forEach(i -> {
                ImagingResultEntry entry = ImagingResultEntry.builder()
                        .date(i.getDate())
                        .type(i.getType())
                        .findings(i.getFindings())
                        .build();
                entry.setPatientData(data);
                data.getImagingResults().add(entry);
            });
        }

        return data;
    }
}
