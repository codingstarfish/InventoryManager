package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.dto.AllInventoryResult;
import inventory.dto.ItemDetail;

// 재고 조회
public class QueryService {
    private final InventoryDatabase db;

    /**
     * @param db 재고를 조회할 데이터베이스
     */
    public QueryService(InventoryDatabase db) {
        this.db = db;
    }

    /**
     * 논리코드순 상품 요약과 창고 사용량 조회. 파일은 저장하지 않음
     *
     * @return 논리코드순 전체 상품 요약과 현재 창고 사용량
     */
    public AllInventoryResult findAll() {
        return new AllInventoryResult(db.findAllOrdered().stream()
                .map(item -> db.summarize(item.code())).toList(), db.warehouse());
    }

    /**
     * 선택 상품의 요약과 접미번호순 낱개 목록 조회. 판매 기록도 포함
     *
     * @param code 조회할 등록 상품의 논리코드
     * @return 선택 상품의 재고 요약과 판매 기록을 포함한 접미번호순 낱개 목록
     */
    public ItemDetail findOne(String code) {
        return new ItemDetail(db.summarize(code), db.findPhysicalItems(code));
    }
}
