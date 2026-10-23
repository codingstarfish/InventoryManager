package inventory.dto;

/** 파일 저장을 완료한 입고·판매 수량과 변경 후 현재 재고. */
public record StockChangeResult(int quantity, int currentAfter) {
}
