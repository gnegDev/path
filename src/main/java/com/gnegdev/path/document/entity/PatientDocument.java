package com.gnegdev.path.document.entity;

import com.gnegdev.path.auth.entity.User;
import com.gnegdev.path.extraction.entity.PatientData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"user", "extractedData"})
public class PatientDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    private String medicalHistoryKey;
    private String medicalHistoryFilename;
    private String medicalHistoryContentType;

    private String treatmentPlanKey;
    private String treatmentPlanFilename;
    private String treatmentPlanContentType;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private PatientData extractedData;

    public enum ProcessingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
