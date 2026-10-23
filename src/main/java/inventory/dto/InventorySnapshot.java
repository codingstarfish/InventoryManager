package inventory.dto;

import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 전체 상태의 불변 복사. 파일 규칙은 시작 검사에서, 변경 조건은 서비스에서 검사합니다. */
public record InventorySnapshot(
        Map<String, LogicalItem> logicalItems,
        Map<String, List<PhysicalItem>> physicalByCode
) {
    public InventorySnapshot {
        logicalItems = Map.copyOf(logicalItems);
        Map<String, List<PhysicalItem>> copy = new HashMap<>();
        physicalByCode.forEach((code, rows) -> copy.put(code, List.copyOf(rows)));
        physicalByCode = Map.copyOf(copy);
    }
}
