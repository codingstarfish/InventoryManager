package inventory.dto;

import java.util.List;

//논리코드순 아이템 요약과 현재 재고들의 사이즈 합
public record AllInventoryResult(List<StockSummary> items, WarehouseSummary warehouse) {
    public AllInventoryResult {
        items = List.copyOf(items);
    }
}
