package com.wreckurring.urlshortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRangeServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenRangeService tokenRangeService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getNextId_AllocatesNewRangeAndIncrements() {
        // simulate redis incrementing the counter by 1000 and returning the new max
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(1000L);

        long id1 = tokenRangeService.getNextId(); // should trigger allocation: max=1000, current=1
        long id2 = tokenRangeService.getNextId(); // uses memory: current=2

        assertEquals(1L, id1);
        assertEquals(2L, id2);
        
        // verify redis was only called once for the whole block of IDs
        verify(valueOperations, times(1)).increment(anyString(), anyLong());
    }
}