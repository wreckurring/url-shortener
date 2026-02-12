package com.wreckurring.urlshortener.service;

import com.wreckurring.urlshortener.model.Url;
import com.wreckurring.urlshortener.model.UrlClick;
import com.wreckurring.urlshortener.repository.UrlClickRepository;
import com.wreckurring.urlshortener.repository.UrlRepository;
import com.wreckurring.urlshortener.util.ShortCodeGenerator;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;
    
    @Autowired
    private UrlClickRepository urlClickRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "url:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final int MAX_COLLISION_ATTEMPTS = 5;

    @Transactional
    public String shortenUrl(String originalUrl) {
        // Check if URL already exists
        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get().getShortCode();
        }

        // Generate unique short code with collision handling
        String shortCode = generateUniqueShortCode(originalUrl);
        
        // Save to database
        Url url = new Url(originalUrl, shortCode);
        urlRepository.save(url);

        // Cache in Redis
        redisTemplate.opsForValue().set(
            REDIS_KEY_PREFIX + shortCode, 
            originalUrl, 
            CACHE_TTL
        );

        return shortCode;
    }

    @Transactional
    public String getOriginalUrl(String shortCode, String ipAddress, String userAgent, String referer) {
        // Try Redis cache first
        String cachedUrl = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + shortCode);
        
        if (cachedUrl != null) {
            // Still need to track analytics even if cached
            trackClick(shortCode, ipAddress, userAgent, referer);
            return cachedUrl;
        }

        // Fallback to database
        Optional<Url> urlOptional = urlRepository.findByShortCode(shortCode);
        
        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();
            
            // Track the click
            trackClick(shortCode, ipAddress, userAgent, referer);
            
            // Update cache
            redisTemplate.opsForValue().set(
                REDIS_KEY_PREFIX + shortCode, 
                url.getOriginalUrl(), 
                CACHE_TTL
            );
            
            return url.getOriginalUrl();
        }

        return null;
    }

    private String generateUniqueShortCode(String originalUrl) {
        for (int attempt = 0; attempt < MAX_COLLISION_ATTEMPTS; attempt++) {
            String shortCode = ShortCodeGenerator.generateFromUrl(originalUrl + attempt);
            
            if (!urlRepository.existsByShortCode(shortCode)) {
                return shortCode;
            }
        }
        
        // If all attempts fail, use pure random
        String randomCode;
        do {
            randomCode = ShortCodeGenerator.generateRandom();
        } while (urlRepository.existsByShortCode(randomCode));
        
        return randomCode;
    }

    private void trackClick(String shortCode, String ipAddress, String userAgent, String referer) {
        Optional<Url> urlOptional = urlRepository.findByShortCode(shortCode);
        
        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();
            
            // Increment click count
            url.incrementClickCount();
            urlRepository.save(url);
            
            // Parse user agent
            UserAgent ua = UserAgent.parseUserAgentString(userAgent);
            
            // Create click record
            UrlClick click = new UrlClick(url, ipAddress, userAgent, referer);
            click.setDeviceType(ua.getOperatingSystem().getDeviceType().getName());
            click.setBrowser(ua.getBrowser().getName());
            click.setCountry(getCountryFromIp(ipAddress)); // Will implement this
            
            urlClickRepository.save(click);
        }
    }

    private String getCountryFromIp(String ipAddress) {
        if (ipAddress == null || ipAddress.equals("0:0:0:0:0:0:0:1") || ipAddress.equals("127.0.0.1")) {
            return "Local";
        }
        return "Unknown";
    }
}