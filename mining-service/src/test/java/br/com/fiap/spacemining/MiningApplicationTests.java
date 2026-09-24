package br.com.fiap.spacemining;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mining-test;DB_CLOSE_DELAY=-1",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class MiningApplicationTests {

    @org.springframework.beans.factory.annotation.Autowired
    private br.com.fiap.spacemining.services.MiningService mining;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.boot.amqp.autoconfigure.RabbitProperties rabbitProperties;

    @org.springframework.beans.factory.annotation.Autowired
    private javax.sql.DataSource dataSource;

    @Test
    void contextLoads() {
        var listener = rabbitProperties.getListener().getSimple();
        org.junit.jupiter.api.Assertions.assertEquals(1, listener.getPrefetch());
        org.junit.jupiter.api.Assertions.assertEquals(1, listener.getConcurrency());
        org.junit.jupiter.api.Assertions.assertEquals(1, listener.getMaxConcurrency());
        org.junit.jupiter.api.Assertions.assertEquals(4, listener.getRetry().getMaxRetries());
    }

    @Test
    void countsEachReceivedCommand() {
        long before = mining.counts().get("LEFT");
        mining.execute("LEFT");
        mining.execute("LEFT");
        org.junit.jupiter.api.Assertions.assertEquals(before + 2, mining.counts().get("LEFT"));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> mining.execute("INVALID"));
        org.junit.jupiter.api.Assertions.assertEquals(6, mining.counts().size());
    }

    @Test
    void initializationPreservesExistingCounts() {
        mining.execute("OPEN");
        long before = mining.counts().get("OPEN");
        new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(
                new org.springframework.core.io.ClassPathResource("schema.sql")).execute(dataSource);
        org.junit.jupiter.api.Assertions.assertEquals(before, mining.counts().get("OPEN"));
    }

}
