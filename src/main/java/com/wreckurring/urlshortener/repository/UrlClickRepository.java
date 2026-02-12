package com.wreckurring.urlshortener.repository;

import com.wreckurring.urlshortener.model.UrlClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface UrlClickRepository extends JpaRepository<UrlClick, Long> {
    
    long countByUrlShortCode(String shortCode);
    
    List<UrlClick> findByUrlShortCodeOrderByClickedAtDesc(String shortCode);
    
    @Query("SELECT DATE(c.clickedAt) as date, COUNT(c) as count " +
           "FROM UrlClick c WHERE c.url.shortCode = :shortCode " +
           "GROUP BY DATE(c.clickedAt) ORDER BY DATE(c.clickedAt)")
    List<Object[]> getClicksByDate(@Param("shortCode") String shortCode);
    
    @Query("SELECT c.country, COUNT(c) as count " +
           "FROM UrlClick c WHERE c.url.shortCode = :shortCode " +
           "AND c.country IS NOT NULL " +
           "GROUP BY c.country ORDER BY count DESC")
    List<Object[]> getTopCountries(@Param("shortCode") String shortCode);
    
    @Query("SELECT c.deviceType, COUNT(c) as count " +
           "FROM UrlClick c WHERE c.url.shortCode = :shortCode " +
           "AND c.deviceType IS NOT NULL " +
           "GROUP BY c.deviceType")
    List<Object[]> getDeviceBreakdown(@Param("shortCode") String shortCode);
    
    @Query("SELECT c.browser, COUNT(c) as count " +
           "FROM UrlClick c WHERE c.url.shortCode = :shortCode " +
           "AND c.browser IS NOT NULL " +
           "GROUP BY c.browser ORDER BY count DESC")
    List<Object[]> getBrowserBreakdown(@Param("shortCode") String shortCode);
}