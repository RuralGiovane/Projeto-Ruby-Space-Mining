package br.com.fiap.spacemining.config;

import br.com.fiap.spacemining.services.ValidatorService.ValidationUnavailableException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class ValidatorConfiguration {
    @Bean
    Queue commandQueue(@Value("${mining.queue}") String name) {
        return QueueBuilder.durable(name).singleActiveConsumer()
                .deadLetterExchange("").deadLetterRoutingKey(name + ".failed").build();
    }

    @Bean
    Queue failedCommandQueue(@Value("${mining.queue}") String name) {
        return QueueBuilder.durable(name + ".failed").build();
    }

    @Bean
    public RetryTemplate validationRetry() {
        return RetryTemplate.builder()
                .maxAttempts(5)
                .exponentialBackoff(500, 2, 4000)
                .retryOn(ValidationUnavailableException.class)
                .build();
    }
}
