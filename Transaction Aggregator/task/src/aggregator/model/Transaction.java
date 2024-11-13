package aggregator.model;

public record Transaction(
        String id,
        String serverId,
        String account,
        String amount,
        String timestamp
) {
}
