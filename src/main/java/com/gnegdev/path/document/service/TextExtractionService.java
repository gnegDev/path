package com.gnegdev.path.document.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class TextExtractionService {

    public String extractText(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        if ("application/pdf".equals(contentType) || filename.endsWith(".pdf")) {
            return extractFromPdf(file.getBytes());
        }

        // Plain text, TXT, or unknown — read as UTF-8
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    public String extractText(byte[] bytes, String contentType) throws IOException {
        if ("application/pdf".equals(contentType)) {
            return extractFromPdf(bytes);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String extractFromPdf(byte[] bytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF", text.length());
            return text;
        }
    }
}
