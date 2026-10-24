package inventory.dto;

import inventory.literal.Limits;

//used: 현재 차지하고 있는 양. 
public record WarehouseSummary(long used) {
    //남은 용량을 반환
    public long remaining() {
        return Limits.MAX_CAPACITY - used;
    }
}
