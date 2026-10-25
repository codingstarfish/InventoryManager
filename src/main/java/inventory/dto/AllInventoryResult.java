package inventory.dto;

import java.util.List;

// 전체 상품 요약과 창고 사용량 조회 결과
public record AllInventoryResult(List<StockSummary> items, WarehouseSummary warehouse) {
    // 조회 결과 목록을 밖에서 수정하지 못하게 복사
    public AllInventoryResult {
        items = List.copyOf(items);
    }
}
