package com.wreckurring.urlshortener.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.wreckurring.urlshortener.model.Url;
import com.wreckurring.urlshortener.repository.UrlRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class BloomFilterService {
    @Autowired
    private UrlRepository urlRepository;
    private BloomFilter<String> bloomFilter;

    @PostConstruct
    public void init() {
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 10_000_000, 0.01);
        List<Url> allUrls = urlRepository.findAll();
        for (Url url : allUrls) {
            bloomFilter.put(url.getShortCode());
        }
    }

    public void add(String shortCode) { bloomFilter.put(shortCode); }
    public boolean mightContain(String shortCode) { return bloomFilter.mightContain(shortCode); }
}