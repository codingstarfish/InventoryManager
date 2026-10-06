package inventory.dto;

import java.util.Objects;
import java.util.List;

/**
 * items: 모든 상품, 논리코드순.
 * warehouse: 창고 합계.
 * 상품 없음도 정상이며 창고 합계를 반환. 재고 0·동일 이름 상품도 유지. 목록 불변 복사.
 */
public record AllInventoryResult(
        List<StockSummary> items,
        WarehouseSummary warehouse
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다. 컬렉션은 불변 복사합니다.
     * 상품 없음도 정상이며 창고 합계를 반환. 재고 0·동일 이름 상품도 유지. 목록 불변 복사.
     * @param items 모든 상품, 논리코드순
     * @param warehouse 창고 합계
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드 또는 컬렉션 원소가 null
     */
    public AllInventoryResult {
        items = List.copyOf(items);
        Objects.requireNonNull(warehouse, "warehouse");
        String previous = "";
        long used = 0;
        for (StockSummary summary : items) {
            String code = summary.item().code();
            if (code.compareTo(previous) <= 0) {
                throw new IllegalArgumentException("상품 목록 정렬 또는 중복 위반");
            }
            previous = code;
            used += (long) summary.item().size() * summary.current();
        }
        if (used != warehouse.used()) {
            throw new IllegalArgumentException("창고 합계 불일치");
        }
    }
}
