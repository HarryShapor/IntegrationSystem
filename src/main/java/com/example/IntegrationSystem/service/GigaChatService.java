package com.example.IntegrationSystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GigaChatService {
    private final HttpClient client = createUnsafeHttpClient();

    @Value("${gigachat.clientId}")
    private String clientId;

    @Value("${gigachat.clientSecret}")
    private String clientSecret;

    public String getAccessToken() throws Exception {
        String auth = java.util.Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        String rqUid = UUID.randomUUID().toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ngw.devices.sberbank.ru:9443/api/v2/oauth"))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("RqUID", rqUid)
                .POST(HttpRequest.BodyPublishers.ofString("scope=GIGACHAT_API_PERS"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            String body = response.body();
            int start = body.indexOf("\"access_token\":\"") + 16;
            int end = body.indexOf("\"", start);
            return body.substring(start, end);
        } else {
            throw new RuntimeException("Failed to get token: " + response.body());
        }
    }

    public String getAnalyticsRecommendation(String accessToken, String prompt) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "GigaChat");
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        body.put("messages", List.of(message));
        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://gigachat.devices.sberbank.ru/api/v1/chat/completions"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            String bodyResp = response.body();
            int start = bodyResp.indexOf("\"content\":\"") + 12;
            int end = bodyResp.indexOf('"', start);
            return bodyResp.substring(start, end);
        } else {
            throw new RuntimeException("Failed to get analytics: " + response.body());
        }
    }

    // --- Отключение проверки SSL ---
    private static HttpClient createUnsafeHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать небезопасный HttpClient", e);
        }
    }
} 