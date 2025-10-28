package com.rodemtree.yeyakitda;

import com.rodemtree.yeyakitda.config.AbstractIntegrationContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class YeyakItdaApplicationTests extends AbstractIntegrationContainer {

    @Test
    void contextLoads() {
    }

}
