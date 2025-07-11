package com.example.IntegrationSystem.controller;


import com.example.IntegrationSystem.service.MoexApiClient;
import com.example.IntegrationSystem.service.ParserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

@RestController
public class MoexController {

    private final MoexApiClient moexApiClient;
    private final ParserService parserService;

    public MoexController(MoexApiClient moexApiClient, ParserService parserService) {
        this.moexApiClient = moexApiClient;
        this.parserService = parserService;
    }

    @GetMapping
    public String fetchData() throws IOException, InterruptedException {
        String data = moexApiClient.getJSON("https://iss.moex.com/iss/securities/SBER/aggregates.json?date=2022-09-21");
        return data;
    }

    public String parse(String data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(data);
        parserService.parse(rootNode);
        return "";
    }


    @PostConstruct
    public void init() throws IOException, InterruptedException {
        System.out.println(this.parse(this.fetchData()));
    }

}
