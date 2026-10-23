package inventory.database;

import inventory.dto.StockSummary;
import inventory.dto.WarehouseSummary;
import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import inventory.exception.BusinessRuleException;
import inventory.literal.ErrorCode;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 가변 Map과 List로 현재 데이터를 관리하고, 변경한 종류의 파일만 저장합니다. */
public class InventoryDatabase {
    private final Map<String, LogicalItem> logicalItems = new HashMap<>();
    private final Map<String, List<PhysicalItem>> physicalByCode = new HashMap<>();
    private final EntityWriter writer;

    public InventoryDatabase(EntityWriter writer) {
        this.writer = writer;
    }

    /** 시작 검사 또는 등록 검증을 마친 상품과 빈 낱개 목록을 추가합니다. */
    public void addItem(LogicalItem item) {
        logicalItems.put(item.code(), item);
        physicalByCode.put(item.code(), new ArrayList<>());
    }

    public int size() {
        return logicalItems.size();
    }

    public LogicalItem requireItem(String code) {
        LogicalItem item = logicalItems.get(code);
        if (item == null) {
            throw new BusinessRuleException(ErrorCode.CODE_NOT_FOUND, Map.of());
        }
        return item;
    }

    public List<LogicalItem> findAllOrdered() {
        return logicalItems.values().stream()
                .sorted(Comparator.comparing(LogicalItem::code)).toList();
    }

    /** 등록된 상품의 가변 목록. 시작 로딩과 입고·판매가 접미번호 순서를 유지합니다. */
    public List<PhysicalItem> findPhysicalItems(String code) {
        return physicalByCode.get(code);
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
        for (LogicalItem item : logicalItems.values()) {
            for (PhysicalItem row : findPhysicalItems(item.code())) {
                if (!row.sold()) {
                    used += item.size();
                }
            }
        }
        return new WarehouseSummary(used);
    }

    /** 등록에서만 호출하며, 물리 파일은 저장하지 않습니다. */
    public void saveLogical() {
        writer.writeLogical(logicalItems.values());
    }

    /** 입고·판매에서만 호출하며, 논리 파일은 저장하지 않습니다. */
    public void savePhysical() {
        List<PhysicalItem> rows = physicalByCode.values().stream()
                .flatMap(List::stream).toList();
        writer.writePhysical(rows);
    }
}
