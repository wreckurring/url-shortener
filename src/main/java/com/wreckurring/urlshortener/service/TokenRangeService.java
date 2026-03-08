package com.wreckurring.urlshortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TokenRangeService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String ID_COUNTER_KEY = "url:token:allocator";
    private static final long RANGE_STEP = 1000;
    
    private final AtomicLong currentId = new AtomicLong(0);
    private volatile long maxId = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public long getNextId() {
        long id = currentId.getAndIncrement();
        if (id < maxId) {
            return id;
        }
        
        // only lock when we need to fetch a new range from redis
        lock.lock();
        try {
            id = currentId.getAndIncrement();
            if (id < maxId) {
                return id;
            }
            allocateNewRange();
            return currentId.getAndIncrement();
        } finally {
            lock.unlock();
        }
    }

    private void allocateNewRange() {
        Long nextMax = redisTemplate.opsForValue().increment(ID_COUNTER_KEY, RANGE_STEP);
        if (nextMax == null) throw new RuntimeException("Redis unavailable for Token Allocation");
        this.maxId = nextMax;
        this.currentId.set(maxId - RANGE_STEP + 1);
    }
}