package com.rodemtree.yeyakitda.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

public abstract class AbstractMongoDBContainer {
    private static final MongoDBContainer mongoDBContainer = TestContainerManager.getMongoDBContainer();

    @DynamicPropertySource
    private static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
}
