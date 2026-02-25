package com.gnegdev.path.analysis.repository;

import com.gnegdev.path.analysis.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    Optional<AnalysisResult> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);
}
