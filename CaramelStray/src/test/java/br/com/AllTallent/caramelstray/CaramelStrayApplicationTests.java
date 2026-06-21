package br.com.AllTallent.caramelstray;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CaramelStrayApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void shouldDelegateMainMethodToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
            br.com.AllTallent.CaramelStrayApplication.main(new String[]{"--spring.main.web-application-type=none"});
            springApplication.verify(() -> SpringApplication.run(
                    br.com.AllTallent.CaramelStrayApplication.class,
                    new String[]{"--spring.main.web-application-type=none"}));
        }
    }

}
