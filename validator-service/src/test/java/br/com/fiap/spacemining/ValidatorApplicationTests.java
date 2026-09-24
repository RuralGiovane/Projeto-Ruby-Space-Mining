package br.com.fiap.spacemining;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "eureka.client.enabled=false")
class ValidatorApplicationTests {
    @Test
    void contextLoadsIndependently() {
    }
}

