package inventory.dto;

import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import java.util.List;
import java.util.Map;

/**
 * logicalItems: 논리코드별 상품.
 * physicalByCode: 논리코드별 전체 낱개 목록.
 * 두 맵 key는 동일하며 상품 code와 일치합니다. 낱개가 없으면 빈 목록.
 * 낱개 logicalCode는 key와 일치하고 접미번호순이며 1부터 연속, 중복 없음, FIFO 상태.
 * 논리코드는 P00001부터 연속. 용량 상한 및 H=T+Q, A=999-H를 만족.
 * 각 내부 목록까지 깊은 불변 복사. 검증·복사는 저장 전에 완료해야 합니다.
 */
public record InventorySnapshot(
        Map<String, LogicalItem> logicalItems,
        Map<String, List<PhysicalItem>> physicalByCode
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다. 컬렉션은 불변 복사합니다.
     * 두 맵 key는 동일하며 상품 code와 일치합니다. 낱개가 없으면 빈 목록.
     * 낱개 logicalCode는 key와 일치하고 접미번호순이며 1부터 연속, 중복 없음, FIFO 상태.
     * 논리코드는 P00001부터 연속. 용량 상한 및 H=T+Q, A=999-H를 만족.
     * 각 내부 목록까지 깊은 불변 복사. 검증·복사는 저장 전에 완료해야 합니다.
     * @param logicalItems 논리코드별 상품
     * @param physicalByCode 논리코드별 전체 낱개 목록
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드 또는 컬렉션 원소가 null
     */
    public InventorySnapshot {
        // TODO: 위 생성자 검증과 필요한 불변 복사를 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
