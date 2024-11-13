package aggregator.service;

import aggregator.model.Transaction;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AggregateService {
    Mono<List<Transaction>> getAggregate(String account);
}
