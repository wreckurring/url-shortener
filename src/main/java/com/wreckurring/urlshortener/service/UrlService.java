package com.wreckurring.urlshortener.service;

import com.wreckurring.urlshortener.model.UrlMapping;
import com.wreckurring.urlshortener.repository.UrlRepository;
import com.wreckurring.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    @Transactional // Ensures the two-step save is treated as one task
    public String shortenUrl(String originalUrl) {
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);

        // 1. Save to get the ID from PostgreSQL
        UrlMapping saved = urlRepository.save(mapping);

        // 2. Generate the code using that ID
        String shortCode = Base62Encoder.encode(saved.getId());
        saved.setShortCode(shortCode);

        // 3. Update the record with the new code
        urlRepository.save(saved);
        return shortCode;
    }

    public String getOriginalUrl(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(UrlMapping::getOriginalUrl)
                .orElseThrow(() -> new RuntimeException("URL not found for: " + shortCode));
    }
}