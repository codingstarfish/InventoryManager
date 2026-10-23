package inventory.dto;

import java.util.List;

/** 논리코드순 전체 상품과 창고 용량. 재고가 없는 상품도 포함합니다. */
public record AllInventoryResult(List<StockSummary> items, WarehouseSummary warehouse) {
    public AllInventoryResult {
        items = List.copyOf(items);
    }
}
