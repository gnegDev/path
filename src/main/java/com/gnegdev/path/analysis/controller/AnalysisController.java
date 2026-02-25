package com.gnegdev.path.analysis.controller;

import com.gnegdev.path.analysis.dto.AnalysisResponse;
import com.gnegdev.path.analysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents/{documentId}/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * Run LLM analysis for the given document.
     * Downloads medical history and treatment plan from MinIO,
     * sends them to the Yandex Cloud LLM, and saves the result to DB.
     *
     * POST /api/documents/{documentId}/analysis
     */
    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        AnalysisResponse response = analysisService.analyze(documentId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Get the existing analysis result for the given document.
     *
     * GET /api/documents/{documentId}/analysis
     */
    @GetMapping
    public ResponseEntity<AnalysisResponse> get(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        AnalysisResponse response = analysisService.getAnalysis(documentId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
