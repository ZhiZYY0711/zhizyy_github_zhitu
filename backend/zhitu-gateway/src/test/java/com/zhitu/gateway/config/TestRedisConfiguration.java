package com.zhitu.gateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import redis.embedded.RedisServer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

/**
 * Test configuration for embedded Redis server
 * 
 * Starts an embedded Redis instance for integration tests
 * to avoid dependency on external Redis server
 */
@TestConfiguration
public class TestRedisConfiguration {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        try {
            redisServer = new RedisServer(6379);
            redisServer.start();
        } catch (Exception e) {
            // Redis might already be running, ignore
            System.out.println("Could not start embedded Redis: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }
}
