package com.example.IntegrationSystem.controller;


import com.example.IntegrationSystem.service.MoexApiClient;
import com.example.IntegrationSystem.service.ParserService;
import com.example.IntegrationSystem.service.QuoteFileService;
import com.example.IntegrationSystem.model.Quote;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/quotes")
public class MoexController {

    private final MoexApiClient moexApiClient;
    private final ParserService parserService;
    private final QuoteFileService quoteFileService;

    public MoexController(MoexApiClient moexApiClient, ParserService parserService, QuoteFileService quoteFileService) {
        this.moexApiClient = moexApiClient;
        this.parserService = parserService;
        this.quoteFileService = quoteFileService;
    }

    @GetMapping
    public String fetchData(String URL) throws IOException, InterruptedException {
        String data = moexApiClient.getJSON(URL);
        return data;
    }


    @GetMapping("/save")
    public ResponseEntity<String> saveQuotesForMonth(@RequestParam int year, @RequestParam int month) {
        YearMonth ym = YearMonth.of(year, month);
        List<String> secids = List.of("SBER", "YNDX", "GAZP");
        List<Quote> quotes = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            for (String secid : secids) {
                String url = String.format("https://iss.moex.com/iss/securities/%s/aggregates.json?date=%s", secid, date);
                try {
                    String json = moexApiClient.getJSON(url);
                    JsonNode rootNode = mapper.readTree(json);
                    Quote quote = parserService.parse(rootNode);
                    if (quote != null) {
                        quotes.add(quote);
                    }
                } catch (Exception e) {
                }
            }
            try {
                Thread.sleep(5000); // 5 сек между запросами
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            quoteFileService.saveQuotesToFile(quotes, year, month);
            String fileName = String.format("quotes_%d-%02d.txt", year, month);
            return ResponseEntity.ok("Quotes saved to src/main/resources/" + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error saving file: " + e.getMessage());
        }
    }


//    @PostConstruct
//    public void init() throws IOException, InterruptedException {
//        this.saveQuotesForMonth(2023, 10);
//    }

}

