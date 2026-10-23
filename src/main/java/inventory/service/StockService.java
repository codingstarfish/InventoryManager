package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.dto.StockChangeResult;
import inventory.dto.StockSummary;
import inventory.entity.PhysicalItem;
import inventory.exception.BusinessRuleException;
import inventory.literal.ErrorCode;
import inventory.literal.Limits;
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

    /** 선택과 수량 범위 검사를 마친 요청의 접미번호 여유와 용량을 검사한 뒤 입고합니다. */
    public StockChangeResult receive(StockSummary summary, int quantity) {
        if (quantity > summary.issuable()) {
            throw new BusinessRuleException(ErrorCode.SUFFIX_SHORTAGE, Map.of("issuable", (long) summary.issuable()));
        }
        long required = (long) summary.item().size() * quantity;
        long remaining = db.warehouse().remaining();
        if (required > remaining) {
            throw new BusinessRuleException(ErrorCode.CAPACITY_SHORTAGE,
                    Map.of("required", required, "remaining", remaining));
        }
        String code = summary.item().code();
        List<PhysicalItem> rows = db.findPhysicalItems(code);
        for (int i = 1; i <= quantity; i++) {
            rows.add(new PhysicalItem(code, summary.received() + i, false));
        }
        db.savePhysical();
        return new StockChangeResult(quantity, summary.current() + quantity);
    }

    /** 선택과 수량 범위 검사를 마친 요청의 재고를 확인하고 작은 접미번호부터 판매합니다. */
    public StockChangeResult sell(StockSummary summary, int quantity) {
        if (quantity > summary.current()) {
            throw new BusinessRuleException(ErrorCode.STOCK_SHORTAGE, Map.of("current", (long) summary.current()));
        }
        List<PhysicalItem> rows = db.findPhysicalItems(summary.item().code());
        int sold = 0;
        for (int i = 0; i < rows.size() && sold < quantity; i++) {
            PhysicalItem row = rows.get(i);
            if (!row.sold()) {
                rows.set(i, row.markSold());
                sold++;
            }
        }
        db.savePhysical();
        return new StockChangeResult(quantity, summary.current() - quantity);
    }
}
