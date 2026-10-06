package inventory.dto;

import inventory.literal.Limits;
import java.util.Objects;
import inventory.entity.LogicalItem;
import java.util.OptionalInt;

/**
 * item: 등록 상품.
 * received: H: 누적 입고.
 * sold: T: 누적 판매.
 * current: Q: 현재 재고.
 * issuable: A: 발급 가능 수.
 * nextSuffix: 다음 접미번호, 소진이면 empty.
 * 각 수량 0~999, H=T+Q, A=999-H. H&lt;999이면 다음 번호 H+1, H=999이면 empty.
 */
public record StockSummary(
        LogicalItem item,
        int received,
        int sold,
        int current,
        int issuable,
        OptionalInt nextSuffix
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다.
     * 각 수량 0~999, H=T+Q, A=999-H. H&lt;999이면 다음 번호 H+1, H=999이면 empty.
     * @param item 등록 상품
     * @param received H: 누적 입고
     * @param sold T: 누적 판매
     * @param current Q: 현재 재고
     * @param issuable A: 발급 가능 수
     * @param nextSuffix 다음 접미번호, 소진이면 empty
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드가 null
     */
    public StockSummary {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(nextSuffix, "nextSuffix");
        if (received < 0 || received > Limits.MAX_SUFFIX || sold < 0 || sold > received
                || current < 0 || current > received || received != sold + current
                || issuable != Limits.MAX_SUFFIX - received) {
            throw new IllegalArgumentException("상품 수량 관계 위반");
        }
        if (received == Limits.MAX_SUFFIX ? nextSuffix.isPresent()
                : nextSuffix.isEmpty() || nextSuffix.getAsInt() != received + 1) {
            throw new IllegalArgumentException("다음 접미번호 계약 위반");
        }
    }
}
