package inventory.dto;

import inventory.validation.DomainRules;
import inventory.literal.Limits;
import java.util.HashMap;
import java.util.Objects;
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
        logicalItems = Map.copyOf(logicalItems);
        Objects.requireNonNull(physicalByCode, "physicalByCode");
        if (logicalItems.size() > Limits.MAX_PRODUCTS || !logicalItems.keySet().equals(physicalByCode.keySet())) {
            throw new IllegalArgumentException("논리·물리 맵 key 또는 상품 수 계약 위반");
        }
        Map<String, List<PhysicalItem>> physicalCopy = new HashMap<>();
        List<String> codes = logicalItems.keySet().stream().sorted().toList();
        long used = 0;
        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            LogicalItem item = logicalItems.get(code);
            if (!code.equals(item.code()) || !code.equals(DomainRules.formatLogicalCode(i + 1))) {
                throw new IllegalArgumentException("논리코드 key 또는 연속성 위반");
            }
            List<PhysicalItem> rows = List.copyOf(physicalByCode.get(code));
            if (rows.size() > Limits.MAX_SUFFIX) {
                throw new IllegalArgumentException("상품별 누적 입고 한도 위반");
            }
            boolean seenUnsold = false;
            for (int j = 0; j < rows.size(); j++) {
                PhysicalItem row = rows.get(j);
                if (!code.equals(row.logicalCode()) || row.suffix() != j + 1) {
                    throw new IllegalArgumentException("낱개 참조 또는 접미번호 연속성 위반");
                }
                if (row.sold()) {
                    if (seenUnsold) {
                        throw new IllegalArgumentException("선입선출 상태 위반");
                    }
                } else {
                    seenUnsold = true;
                    used += (long) item.size();
                }
            }
            physicalCopy.put(code, rows);
        }
        if (used > Limits.MAX_CAPACITY) {
            throw new IllegalArgumentException("창고 용량 초과");
        }
        physicalByCode = Map.copyOf(physicalCopy);
    }
}
