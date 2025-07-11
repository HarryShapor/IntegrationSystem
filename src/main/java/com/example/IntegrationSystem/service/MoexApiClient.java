package com.example.IntegrationSystem.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class MoexApiClient {

    private HttpClient client = HttpClient.newHttpClient();

    private HttpRequest getRequest(String URL){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();
        return request;
    }

    public String getJSON(String URL) throws IOException, InterruptedException {

        HttpResponse<String> response = client.send(
                getRequest(URL),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 200){
            String json = response.body();
            return json;
        }

        return "Ошибка HTTP: " + response.statusCode();
    }

}
