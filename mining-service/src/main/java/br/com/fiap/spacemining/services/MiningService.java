package br.com.fiap.spacemining.services;

import br.com.fiap.spacemining.model.Command;
import br.com.fiap.spacemining.model.CommandCountRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MiningService {
    private static final Logger log = LoggerFactory.getLogger(MiningService.class);
    private final CommandCountRepository commandCounts;

    public MiningService(CommandCountRepository commandCounts) {
        this.commandCounts = commandCounts;
    }

    @Transactional
    @RabbitListener(queues = "${mining.queue}")
    public void execute(String value) {
        Command command = Command.parse(value);
        var count = commandCounts.findByCommandForUpdate(command.name())
                .orElseThrow(() -> new IllegalStateException("Contador não encontrado para " + command));
        count.increment();
        log.info("Robô executando comando: {}", command);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> counts() {
        Map<String, Long> result = new LinkedHashMap<>();
        commandCounts.findAll().stream().sorted((a, b) -> a.getCommand().compareTo(b.getCommand()))
                .forEach(count -> result.put(count.getCommand(), count.getTotal()));
        return result;
    }
}
