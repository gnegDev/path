package com.gnegdev.path.extraction.repository;

import com.gnegdev.path.extraction.entity.PatientData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientDataRepository extends JpaRepository<PatientData, Long> {
}
