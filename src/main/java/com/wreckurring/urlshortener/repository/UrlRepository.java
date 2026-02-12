package com.wreckurring.urlshortener.repository;

import com.wreckurring.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    
    Optional<Url> findByShortCode(String shortCode);
    
    Optional<Url> findByOriginalUrl(String originalUrl);
    
    boolean existsByShortCode(String shortCode);
}