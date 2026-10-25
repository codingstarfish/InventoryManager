package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.dto.AllInventoryResult;
import inventory.dto.ItemDetail;

// 재고 조회
public class QueryService {
    private final InventoryDatabase db;

    public QueryService(InventoryDatabase db) {
        this.db = db;
    }

    // 논리코드순 상품 요약과 창고 사용량 조회. 파일은 저장하지 않음
    public AllInventoryResult findAll() {
        return new AllInventoryResult(db.findAllOrdered().stream()
                .map(item -> db.summarize(item.code())).toList(), db.warehouse());
    }

    // 선택 상품의 요약과 접미번호순 낱개 목록 조회. 판매 기록도 포함
    public ItemDetail findOne(String code) {
        return new ItemDetail(db.summarize(code), db.findPhysicalItems(code));
    }
}
