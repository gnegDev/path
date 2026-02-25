package com.gnegdev.path.analysis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "analysis_mismatches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "analysisResult")
public class MismatchEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "analysis_result_id", nullable = false)
    @JsonIgnore
    private AnalysisResult analysisResult;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String current;

    @Column(columnDefinition = "TEXT")
    private String recommended;
}
