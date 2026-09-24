package br.com.fiap.spacemining.web;

import br.com.fiap.spacemining.services.CommandService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommandController {
    private final CommandService commands;

    public CommandController(CommandService commands) {
        this.commands = commands;
    }

    @PostMapping("/command")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(@RequestBody CommandRequest request) {
        commands.send(request);
    }
}
