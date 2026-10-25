package inventory.dto;

import java.util.List;

/**
 * 전체 상품 요약과 창고 사용량 조회 결과
 * @param items 조회한 전체 상품의 수량 요약 목록
 * @param warehouse 조회 시점의 창고 사용량
 */
public record AllInventoryResult(List<StockSummary> items, WarehouseSummary warehouse) {
    /**
     * 조회 결과 목록을 밖에서 수정하지 못하게 복사
     * @param items 복사해 보관할 상품별 수량 요약 목록. null 및 null 원소는 허용하지 않음
     * @param warehouse 조회 시점의 창고 사용량
     */
    public AllInventoryResult {
        items = List.copyOf(items);
    }
}
