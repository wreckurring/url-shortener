package com.wreckurring.urlshortener.service;

import com.wreckurring.urlshortener.model.Url;
import com.wreckurring.urlshortener.model.ClickEvent;
import com.wreckurring.urlshortener.repository.UrlRepository;
import com.wreckurring.urlshortener.util.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.Optional;

@Service
public class UrlService {
    @Autowired private UrlRepository urlRepository;
    @Autowired private RedisTemplate<String, String> redisTemplate;
    @Autowired private BloomFilterService bloomFilterService;
    @Autowired private TokenRangeService tokenRangeService;
    @Autowired private AnalyticsProducer analyticsProducer;

    private static final String REDIS_KEY_PREFIX = "url:";
    private static final Duration CACHE_TTL = Duration.ofDays(7);

    @Transactional
    public String shortenUrl(String originalUrl) {
        Optional<Url> existingUrl = urlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) return existingUrl.get().getShortCode();

        // generate short code
        long uniqueId = tokenRangeService.getNextId();
        String shortCode = ShortCodeGenerator.encodeBase62(uniqueId);

        // save to database
        Url url = new Url(originalUrl, shortCode);
        urlRepository.save(url);

        // add to bloom filter & redis cache
        bloomFilterService.add(shortCode);
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + shortCode, originalUrl, CACHE_TTL);

        return shortCode;
    }

    public String getOriginalUrl(String shortCode, String ipAddress, String userAgent, String referer) {
        // bloom filter check
        if (!bloomFilterService.mightContain(shortCode)) return null;

        // try redis cache first
        String cachedUrl = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + shortCode);

        if (cachedUrl != null) {
            fireAnalyticsEvent(shortCode, ipAddress, userAgent, referer);
            return cachedUrl;
        }

        // fallback to database
        Optional<Url> urlOptional = urlRepository.findByShortCode(shortCode);
        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + shortCode, url.getOriginalUrl(), CACHE_TTL);
            fireAnalyticsEvent(shortCode, ipAddress, userAgent, referer);
            return url.getOriginalUrl();
        }

        return null;
    }

    private void fireAnalyticsEvent(String shortCode, String ipAddress, String userAgent, String referer) {
        analyticsProducer.sendClickEvent(new ClickEvent(shortCode, ipAddress, userAgent, referer));
    }
}