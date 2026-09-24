package br.com.fiap.spacemining.web;

import br.com.fiap.spacemining.services.ValidatorService.ValidationUnavailableException;
import java.util.Map;
import org.springframework.amqp.AmqpException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> invalid(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler({ValidationUnavailableException.class, AmqpException.class})
    ResponseEntity<?> unavailable(Exception exception) {
        return ResponseEntity.status(503).body(Map.of("error", "Serviço indisponível; tente novamente mais tarde."));
    }
}
