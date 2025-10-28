package com.rodemtree.yeyakitda.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

public abstract class AbstractRedisContainer {
    private static final GenericContainer<?> redisContainer = TestContainerManager.getRedisContainer();

    @DynamicPropertySource
    private static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer.getMappedPort(6379)::toString);
    }
}
