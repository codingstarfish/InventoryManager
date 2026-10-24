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
     * 입고와 판매에 사용할 공유 데이터베이스를 보관합니다.
     * @param db 시작 검사를 마친 상품과 낱개 재고 및 파일 저장 기능을 제공하는 공유 데이터베이스
     */
    public StockService(InventoryDatabase db) {
        this.db = db;
    }

    /**
     * 입고할 상품을 선택하고 누적 입고 수량이 999개에 도달했는지 확인
     * @param code 등록된 상품을 가리키는 P00001~P99999 형식의 논리코드
     * @return 선택한 상품의 정보와 현재 재고, 누적 입고,판매 수량을 담은 요약 객체
     */
    public StockSummary selectInbound(String code) {
        StockSummary summary = db.summarize(code);
        if (summary.received() == Limits.MAX_SUFFIX) {
            throw new BusinessRuleException(ErrorCode.LIMIT_PHYSICAL_CODE, Map.of());
        }
        return summary;
    }

    /**
     * 판매할 상품의 재고 정보를 조회,  현재 재고가 0개인 상품도 선택함
     * @param code 등록된 상품을 가리키는 P00001~P99999 형식의 논리코드
     * @return 선택한 상품의 정보와 현재 재고, 누적 입고 및 판매 수량을 담은 요약 객체
     */
    public StockSummary selectSale(String code) {
        return db.summarize(code);
    }

    /**
     * 접미번호와 창고 용량을 확인한 뒤 요청 수량 전체를 입고하고 물리 파일에 저장
     * @param summary 입고 대상 선택 단계에서 얻은 최신 재고 요약
     * @param quantity 입력 검증을 통과한 1~100개의 입고 요청 수량
     * @return 입고한 수량과 입고 후 현재 재고 수량을 담은 결과 객체
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
     * 미판매 낱개를 접미번호가 작은 순서대로 판매 처리하고 물리 파일에 저장
     * @param summary 판매 대상 선택 단계에서 얻은 최신 재고 요약
     * @param quantity 입력 검증을 통과한 1~100개의 판매 요청 수량
     * @return 판매한 수량과 판매 후 현재 재고 수량을 담은 결과 객체
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
