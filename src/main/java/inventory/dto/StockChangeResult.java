package inventory.dto;

/**
 * 입고/판매 결과: 처리한 수량과 변경 후 재고
 * @param quantity 이번 입고 또는 판매에서 처리한 낱개 수(개)
 * @param currentAfter 처리 후 남은 미판매 재고 수(개)
 */
public record StockChangeResult(int quantity, int currentAfter) {
}
