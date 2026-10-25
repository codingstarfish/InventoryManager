package inventory.dto;

import inventory.literal.Limits;

// 창고 사용량: 미판매 낱개 크기의 합 (0~1000000)
public record WarehouseSummary(long used) {
    // 남은 용량 = 최대 용량 - 사용량
    public long remaining() {
        return Limits.MAX_CAPACITY - used;
    }
}
