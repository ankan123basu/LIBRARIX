package com.librarix.ai;

import com.librarix.dto.ResourceDTO;
import com.librarix.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final SemanticSearchService semanticSearchService;
    private final LibrarianAssistantService librarianAssistantService;
    private final RecommendationService recommendationService;
    private final ExamDemandForecastingService examDemandForecastingService;

    @GetMapping("/search/semantic")
    public ResponseEntity<List<ResourceDTO>> searchSemantic(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(semanticSearchService.searchSemantic(q, limit));
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askLibrarian(@RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "");
        String answer = librarianAssistantService.askLibrarian(question);
        return ResponseEntity.ok(Map.of("question", question, "answer", answer));
    }

    @GetMapping("/recommendations/mine")
    public ResponseEntity<List<ResourceDTO>> getMyRecommendations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(userPrincipal.getId(), limit));
    }

    @GetMapping("/forecast")
    public ResponseEntity<List<ExamDemandForecastingService.DemandForecast>> getExamDemandForecast() {
        return ResponseEntity.ok(examDemandForecastingService.forecastDemandForExamPeriod());
    }
}
