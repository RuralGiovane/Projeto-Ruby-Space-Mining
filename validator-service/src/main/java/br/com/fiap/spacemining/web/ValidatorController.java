package br.com.fiap.spacemining.web;

import br.com.fiap.spacemining.services.ValidatorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ValidatorController {
    private final ValidatorService validator;

    public ValidatorController(ValidatorService validator) {
        this.validator = validator;
    }

    @PostMapping("/command")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void validate(@RequestBody CommandRequest request) {
        validator.validateAndPublish(request.command());
    }
}
