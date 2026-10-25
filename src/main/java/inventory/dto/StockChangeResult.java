package inventory.dto;

// 입고/판매 결과: 처리한 수량과 변경 후 재고
public record StockChangeResult(int quantity, int currentAfter) {
}
