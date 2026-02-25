package com.gnegdev.path.document.repository;

import com.gnegdev.path.auth.entity.User;
import com.gnegdev.path.document.entity.PatientDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientDocumentRepository extends JpaRepository<PatientDocument, Long> {

    List<PatientDocument> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<PatientDocument> findByIdAndUser(Long id, User user);
}
