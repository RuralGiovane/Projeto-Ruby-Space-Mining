package br.com.fiap.spacemining.services;

import br.com.fiap.spacemining.model.Command;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
public class ValidatorService {
    private static final Logger log = LoggerFactory.getLogger(ValidatorService.class);
    private final RabbitTemplate rabbitTemplate;
    private final RetryTemplate validationRetry;
    private final String queue;
    private final DoubleSupplier random;

    @Autowired
    public ValidatorService(RabbitTemplate rabbitTemplate, RetryTemplate validationRetry,
                            @Value("${mining.queue}") String queue) {
        this(rabbitTemplate, validationRetry, queue, () -> ThreadLocalRandom.current().nextDouble());
    }

    ValidatorService(RabbitTemplate rabbitTemplate, RetryTemplate validationRetry,
                     String queue, DoubleSupplier random) {
        this.rabbitTemplate = rabbitTemplate;
        this.validationRetry = validationRetry;
        this.queue = queue;
        this.random = random;
    }

    public void validateAndPublish(String value) {
        Command command = Command.parse(value);
        validationRetry.execute(context -> {
            if (random.getAsDouble() < 0.5) {
                log.warn("Falha simulada ao validar {} na tentativa {}", command, context.getRetryCount() + 1);
                throw new ValidationUnavailableException();
            }
            return null;
        });
        rabbitTemplate.convertAndSend("", queue, command.name());
    }

    public static class ValidationUnavailableException extends RuntimeException {
        public ValidationUnavailableException() {
            super("Validação indisponível após as tentativas; tente novamente mais tarde.");
        }
    }
}
