package com.gnegdev.path.extraction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "treatment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "patientData")
public class TreatmentHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_data_id", nullable = false)
    @JsonIgnore
    private PatientData patientData;

    private String treatmentType;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String startDate;
    private String endDate;
    private String outcomeDynamic;
    private String outcomeDate;

    @Column(columnDefinition = "TEXT")
    private String details;
}
