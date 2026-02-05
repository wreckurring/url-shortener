package com.wreckurring.urlshortener.controller;

import com.wreckurring.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping(value = "/shorten", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> shorten(@Valid @RequestBody ShortenRequest request) {
        String shortCode = urlService.shortenUrl(request.getOriginalUrl());
        return ResponseEntity.ok(Map.of("shortCode", shortCode));
    }

    // This Regex [a-zA-Z0-9]+ ensures we only catch alphanumeric short codes.
    // It will ignore "index.html" (because of the dot) and let Spring serve the static file.
    @GetMapping("/{shortCode:[a-zA-Z0-9]+}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
