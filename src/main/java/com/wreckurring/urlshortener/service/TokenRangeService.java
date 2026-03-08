package com.wreckurring.urlshortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenRangeService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String ID_COUNTER_KEY = "url:token:allocator";
    private static final long RANGE_STEP = 1000;
    
    private long currentId = 0;
    private long maxId = 0;

    public synchronized long getNextId() {
        if (currentId >= maxId) {
            allocateNewRange();
        }
        return currentId++;
    }

    private void allocateNewRange() {
        Long nextMax = redisTemplate.opsForValue().increment(ID_COUNTER_KEY, RANGE_STEP);
        if (nextMax == null) throw new RuntimeException("Redis unavailable for Token Allocation");
        this.maxId = nextMax;
        this.currentId = maxId - RANGE_STEP + 1;
    }
}