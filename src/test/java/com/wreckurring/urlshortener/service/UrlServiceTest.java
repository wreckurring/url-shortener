package com.wreckurring.urlshortener.service;

import com.wreckurring.urlshortener.model.Url;
import com.wreckurring.urlshortener.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock private UrlRepository urlRepository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private BloomFilterService bloomFilterService;
    @Mock private TokenRangeService tokenRangeService;
    @Mock private AnalyticsProducer analyticsProducer;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shortenUrl_WhenUrlExists_ReturnsExistingShortCode() {
        Url existingUrl = new Url("https://example.com", "abc");
        when(urlRepository.findByOriginalUrl("https://example.com")).thenReturn(Optional.of(existingUrl));

        String shortCode = urlService.shortenUrl("https://example.com");

        assertEquals("abc", shortCode);
        verify(tokenRangeService, never()).getNextId();
    }

    @Test
    void shortenUrl_WhenNewUrl_GeneratesAndCaches() {
        when(urlRepository.findByOriginalUrl("https://newsite.com")).thenReturn(Optional.empty());
        when(tokenRangeService.getNextId()).thenReturn(1000L);

        String shortCode = urlService.shortenUrl("https://newsite.com");

        assertEquals("g8", shortCode);
        verify(urlRepository).save(any(Url.class));
        verify(bloomFilterService).add("g8");
        verify(valueOperations).set(eq("url:g8"), eq("https://newsite.com"), any());
    }

    @Test
    void getOriginalUrl_WhenBloomFilterMisses_ReturnsNullImmediately() {
        when(bloomFilterService.mightContain("invalidCode")).thenReturn(false);

        String result = urlService.getOriginalUrl("invalidCode", "127.0.0.1", "Agent", "Ref");

        assertNull(result);
        verify(redisTemplate, never()).opsForValue();
        verify(urlRepository, never()).findByShortCode(anyString());
    }

    @Test
    void getOriginalUrl_WhenRedisHits_ReturnsUrlAndFiresAnalytics() {
        when(bloomFilterService.mightContain("validCode")).thenReturn(true);
        when(valueOperations.get("url:validCode")).thenReturn("https://cached.com");

        String result = urlService.getOriginalUrl("validCode", "127.0.0.1", "Agent", "Ref");

        assertEquals("https://cached.com", result);
        verify(urlRepository, never()).findByShortCode(anyString());
        verify(analyticsProducer).sendClickEvent(any()); // Ensures async analytics fired
    }
}