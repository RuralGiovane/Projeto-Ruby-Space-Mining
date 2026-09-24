package br.com.fiap.spacemining.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MiningConfiguration {
    @Bean
    Queue commandQueue(@Value("${mining.queue}") String name) {
        return QueueBuilder.durable(name).singleActiveConsumer()
                .deadLetterExchange("").deadLetterRoutingKey(name + ".failed").build();
    }

    @Bean
    Queue failedCommandQueue(@Value("${mining.queue}") String name) {
        return QueueBuilder.durable(name + ".failed").build();
    }
}
