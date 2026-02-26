package com.gnegdev.path.web;

import com.gnegdev.path.analysis.dto.AnalysisResponse;
import com.gnegdev.path.analysis.service.AnalysisService;
import com.gnegdev.path.auth.dto.RegisterRequest;
import com.gnegdev.path.auth.repository.UserRepository;
import com.gnegdev.path.auth.service.AuthService;
import com.gnegdev.path.document.dto.DocumentResponse;
import com.gnegdev.path.document.entity.PatientDocument;
import com.gnegdev.path.document.repository.PatientDocumentRepository;
import com.gnegdev.path.document.service.DocumentService;
import com.gnegdev.path.document.service.MinioStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStream;
import java.util.List;

@Controller
@RequestMapping("/web")
@RequiredArgsConstructor
public class WebController {

    private final DocumentService documentService;
    private final AnalysisService analysisService;
    private final AuthService authService;
    private final PatientDocumentRepository patientDocumentRepository;
    private final UserRepository userRepository;
    private final MinioStorageService minioStorageService;

    // ─── Landing ──────────────────────────────────────────────────────────────

    @GetMapping({"/", ""})
    public String index() {
        return "index";
    }

    // ─── Auth pages ───────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register-process")
    public String registerProcess(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(username);
            request.setEmail(email);
            request.setPassword(password);
            authService.register(request);
            return "redirect:/web/login";
        } catch (Exception e) {
            return "redirect:/web/register?error=" + encodeError(e.getMessage());
        }
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        List<DocumentResponse> documents = documentService.listDocuments(auth.getName());
        long completed = documents.stream()
                .filter(d -> "COMPLETED".equals(d.getStatus()))
                .count();
        model.addAttribute("documents", documents);
        model.addAttribute("username", auth.getName());
        model.addAttribute("total", documents.size());
        model.addAttribute("completed", completed);
        return "dashboard";
    }

    // ─── Upload ───────────────────────────────────────────────────────────────

    @PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(
            @RequestParam MultipartFile medicalHistory,
            @RequestParam(required = false) MultipartFile treatmentPlan,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            documentService.upload(auth.getName(), medicalHistory, treatmentPlan);
            redirectAttributes.addFlashAttribute("uploadSuccess", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("uploadError", e.getMessage());
        }
        return "redirect:/web/dashboard";
    }

    // ─── Patient record ───────────────────────────────────────────────────────

    @GetMapping("/patients/{id}")
    public String patientRecord(@PathVariable Long id, Model model, Authentication auth) {
        DocumentResponse doc = documentService.getDocument(id, auth.getName());
        model.addAttribute("doc", doc);
        model.addAttribute("username", auth.getName());
        return "patient-record";
    }

    // ─── AI chat ─────────────────────────────────────────────────────────────

    @GetMapping("/patients/{id}/chat")
    public String aiChat(@PathVariable Long id, Model model, Authentication auth) {
        DocumentResponse doc = documentService.getDocument(id, auth.getName());
        model.addAttribute("doc", doc);
        model.addAttribute("username", auth.getName());
        try {
            AnalysisResponse analysis = analysisService.getAnalysis(id, auth.getName());
            model.addAttribute("analysis", analysis);
        } catch (Exception e) {
            model.addAttribute("analysis", null);
        }
        return "ai-chat";
    }

    // ─── Analyze ─────────────────────────────────────────────────────────────

    @PostMapping("/patients/{id}/analyze")
    @ResponseBody
    public ResponseEntity<AnalysisResponse> analyze(@PathVariable Long id, Authentication auth) {
        try {
            AnalysisResponse response = analysisService.analyze(id, auth.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─── Downloads ────────────────────────────────────────────────────────────

    @GetMapping("/patients/{id}/download/medical-history")
    public ResponseEntity<InputStreamResource> downloadMedicalHistory(
            @PathVariable Long id, Authentication auth) {
        try {
            var user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            PatientDocument doc = patientDocumentRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Document not found"));
            InputStream stream = minioStorageService.downloadFile(doc.getMedicalHistoryKey());
            String filename = doc.getMedicalHistoryFilename() != null
                    ? doc.getMedicalHistoryFilename() : "medical-history";
            String contentType = doc.getMedicalHistoryContentType() != null
                    ? doc.getMedicalHistoryContentType() : "application/octet-stream";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(
                    ContentDisposition.attachment().filename(filename).build());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/patients/{id}/download/treatment-plan")
    public ResponseEntity<InputStreamResource> downloadTreatmentPlan(
            @PathVariable Long id, Authentication auth) {
        try {
            var user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            PatientDocument doc = patientDocumentRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Document not found"));
            if (doc.getTreatmentPlanKey() == null) {
                return ResponseEntity.notFound().build();
            }
            InputStream stream = minioStorageService.downloadFile(doc.getTreatmentPlanKey());
            String filename = doc.getTreatmentPlanFilename() != null
                    ? doc.getTreatmentPlanFilename() : "treatment-plan";
            String contentType = doc.getTreatmentPlanContentType() != null
                    ? doc.getTreatmentPlanContentType() : "application/octet-stream";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(
                    ContentDisposition.attachment().filename(filename).build());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    private String encodeError(String message) {
        if (message == null) return "unknown";
        try {
            return java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "error";
        }
    }
}
