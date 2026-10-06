package inventory.dto;

import java.util.Objects;
import inventory.entity.PhysicalItem;
import java.util.List;

/**
 * summary: 선택 상품 수량 정보.
 * physicalItems: 판매 완료 포함 모든 낱개, 접미번호순.
 * 낱개의 논리코드·수량은 summary와 일치해야 합니다. 목록 불변 복사. 창고 합계는 포함하지 않습니다.
 */
public record ItemDetail(
        StockSummary summary,
        List<PhysicalItem> physicalItems
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다. 컬렉션은 불변 복사합니다.
     * 낱개의 논리코드·수량은 summary와 일치해야 합니다. 목록 불변 복사. 창고 합계는 포함하지 않습니다.
     * @param summary 선택 상품 수량 정보
     * @param physicalItems 판매 완료 포함 모든 낱개, 접미번호순
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드 또는 컬렉션 원소가 null
     */
    public ItemDetail {
        Objects.requireNonNull(summary, "summary");
        physicalItems = List.copyOf(physicalItems);
        int soldCount = 0;
        boolean seenUnsold = false;
        for (int i = 0; i < physicalItems.size(); i++) {
            PhysicalItem row = physicalItems.get(i);
            if (!row.logicalCode().equals(summary.item().code()) || row.suffix() != i + 1) {
                throw new IllegalArgumentException("단일 조회의 참조 또는 접미번호 위반");
            }
            if (row.sold()) {
                if (seenUnsold) {
                    throw new IllegalArgumentException("선입선출 위반");
                }
                soldCount++;
            } else {
                seenUnsold = true;
            }
        }
        if (physicalItems.size() != summary.received() || soldCount != summary.sold()) {
            throw new IllegalArgumentException("단일 조회 수량 불일치");
        }
    }
}
