package com.wreckurring.urlshortener.service;

import com.wreckurring.urlshortener.model.UrlMapping;
import com.wreckurring.urlshortener.repository.UrlRepository;
import com.wreckurring.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final StringRedisTemplate redisTemplate; // Built-in tool to talk to Redis

    @Transactional
    public String shortenUrl(String originalUrl) {
        String normalizedUrl = normalizeUrl(originalUrl);

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(normalizedUrl);

        // 1. Save to PostgreSQL to get the unique ID
        UrlMapping saved = urlRepository.save(mapping);

        // 2. Generate the short code
        String shortCode = Base62Encoder.encode(saved.getId());
        saved.setShortCode(shortCode);

        // 3. Update PostgreSQL with the short code
        urlRepository.save(saved);

        // 4. Cache in Redis for 1 hour (Speed Layer)
        redisTemplate.opsForValue().set(shortCode, normalizedUrl, 1, TimeUnit.HOURS);

        return shortCode;
    }

    private String normalizeUrl(String originalUrl) {
        String normalized = originalUrl == null ? "" : originalUrl.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("originalUrl cannot be blank");
        }

        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();

            if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("Only http and https URLs are allowed");
            }

            return normalized;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Invalid URL format", ex);
        }
    }

    public String getOriginalUrl(String shortCode) {
        // 1. Check Redis (Fast Look-up)
        String cachedUrl = redisTemplate.opsForValue().get(shortCode);
        if (cachedUrl != null) {
            System.out.println("Cache Hit! Found in Redis.");
            return cachedUrl;
        }

        // 2. If not in Redis, check PostgreSQL (Slow Look-up)
        System.out.println("Cache Miss! Looking in PostgreSQL.");
        String originalUrl = urlRepository.findByShortCode(shortCode)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new RuntimeException("URL not found for: " + shortCode));

        // 3. Warm up the cache: Put it back in Redis so it's fast next time
        redisTemplate.opsForValue().set(shortCode, originalUrl, 1, TimeUnit.HOURS);

        return originalUrl;
    }
}
