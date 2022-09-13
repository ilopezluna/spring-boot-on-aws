package io.ilopezluna.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class ApplicationTests {


    private static final DockerImageName POSTGRESQL_IMAGE = DockerImageName.parse("postgres:10.18-alpine");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRESQL_IMAGE);

    @DynamicPropertySource
    static void dependenciesProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.url",
            () -> {
                return "jdbc:postgresql://%s:%d/%s".formatted(
                    POSTGRES.getHost(),
                    POSTGRES.getMappedPort(5432),
                    POSTGRES.getDatabaseName()
                );
            }
        );
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void contextLoads() {
    }

}
