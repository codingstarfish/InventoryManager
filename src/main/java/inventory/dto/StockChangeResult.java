package inventory.dto;

//입고 또는 판매한 수량과 변경 후 재고
public record StockChangeResult(int quantity, int currentAfter) {
}
