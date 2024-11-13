package aggregator.service.impl;

import aggregator.exception.ExternalServiceException;
import aggregator.model.Transaction;
import aggregator.service.AggregateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.*;

@Service
public class AggregateServiceImpl implements AggregateService {
    private final WebClient webClient;
    private static final int MAX_RETRIES = 5;

    @Autowired
    public AggregateServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    @Cacheable(value = "aggregatesCache", key = "#account")
    public Mono<List<Transaction>> getAggregate(String account) {
        String url1 = "http://localhost:8888/transactions?account=" + account;
        String url2 = "http://localhost:8889/transactions?account=" + account;

        Flux<Transaction> transactionFlux1 = fetchTransactionsWithRetry(account, "8888");
        Flux<Transaction> transactionFlux2 = fetchTransactionsWithRetry(account, "8889");

        return Flux.merge(transactionFlux1,transactionFlux2)
                .sort(Comparator.comparing(Transaction::timestamp).reversed())
                .collectList();
    }

    public Flux<Transaction> fetchTransactionsWithRetry(String account, String serverPort) {
        String url = String.format("http://localhost:%s/transactions?account=%s", serverPort, account);

        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ExternalServiceException("Server error", response.statusCode()))
                )
                .bodyToFlux(Transaction.class)
                // https://www.baeldung.com/spring-webflux-retry
                .retryWhen(Retry.max(MAX_RETRIES)
                        .filter(throwable -> throwable instanceof ExternalServiceException));

    }

}
