package br.com.fiap.spacemining.web;

import br.com.fiap.spacemining.services.MiningService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MiningController {
    private final MiningService mining;

    public MiningController(MiningService mining) {
        this.mining = mining;
    }

    @GetMapping("/commands/counts")
    public Map<String, Long> counts() {
        return mining.counts();
    }
}
