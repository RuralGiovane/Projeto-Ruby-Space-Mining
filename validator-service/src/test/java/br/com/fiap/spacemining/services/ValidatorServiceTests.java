package br.com.fiap.spacemining.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidatorServiceTests {
    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final List<Long> delays = new ArrayList<>();

    private RetryTemplate retry() {
        RetryTemplate retry = RetryTemplate.builder().maxAttempts(5)
                .retryOn(ValidatorService.ValidationUnavailableException.class).build();
        ExponentialBackOffPolicy backoff = new ExponentialBackOffPolicy();
        backoff.setInitialInterval(500);
        backoff.setMultiplier(2);
        backoff.setMaxInterval(4000);
        backoff.setSleeper(delays::add);
        retry.setBackOffPolicy(backoff);
        return retry;
    }

    @Test
    void retriesTransientFailuresThenPublishesOnce() {
        AtomicInteger attempts = new AtomicInteger();
        ValidatorService service = new ValidatorService(rabbit, retry(), "robot.commands",
                () -> attempts.incrementAndGet() < 3 ? 0.1 : 0.9);
        service.validateAndPublish("LEFT");
        assertEquals(3, attempts.get());
        assertEquals(List.of(500L, 1000L), delays);
        verify(rabbit).convertAndSend("", "robot.commands", "LEFT");
        verifyNoMoreInteractions(rabbit);
    }

    @Test
    void exhaustionDoesNotPublish() {
        ValidatorService service = new ValidatorService(rabbit, retry(), "robot.commands", () -> 0.1);
        assertThrows(ValidatorService.ValidationUnavailableException.class,
                () -> service.validateAndPublish("FRONT"));
        assertEquals(List.of(500L, 1000L, 2000L, 4000L), delays);
        verifyNoInteractions(rabbit);
    }

    @Test
    void invalidCommandsDoNotRetryOrPublish() {
        ValidatorService service = new ValidatorService(rabbit, retry(), "robot.commands",
                () -> { throw new AssertionError("Não deve simular falha para comando inválido"); });
        for (String command : new String[]{null, "", "left", "INVALID"}) {
            assertThrows(IllegalArgumentException.class, () -> service.validateAndPublish(command));
        }
        assertTrue(delays.isEmpty());
        verifyNoInteractions(rabbit);
    }

    @Test
    void acceptsAllSixCommands() {
        ValidatorService service = new ValidatorService(rabbit, retry(), "robot.commands", () -> 0.5);
        for (String command : List.of("RIGHT", "LEFT", "FRONT", "BACK", "OPEN", "CLOSE")) {
            service.validateAndPublish(command);
            verify(rabbit).convertAndSend("", "robot.commands", command);
        }
        assertTrue(delays.isEmpty());
    }
}
