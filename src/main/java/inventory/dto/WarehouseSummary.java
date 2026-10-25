package inventory.dto;

import inventory.literal.Limits;

/**
 * 창고 사용량: 미판매 낱개 크기의 합 (0~1000000)
 * @param used 미판매 낱개가 차지하는 창고 사용량(용량 단위)
 */
public record WarehouseSummary(long used) {
    /**
     * 남은 용량 = 최대 용량 - 사용량
     * @return 추가 입고에 사용할 수 있는 남은 용량(용량 단위)
     */
    public long remaining() {
        return Limits.MAX_CAPACITY - used;
    }
}
