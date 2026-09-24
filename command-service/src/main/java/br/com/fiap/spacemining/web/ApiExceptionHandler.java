package br.com.fiap.spacemining.web;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<?> upstream(RestClientResponseException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(exception.getResponseBodyAsString());
    }

    @ExceptionHandler({ResourceAccessException.class, IllegalStateException.class})
    ResponseEntity<?> unavailable(Exception exception) {
        return ResponseEntity.status(503).body(Map.of("error", "Validator indisponível; tente novamente mais tarde."));
    }
}
