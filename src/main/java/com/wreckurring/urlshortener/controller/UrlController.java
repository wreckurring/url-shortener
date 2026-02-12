package com.wreckurring.urlshortener.controller;

import com.wreckurring.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/api/v1/shorten")
    public ResponseEntity<Map<String, String>> shortenUrl(@RequestBody Map<String, String> request) {
        String originalUrl = request.get("url");
        
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL is required"));
        }

        try {
            String shortCode = urlService.shortenUrl(originalUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("shortCode", shortCode);
            response.put("shortUrl", "http://localhost:9090/" + shortCode);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to shorten URL"));
        }
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{7}}")
    public RedirectView redirectToOriginalUrl(
            @PathVariable String shortCode,
            HttpServletRequest request) {
                    
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        
        String originalUrl = urlService.getOriginalUrl(shortCode, ipAddress, userAgent, referer);
        
        if (originalUrl != null) {
            return new RedirectView(originalUrl);
        } else {
            return new RedirectView("/?error=not-found");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}