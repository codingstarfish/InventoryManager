package inventory.dto;

import inventory.entity.PhysicalItem;
import java.util.List;

/**
 * 선택한 상품의 요약과 판매 기록을 포함한 전체 낱개 목록을 보관
 * @param summary 선택 상품의 정보와 누적 입고,판매 수량의 요약
 * @param physicalItems 접미번호순 낱개 목록
 */
public record ItemDetail(StockSummary summary, List<PhysicalItem> physicalItems) {
    /**
     * 단일 상품 조회 결과를 생성하고 낱개 목록을 변경할 수 없는 목록으로 복사함
     * @param summary 선택한 상품 정보와 누적 입고,판매 수량 및 현재 재고의 요약
     * @param physicalItems 판매된 낱개를 포함하는 접미번호 오름차순의 전체 낱개 목록
     */
    public ItemDetail {
        physicalItems = List.copyOf(physicalItems);
    }
}
