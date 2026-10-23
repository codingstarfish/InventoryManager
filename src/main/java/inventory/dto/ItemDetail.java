package inventory.dto;

import inventory.entity.PhysicalItem;
import java.util.List;

/** 선택 상품의 수량과 판매 기록을 포함한 접미번호순 낱개 목록. */
public record ItemDetail(StockSummary summary, List<PhysicalItem> physicalItems) {
    public ItemDetail {
        physicalItems = List.copyOf(physicalItems);
    }
}
