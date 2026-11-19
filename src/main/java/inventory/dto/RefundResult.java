package inventory.dto;

public record RefundResult(inventory.entity.PhysicalKey key, int refundAmount, int currentAfter, long resalePrice) {
}
