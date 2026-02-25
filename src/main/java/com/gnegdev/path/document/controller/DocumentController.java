package com.gnegdev.path.document.controller;

import com.gnegdev.path.document.dto.DocumentResponse;
import com.gnegdev.path.document.service.DocumentService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload medical history and (optionally) treatment plan files.
     * The medical history is processed by LLM to extract structured data.
     *
     * @param medicalHistory Required. PDF or plain text file of the patient's medical history.
     * @param treatmentPlan  Optional. PDF or plain text file of the treatment plan.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("medicalHistory") @NotNull MultipartFile medicalHistory,
            @RequestParam(value = "treatmentPlan", required = false) MultipartFile treatmentPlan,
            Authentication authentication
    ) {
        DocumentResponse response = documentService.upload(
                authentication.getName(), medicalHistory, treatmentPlan
        );
        return ResponseEntity.ok(response);
    }

    /**
     * List all documents uploaded by the current user.
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(documentService.listDocuments(authentication.getName()));
    }

    /**
     * Get a specific document with its extracted medical data.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> get(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(documentService.getDocument(id, authentication.getName()));
    }
}
