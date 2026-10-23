package inventory.dto;

import inventory.literal.Limits;

/** 미판매 낱개의 크기 합과 남은 창고 용량. */
public record WarehouseSummary(long used) {
    public long remaining() {
        return Limits.MAX_CAPACITY - used;
    }
}
