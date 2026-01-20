package com.dev.url_shortener.dto.mapper;

import com.dev.url_shortener.dto.response.ShortenUrlResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class UrlMapper {

    public ShortenUrlResponse toShortenUrlResponse(Map urlData){
        String shortCode = (String) urlData.get("shortCode");

        return ShortenUrlResponse.builder()
                .shortUrl("http://localhost:8080/"+shortCode)
                .longUrl((String) urlData.get("longUrl"))
                .createdAt((LocalDateTime) urlData.get("createdAt"))
                .expirationDate((LocalDateTime) urlData.get("expirationDate"))
                .build();
    }
}
