package inventory.dto;

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
        // TODO: 위 생성자 검증과 필요한 불변 복사를 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
