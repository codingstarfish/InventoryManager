package inventory.dto;

import inventory.entity.PhysicalItem;
import java.util.List;

// 선택 상품의 요약과 접미번호순 낱개 목록. 판매 기록도 포함
public record ItemDetail(StockSummary summary, List<PhysicalItem> physicalItems) {
    // 조회 결과 목록을 밖에서 수정하지 못하게 복사
    public ItemDetail {
        physicalItems = List.copyOf(physicalItems);
    }
}
