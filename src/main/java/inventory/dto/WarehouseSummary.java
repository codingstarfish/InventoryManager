package inventory.dto;

import inventory.literal.Limits;

/**
 * 창고의 현재 사용 용량을 보관하고 남은 용량을 계산
 * @param used 미판매 낱개 크기의 합계인 사용 용량, 0~1000000 사이
 */
public record WarehouseSummary(long used) {
    /**
     * 고정된 창고 최대 용량 1000000에서 현재 사용 용량 빼기
     * @return 추가 입고에 사용할 수 있는 남은 용량 단위, 0~1000000 사이
     */
    public long remaining() {
        return Limits.MAX_CAPACITY - used;
    }
}
