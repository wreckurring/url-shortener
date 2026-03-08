package com.wreckurring.urlshortener.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {

    @Test
    void testEncodeBase62_Zero() {
        assertEquals("0", ShortCodeGenerator.encodeBase62(0L), "Zero should encode to the first character of the alphabet");
    }

    @Test
    void testEncodeBase62_PositiveNumbers() {
        assertEquals("10", ShortCodeGenerator.encodeBase62(62L));
        
        assertEquals("g8", ShortCodeGenerator.encodeBase62(1000L));
    }

    @Test
    void testEncodeBase62_LargeNumber() {
        String result = ShortCodeGenerator.encodeBase62(10_000_000_000L);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}