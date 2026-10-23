package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.dto.InventorySnapshot;
import inventory.dto.StockChangeResult;
import inventory.dto.StockSummary;
import inventory.entity.PhysicalItem;
import inventory.exception.BusinessRuleException;
import inventory.literal.ErrorCode;
import inventory.literal.Limits;
import inventory.validation.DomainRules;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 입고와 FIFO 판매. 조건을 모두 통과한 뒤 물리 파일만 저장합니다. */
public class StockService {
    private final InventoryDatabase db;

    public StockService(InventoryDatabase db) {
        this.db = db;
    }

    /** 누적 입고가 999개이면 수량을 입력받기 전에 거절합니다. */
    public StockSummary selectInbound(String code) {
        StockSummary summary = db.summarize(code);
        if (summary.received() == Limits.MAX_SUFFIX) {
            throw new BusinessRuleException(ErrorCode.LIMIT_PHYSICAL_CODE, Map.of());
        }
        return summary;
    }

    public StockSummary selectSale(String code) {
        return db.summarize(code);
    }

    /** 수량 → 상품·코드 여유 → 창고 용량 순서로 검사하며, 부분 입고는 하지 않습니다. */
    public StockChangeResult receive(String code, int quantity) {
        DomainRules.validateQuantity(quantity);
        StockSummary summary = selectInbound(code);
        if (quantity > summary.issuable()) {
            throw new BusinessRuleException(ErrorCode.SUFFIX_SHORTAGE, Map.of("issuable", (long) summary.issuable()));
        }
        long required = (long) summary.item().size() * quantity;
        long remaining = db.warehouse().remaining();
        if (required > remaining) {
            throw new BusinessRuleException(ErrorCode.CAPACITY_SHORTAGE,
                    Map.of("required", required, "remaining", remaining));
        }
        List<PhysicalItem> rows = new ArrayList<>(db.findPhysicalItems(code));
        List<String> affected = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            PhysicalItem row = new PhysicalItem(code, summary.received() + i, false);
            rows.add(row);
            affected.add(row.physicalCode());
        }
        saveRows(code, rows);
        return new StockChangeResult(code, quantity, summary.current() + quantity, affected);
    }

    /** 미판매 낱개 중 접미번호가 작은 것부터 판매하며, 기록은 삭제하지 않습니다. */
    public StockChangeResult sell(String code, int quantity) {
        DomainRules.validateQuantity(quantity);
        StockSummary summary = selectSale(code);
        if (quantity > summary.current()) {
            throw new BusinessRuleException(ErrorCode.STOCK_SHORTAGE, Map.of("current", (long) summary.current()));
        }
        List<PhysicalItem> rows = new ArrayList<>(db.findPhysicalItems(code));
        List<String> affected = new ArrayList<>();
        for (int i = 0; i < rows.size() && affected.size() < quantity; i++) {
            PhysicalItem row = rows.get(i);
            if (!row.sold()) {
                rows.set(i, row.markSold());
                affected.add(row.physicalCode());
            }
        }
        saveRows(code, rows);
        return new StockChangeResult(code, quantity, summary.current() - quantity, affected);
    }

    private void saveRows(String code, List<PhysicalItem> rows) {
        InventorySnapshot before = db.snapshot();
        Map<String, List<PhysicalItem>> physical = new HashMap<>(before.physicalByCode());
        physical.put(code, rows);
        db.savePhysical(new InventorySnapshot(before.logicalItems(), physical));
    }
}
