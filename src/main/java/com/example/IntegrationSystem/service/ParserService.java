package com.example.IntegrationSystem.service;

import com.example.IntegrationSystem.model.Quote;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;


@Service
public class ParserService {

    public Quote parse(JsonNode root){

        JsonNode dataNode = root.path("aggregates").get("data");

        if (dataNode.isArray() && dataNode.size() > 0) {
            JsonNode sharesArray = dataNode.get(0);

            String marketName = sharesArray.get(4).asText();
            double value = sharesArray.get(5).asDouble();
            long volume = sharesArray.get(6).asLong();
            String tradedate = sharesArray.get(3).asText();

            Quote quote = new Quote(marketName, value, volume, tradedate);
            return quote;
        }
        else {
            return null;
        }
    }

}
