package aggregator.controller;

import aggregator.model.Transaction;
import aggregator.service.AggregateService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@Validated
public class AggregateController {
    private final AggregateService service;

    @Autowired
    public AggregateController(AggregateService service) {
        this.service = service;
    }

    @GetMapping("/aggregate")
    public Mono<List<Transaction>> getAggregate (@RequestParam @NotBlank String account) {
        return service.getAggregate(account);
    }
}
