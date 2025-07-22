package com.example.IntegrationSystem.controller;

import com.example.IntegrationSystem.service.GigaChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    @Autowired
    private GigaChatService gigaChatService;

    @GetMapping("/recommendation")
    public ResponseEntity<String> getRecommendation(
            @RequestParam int year,
            @RequestParam int monthStart,
            @RequestParam int monthEnd
    ) throws Exception {
        List<String> allQuotes = new ArrayList<>();
        for (int m = monthStart; m <= monthEnd; m++) {
            String fileName = String.format("src/main/resources/quotes_%d-%02d.txt", year, m);
            if (Files.exists(Paths.get(fileName))) {
                List<String> lines = Files.readAllLines(Paths.get(fileName));
                allQuotes.addAll(lines.subList(1, lines.size()));
            }
        }
        if (allQuotes.isEmpty()) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML)
                .body("<html><body><h2>Нет данных за указанный период.</h2></body></html>");
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("Проанализируй котировки акций (название, объем торгов в акциях, объем торгов в цене, дата) за период с ")
                .append(monthStart).append(" по ").append(monthEnd).append(" месяц ").append(year)
                .append(" и дай рекомендации на следующий месяц после периода.\n");
        for (String q : allQuotes) prompt.append(q).append("\n");
        if (monthStart == 1 && monthEnd == 12) {
            prompt.append("Сделай прогноз на следующий год.");
        }
        String accessToken = gigaChatService.getAccessToken();
        String llmResponse = gigaChatService.getAnalyticsRecommendation(accessToken, prompt.toString());

        String normalized = llmResponse.replace("\\n", "\n");
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(normalized);
        String htmlBody = renderer.render(document);

        String html = "<html><head><meta charset='UTF-8'><title>Рекомендации LLM</title>"
                + "<style>body{font-family:sans-serif;margin:2em;} .llm-box{background:#f8f8ff;padding:1.5em;border-radius:10px;box-shadow:0 2px 8px #eee;} h2{color:#2a4d7a;}</style>"
                + "</head><body>"
                + "<h2>Рекомендации по котировкам</h2>"
                + "<div class='llm-box'>" + htmlBody + "</div>"
                + "</body></html>";
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
} 