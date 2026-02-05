package com.wreckurring.urlshortener.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortenRequest {

    @NotBlank(message = "originalUrl is required")
    @Size(max = 2048, message = "originalUrl must be at most 2048 characters")
    @Pattern(
            regexp = "^https?://.+",
            message = "originalUrl must start with http:// or https://"
    )
    private String originalUrl;
}
