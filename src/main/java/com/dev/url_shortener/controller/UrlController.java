package com.dev.url_shortener.controller;

import com.dev.url_shortener.dto.mapper.UrlMapper;
import com.dev.url_shortener.dto.request.ShortenUrlRequest;
import com.dev.url_shortener.dto.response.ShortenUrlResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UrlController {
    @Autowired
    private UrlMapper urlMapper;

    private Map<String, Map<String, Object>> urlDatabase = new HashMap<>();
   @PostMapping("/urls")
    public ResponseEntity createShortUrls(@RequestBody ShortenUrlRequest request){
        String shortCode;

        if(request.getCustomAlias() !=null && !request.getCustomAlias().isEmpty()){
            if(urlDatabase.containsKey(request.getCustomAlias())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Custom alias already exists"));
            }
            shortCode=request.getCustomAlias();
        }
        else{
            shortCode= generateRandomShortCode();
        }

        Map<String,Object> urlEntry = new HashMap<>();
        urlEntry.put("shortCode",shortCode);
        urlEntry.put("longUrl",request.getLongUrl());
        urlEntry.put("createdAt", LocalDateTime.now());
        urlEntry.put("expirationDate",request.getExpirationDate());

        urlDatabase.put(shortCode,urlEntry);

       ShortenUrlResponse shortenUrlResponse = urlMapper.toShortenUrlResponse(urlEntry);

       return ResponseEntity.status(HttpStatus.CREATED).body(shortenUrlResponse);

   }

   @GetMapping("/{shortCode}")
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode){
       Map<String,Object> urlEntry = urlDatabase.get(shortCode);

       if(urlEntry == null){
           throw new RuntimeException("Short Url not found:"+shortCode);
       }

       LocalDateTime expirationDate = (LocalDateTime) urlEntry.get("expirationDate");

       if(expirationDate != null && LocalDateTime.now().isAfter(expirationDate)){
           throw new RuntimeException("Short Url has expired:"+shortCode);
       }

       String longUrl = (String) urlEntry.get("longUrl");

       RedirectView redirectView = new RedirectView();
       redirectView.setUrl(longUrl);
       redirectView.setStatusCode(HttpStatus.FOUND);

       return redirectView;
   }

   @GetMapping("urls")
    public ResponseEntity<List> getAllUrls(){
       List<Map<String,Object>> allUrls = new ArrayList<>();

       List responses = urlDatabase.values().stream()
               .map(urlMapper::toShortenUrlResponse)
               .toList();

       return ResponseEntity.ok(responses);
   }

    private String generateRandomShortCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder shortCode = new StringBuilder();

        for (int i = 0; i < 7; i++) {
            shortCode.append(chars.charAt(random.nextInt(chars.length())));
        }

        // Make sure it doesn't already exist
        if (urlDatabase.containsKey(shortCode.toString())) {
            return generateRandomShortCode(); // Recursive call if collision
        }

        return shortCode.toString();
    }
}
