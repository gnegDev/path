package com.gnegdev.path.analysis.dto;

import com.gnegdev.path.analysis.entity.AnalysisResult;
import com.gnegdev.path.analysis.entity.MismatchEntry;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AnalysisResponse {

    private Long id;
    private Long documentId;
    private String optimal;
    private List<MismatchResponse> mismatches;
    private List<String> recommendations;
    private List<String> sources;
    private LocalDateTime analyzedAt;

    public static AnalysisResponse from(AnalysisResult result) {
        return AnalysisResponse.builder()
                .id(result.getId())
                .documentId(result.getDocument().getId())
                .optimal(result.getOptimal())
                .mismatches(result.getMismatches().stream()
                        .map(MismatchResponse::from)
                        .toList())
                .recommendations(result.getRecommendations())
                .sources(result.getSources())
                .analyzedAt(result.getAnalyzedAt())
                .build();
    }

    @Data
    @Builder
    public static class MismatchResponse {
        private Long id;
        private String type;
        private String current;
        private String recommended;

        public static MismatchResponse from(MismatchEntry e) {
            return MismatchResponse.builder()
                    .id(e.getId())
                    .type(e.getType())
                    .current(e.getCurrent())
                    .recommended(e.getRecommended())
                    .build();
        }
    }
}
