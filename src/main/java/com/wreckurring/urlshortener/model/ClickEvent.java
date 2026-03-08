package com.wreckurring.urlshortener.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClickEvent {
    private String shortCode;
    private String ipAddress;
    private String userAgent;
    private String referer;
}