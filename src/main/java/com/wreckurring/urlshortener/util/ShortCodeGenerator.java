package com.wreckurring.urlshortener.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShortCodeGenerator {
    
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SHORT_CODE_LENGTH = 7;
    
    public static String generateRandom() {
        return RandomStringUtils.randomAlphanumeric(SHORT_CODE_LENGTH);
    }
    
    public static String generateFromUrl(String url) {
        try {
            String input = url + System.nanoTime();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            long num = 0;
            for (int i = 0; i < 8; i++) {
                num = (num << 8) | (hash[i] & 0xFF);
            }
            
            return encodeBase62(Math.abs(num), SHORT_CODE_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to random if SHA-256 fails
            return generateRandom();
        }
    }
    
    private static String encodeBase62(long num, int length) {
        StringBuilder sb = new StringBuilder();
        
        while (num > 0 && sb.length() < length) {
            sb.append(BASE62.charAt((int)(num % 62)));
            num /= 62;
        }
        
        // Pad with random characters if needed
        while (sb.length() < length) {
            sb.append(BASE62.charAt((int)(Math.random() * 62)));
        }
        
        return sb.reverse().toString();
    }
}