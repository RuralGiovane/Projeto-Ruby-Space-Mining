package br.com.fiap.spacemining.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import br.com.fiap.spacemining.web.CommandRequest;

@Service
public class CommandService {
    private final RestTemplate restTemplate;
    private final String validatorUrl;

    public CommandService(RestTemplate restTemplate,
                          @Value("${mining.validator-url}") String validatorUrl) {
        this.restTemplate = restTemplate;
        this.validatorUrl = validatorUrl;
    }

    public void send(CommandRequest request) {
        restTemplate.postForEntity(validatorUrl, request, Void.class);
    }
}
