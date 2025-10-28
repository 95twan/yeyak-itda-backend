package com.rodemtree.yeyakitda.config;

import jakarta.annotation.PreDestroy;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;

public class TestContainerManager {
    private static MongoDBContainer mongoDBContainer;
    private static MySQLContainer<?> mySQLContainer;
    private static GenericContainer<?> redisContainer;

    public static MongoDBContainer getMongoDBContainer() {
        if (mongoDBContainer == null) {
            mongoDBContainer = new MongoDBContainer("mongo:4.4.9");
            mongoDBContainer.start();
        }
        return mongoDBContainer;
    }

    public static MySQLContainer<?> getMySQLContainer() {
        if (mySQLContainer == null) {
            mySQLContainer = new MySQLContainer<>("mysql:8.4.6");
            mySQLContainer.start();
        }
        return mySQLContainer;
    }

    public static GenericContainer<?> getRedisContainer() {
        if (redisContainer == null) {
            redisContainer = new GenericContainer<>("redis:8.2.1")
                    .withExposedPorts(6379);
            redisContainer.start();
        }
        return redisContainer;
    }

    @PreDestroy
    public static void cleanup() {
        if (mongoDBContainer != null) {
            mongoDBContainer.stop();
        }
        if (mySQLContainer != null) {
            mySQLContainer.stop();
        }
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }
}
