package br.com.AllTallent.e2e.support;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseApiE2ETest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("caramelstray_e2e")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected E2eFixtureLoader fixtureLoader;

    protected E2eFixtureLoader.SeedData seed;

    private final Map<String, String> tokenCache = new ConcurrentHashMap<>();

    @BeforeEach
    void setUpBaseApiE2E() {
        this.seed = fixtureLoader.resetAndSeed();
        this.tokenCache.clear();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected RequestSpecification json() {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    protected RequestSpecification auth(String token) {
        return json()
                .header("Authorization", "Bearer " + token);
    }

    protected String loginAsAdminA() {
        return tokenFor(seed.adminAEmail(), seed.defaultPassword());
    }

    protected String loginAsGestorA() {
        return tokenFor(seed.gestorAEmail(), seed.defaultPassword());
    }

    protected String loginAsColaboradorA() {
        return tokenFor(seed.colaboradorAEmail(), seed.defaultPassword());
    }

    protected String loginAsGestorB() {
        return tokenFor(seed.gestorBEmail(), seed.defaultPassword());
    }

    protected String loginAsColaboradorB() {
        return tokenFor(seed.colaboradorBEmail(), seed.defaultPassword());
    }

    protected String tokenFor(String email, String senha) {
        return tokenCache.computeIfAbsent(email + "|" + senha, key -> {
            Response response = json()
                    .body(Map.of("email", email, "password", senha))
                    .when()
                    .post("/api/auth/login");

            response.then().statusCode(200);
            return response.jsonPath().getString("token");
        });
    }
}
