package com.wreckurring.urlshortener.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortenRequest {
    private String originalUrl;
}
