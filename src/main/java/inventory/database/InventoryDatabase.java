package inventory.database;

import inventory.dto.InventorySnapshot;
import inventory.dto.StockSummary;
import inventory.dto.WarehouseSummary;
import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import inventory.exception.BusinessRuleException;
import inventory.literal.ErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** 공유 상태의 조회와 저장. 파일 저장이 성공한 뒤에만 현재 상태를 교체합니다. */
public class InventoryDatabase {
    private InventorySnapshot snapshot;
    private final EntityWriter writer;

    public InventoryDatabase(InventorySnapshot initial, EntityWriter writer) {
        this.snapshot = initial;
        this.writer = writer;
    }

    public InventorySnapshot snapshot() {
        return snapshot;
    }

    public int size() {
        return snapshot.logicalItems().size();
    }

    public LogicalItem requireItem(String code) {
        LogicalItem item = snapshot.logicalItems().get(code);
        if (item == null) {
            throw new BusinessRuleException(ErrorCode.CODE_NOT_FOUND, Map.of());
        }
        return item;
    }

    public List<LogicalItem> findAllOrdered() {
        return snapshot.logicalItems().values().stream()
                .sorted(Comparator.comparing(LogicalItem::code)).toList();
    }

    /** 시작 검사와 입고·판매가 목록의 접미번호 순서를 유지합니다. */
    public List<PhysicalItem> findPhysicalItems(String code) {
        return snapshot.physicalByCode().getOrDefault(code, List.of());
    }

    public StockSummary summarize(String code) {
        LogicalItem item = requireItem(code);
        List<PhysicalItem> rows = findPhysicalItems(code);
        int sold = 0;
        for (PhysicalItem row : rows) {
            if (row.sold()) {
                sold++;
            }
        }
        return new StockSummary(item, rows.size(), sold);
    }

    public WarehouseSummary warehouse() {
        long used = 0;
        for (LogicalItem item : snapshot.logicalItems().values()) {
            for (PhysicalItem row : findPhysicalItems(item.code())) {
                if (!row.sold()) {
                    used += item.size();
                }
            }
        }
        return new WarehouseSummary(used);
    }

    /** 등록에서만 호출하며, 물리 파일은 저장하지 않습니다. */
    public void saveLogical(InventorySnapshot candidate) {
        writer.writeLogical(List.copyOf(candidate.logicalItems().values()));
        snapshot = candidate;
    }

    /** 입고·판매에서만 호출하며, 논리 파일은 저장하지 않습니다. */
    public void savePhysical(InventorySnapshot candidate) {
        List<PhysicalItem> rows = candidate.physicalByCode().values().stream()
                .flatMap(List::stream).toList();
        writer.writePhysical(rows);
        snapshot = candidate;
    }
}
