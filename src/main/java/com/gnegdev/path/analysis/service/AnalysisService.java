package com.gnegdev.path.analysis.service;

import com.gnegdev.path.analysis.dto.AnalysisResponse;
import com.gnegdev.path.analysis.dto.AnalysisResultDto;
import com.gnegdev.path.analysis.entity.AnalysisResult;
import com.gnegdev.path.analysis.entity.MismatchEntry;
import com.gnegdev.path.analysis.repository.AnalysisResultRepository;
import com.gnegdev.path.auth.entity.User;
import com.gnegdev.path.auth.repository.UserRepository;
import com.gnegdev.path.document.entity.PatientDocument;
import com.gnegdev.path.document.repository.PatientDocumentRepository;
import com.gnegdev.path.document.service.MinioStorageService;
import com.gnegdev.path.document.service.TextExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final UserRepository userRepository;
    private final PatientDocumentRepository documentRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final MinioStorageService minioStorage;
    private final TextExtractionService textExtraction;
    private final YandexLlmService yandexLlmService;

    /**
     * Run LLM analysis for a document. If analysis already exists — overwrites it.
     */
    @Transactional
    public AnalysisResponse analyze(Long documentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        PatientDocument doc = documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        if (doc.getStatus() != PatientDocument.ProcessingStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Document is not ready for analysis (status: " + doc.getStatus() + ")"
            );
        }

        // Download and extract text from MinIO
        String medicalHistoryText = downloadAndExtract(doc.getMedicalHistoryKey(), doc.getMedicalHistoryContentType());
        String treatmentPlanText = doc.getTreatmentPlanKey() != null
                ? downloadAndExtract(doc.getTreatmentPlanKey(), doc.getTreatmentPlanContentType())
                : null;

        // Call Yandex Cloud LLM
        AnalysisResultDto dto = yandexLlmService.analyze(medicalHistoryText, treatmentPlanText);

        // Remove previous analysis if exists
        analysisResultRepository.findByDocumentId(documentId)
                .ifPresent(analysisResultRepository::delete);

        // Persist new result
        AnalysisResult result = buildAnalysisResult(doc, dto);
        result = analysisResultRepository.save(result);
        log.info("Analysis saved for document {}, id={}", documentId, result.getId());

        return AnalysisResponse.from(result);
    }

    /**
     * Get existing analysis result for a document.
     */
    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysis(Long documentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Verify document ownership
        documentRepository.findByIdAndUser(documentId, user)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        AnalysisResult result = analysisResultRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("No analysis found for document: " + documentId));

        return AnalysisResponse.from(result);
    }

    // -------------------------------------------------------------------------

    private String downloadAndExtract(String minioKey, String contentType) {
        try {
            byte[] bytes = minioStorage.downloadFile(minioKey).readAllBytes();
            return textExtraction.extractText(bytes, contentType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from MinIO object: " + minioKey, e);
        }
    }

    private AnalysisResult buildAnalysisResult(PatientDocument doc, AnalysisResultDto dto) {
        AnalysisResult result = AnalysisResult.builder()
                .document(doc)
                .optimal(dto.getOptimal())
                .recommendations(dto.getRecommendations() != null ? dto.getRecommendations() : List.of())
                .sources(dto.getSources() != null ? dto.getSources() : List.of())
                .build();

        if (dto.getMismatches() != null) {
            dto.getMismatches().forEach(m -> {
                MismatchEntry entry = MismatchEntry.builder()
                        .type(m.getType())
                        .current(m.getCurrent())
                        .recommended(m.getRecommended())
                        .build();
                entry.setAnalysisResult(result);
                result.getMismatches().add(entry);
            });
        }

        return result;
    }
}
