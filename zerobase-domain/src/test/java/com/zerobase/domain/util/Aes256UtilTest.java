package com.zerobase.domain.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.*;

class Aes256UtilTest {
    private Aes256Util aes256Util;

    @Test
    void encrypt() {
        String ex = Aes256Util.encrypt("Hello world");
        assertEquals(Aes256Util.decrypt(ex), "Hello world");
    }
}