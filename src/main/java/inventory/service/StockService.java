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

    /**
     * @param db 입고와 판매를 처리하고 저장할 데이터베이스
     */
    public StockService(InventoryDatabase db) {
        this.db = db;
    }

    /**
     * 누적 입고가 999개면 상품 선택 단계에서 바로 거절
     *
     * @param code 입고할 등록 상품의 논리코드
     * @return 접미번호를 추가 발급할 수 있는 선택 상품의 현재 재고 요약
     * @throws BusinessRuleException 미등록 상품이면 {@code CODE_NOT_FOUND},
     *         누적 입고가 999개여서 접미번호를 더 발급할 수 없으면 {@code LIMIT_PHYSICAL_CODE}
     */
    public StockSummary selectInbound(String code) {
        StockSummary summary = db.summarize(code);
        if (summary.received() == Limits.MAX_SUFFIX) {
            throw new BusinessRuleException(ErrorCode.LIMIT_PHYSICAL_CODE, Map.of());
        }
        return summary;
    }

    /**
     * 재고 0개인 상품도 선택 가능. 수량 입력 후 재고 부족 안내
     *
     * @param code 판매할 등록 상품의 논리코드
     * @return 선택 상품의 현재 재고 요약
     * @throws BusinessRuleException 상품이 등록되어 있지 않으면 {@code CODE_NOT_FOUND}
     */
    public StockSummary selectSale(String code) {
        return db.summarize(code);
    }

    /**
     * 발급 가능 수와 창고 용량을 확인한 뒤 입고하고 저장
     *
     * @param summary 입고할 상품의 최신 재고 요약
     * @param quantity 1~100 범위 검증을 마친 입고 수량(개)
     * @return 입고한 수량과 입고 후 현재 재고 수량
     * @throws BusinessRuleException 입고 수량이 접미번호 발급 가능 수를 초과하면 {@code SUFFIX_SHORTAGE},
     *         입고에 필요한 용량이 창고의 남은 용량을 초과하면 {@code CAPACITY_SHORTAGE}
     * @throws inventory.exception.SaveFailureException 입고한 낱개 상품을 물리 파일에 저장하지 못한 경우
     */
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

    /**
     * 재고 확인 후 접미번호가 작은 미판매 낱개부터 판매하고 저장
     *
     * @param summary 판매할 상품의 최신 재고 요약
     * @param quantity 1~100 범위 검증을 마친 판매 수량(개)
     * @return 판매한 수량과 판매 후 현재 재고 수량
     * @throws BusinessRuleException 판매 수량이 현재 재고보다 많으면 {@code STOCK_SHORTAGE}
     * @throws inventory.exception.SaveFailureException 낱개 상품의 판매 상태를 물리 파일에 저장하지 못한 경우
     */
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
