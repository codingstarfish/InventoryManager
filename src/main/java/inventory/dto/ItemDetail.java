package inventory.dto;

import inventory.entity.PhysicalItem;
import java.util.List;

/**
 * 선택 상품의 요약과 접미번호순 낱개 목록. 판매 기록도 포함
 * @param summary 선택한 상품의 정보와 누적 입고·판매 수량
 * @param physicalItems 선택한 상품의 접미번호순 낱개 목록. 판매 완료된 낱개도 포함
 */
public record ItemDetail(StockSummary summary, List<PhysicalItem> physicalItems) {
    /**
     * 조회 결과 목록을 밖에서 수정하지 못하게 복사
     * @param summary 선택한 상품의 정보와 누적 입고·판매 수량
     * @param physicalItems 복사해 보관할 낱개 목록. null 및 null 원소는 허용하지 않음
     */
    public ItemDetail {
        physicalItems = List.copyOf(physicalItems);
    }
}
