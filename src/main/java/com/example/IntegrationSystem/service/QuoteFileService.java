package com.example.IntegrationSystem.service;

import com.example.IntegrationSystem.model.Quote;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Service
public class QuoteFileService {
    public void saveQuotesToFile(List<Quote> quotes, int year, int month) throws IOException {
        String fileName = String.format("quotes_%d-%02d.txt", year, month);
        String filePath = Paths.get("src/main/resources", fileName).toString();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Название компании\tОбъем торгов (акции)\tОбъем торгов (цена)\tДата\n");
            for (Quote quote : quotes) {
                writer.write(String.format("%s\t%d\t%.2f\t%s\n",
                        quote.marketName,
                        quote.volume,
                        quote.value,
                        quote.tradedate
                ));
            }
        }
    }
} 