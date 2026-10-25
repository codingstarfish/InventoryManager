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

// 재고 입고, 상품 판매
public class StockService {
    private final InventoryDatabase db;

    public StockService(InventoryDatabase db) {
        this.db = db;
    }

    // 누적 입고가 999개면 상품 선택 단계에서 바로 거절
    public StockSummary selectInbound(String code) {
        StockSummary summary = db.summarize(code);
        if (summary.received() == Limits.MAX_SUFFIX) {
            throw new BusinessRuleException(ErrorCode.LIMIT_PHYSICAL_CODE, Map.of());
        }
        return summary;
    }

    // 재고 0개인 상품도 선택 가능. 수량 입력 후 재고 부족 안내
    public StockSummary selectSale(String code) {
        return db.summarize(code);
    }

    // summary는 최신 조회 결과, quantity는 1~100 검증을 마친 값이어야 함
    // 발급 가능 수와 창고 용량을 확인한 뒤 입고하고 저장
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

    // summary는 최신 조회 결과, quantity는 1~100 검증을 마친 값이어야 함
    // 재고 확인 후 접미번호가 작은 미판매 낱개부터 판매하고 저장
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
