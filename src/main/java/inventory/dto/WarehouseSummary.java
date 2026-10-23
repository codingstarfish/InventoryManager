package inventory.dto;

import inventory.literal.Limits;
/**
 * used: 미판매 낱개의 크기 합.
 * remaining: 창고 남은 용량.
 * 각 값 0~1000000, used+remaining=1000000. 곱과 합은 long.
 */
public record WarehouseSummary(
        long used,
        long remaining
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다.
     * 각 값 0~1000000, used+remaining=1000000. 곱과 합은 long.
     * @param used 미판매 낱개의 크기 합
     * @param remaining 창고 남은 용량
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드가 null
     */
    public WarehouseSummary {
        if (used < 0 || used > Limits.MAX_CAPACITY || remaining < 0
                || remaining > Limits.MAX_CAPACITY || used + remaining != Limits.MAX_CAPACITY) {
            throw new IllegalArgumentException("창고 용량 관계 위반");
        }
    }
}
