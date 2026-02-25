package com.gnegdev.path.extraction.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "patientData")
public class ConsultationEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_data_id", nullable = false)
    @JsonIgnore
    private PatientData patientData;

    private String date;

    @Column(columnDefinition = "TEXT")
    private String recommendation;
}
