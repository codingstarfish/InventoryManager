package inventory.dto;

/**
 * 저장을 마친 입고 또는 판매 작업의 수량 결과를 보관
 * @param quantity 이번 작업에서 실제로 입고하거나 판매한 1~100개의 수량.
 * @param currentAfter 작업을 완료한 뒤 해당 상품의 현재 재고 수량
 */
public record StockChangeResult(int quantity, int currentAfter) {
}
