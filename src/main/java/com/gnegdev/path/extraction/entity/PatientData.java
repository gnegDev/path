package com.gnegdev.path.extraction.entity;

import com.gnegdev.path.document.entity.PatientDocument;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patient_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PatientData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "document_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private PatientDocument document;

    private String fioInitials;
    private String dateOfBirth;

    @Column(columnDefinition = "TEXT")
    private String diagnosisPrimary;

    private String stage;
    private String subtype;

    @Builder.Default
    @OneToMany(mappedBy = "patientData", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<TreatmentHistoryEntry> treatmentHistory = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "patientData", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<BiopsyResultEntry> biopsyResults = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "patientData", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<ConsultationEntry> consultations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "patientData", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<ImagingResultEntry> imagingResults = new ArrayList<>();
}
