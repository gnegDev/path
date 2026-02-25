package com.gnegdev.path.analysis.entity;

import com.gnegdev.path.document.entity.PatientDocument;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analysis_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@ToString
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "document_id", nullable = false, unique = true)
    @JsonIgnore
    @ToString.Exclude
    private PatientDocument document;

    @Column(columnDefinition = "TEXT")
    private String optimal;

    @Builder.Default
    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<MismatchEntry> mismatches = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "analysis_recommendations", joinColumns = @JoinColumn(name = "analysis_result_id"))
    @Column(name = "recommendation", columnDefinition = "TEXT")
    private List<String> recommendations = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "analysis_sources", joinColumns = @JoinColumn(name = "analysis_result_id"))
    @Column(name = "source", columnDefinition = "TEXT")
    private List<String> sources = new ArrayList<>();

    @CreatedDate
    private LocalDateTime analyzedAt;
}
