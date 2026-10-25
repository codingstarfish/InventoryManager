package inventory.dto;

import java.util.List;

/**
 * 전체 상품과 창고의 조회 결과를 보관
 * @param items 논리코드순 상품 요약 목록
 * @param warehouse 창고 사용 용량과 남은 용량을 제공하는 요약 객체
 */
public record AllInventoryResult(List<StockSummary> items, WarehouseSummary warehouse) {
    /**
     * 전체 조회 결과를 생성하고 상품 요약 목록을 변경할 수 없는 목록으로 복사
     * @param items 논리코드 오름차순의 상품 요약 목록
     * @param warehouse 현재 창고 사용 용량과 남은 용량을 제공하는 요약 객체
     */
    public AllInventoryResult {
        items = List.copyOf(items);
    }
}
