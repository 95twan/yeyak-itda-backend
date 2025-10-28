package com.rodemtree.yeyakitda.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;

public abstract class AbstractIntegrationContainer {
    private static final MySQLContainer<?> mySQLContainer = TestContainerManager.getMySQLContainer();
    private static final GenericContainer<?> redisContainer = TestContainerManager.getRedisContainer();
    private static final MongoDBContainer mongoDBContainer = TestContainerManager.getMongoDBContainer();


    @DynamicPropertySource
    private static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);

        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer.getMappedPort(6379)::toString);
    }
}
