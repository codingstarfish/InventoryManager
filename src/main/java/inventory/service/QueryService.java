package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.dto.AllInventoryResult;
import inventory.dto.ItemDetail;

// 재고 조회
public class QueryService {
    private final InventoryDatabase db;

    /**
     * 재고 조회에 사용할 공유 데이터베이스를 보관
     * @param db 시작 검사와 완료된 작업의 결과가 반영된 공유 데이터베이스
     */
    public QueryService(InventoryDatabase db) {
        this.db = db;
    }

    /**
     * 모든 상품의 재고 요약과 창고 사용 현황을 조회, 파일을 다시 읽거나 저장하지 않음
     * @return 논리코드 오름차순의 상품 요약 목록과 창고 사용,남은 용량을 담은 조회 결과
     */
    public AllInventoryResult findAll() {
        return new AllInventoryResult(db.findAllOrdered().stream()
                .map(item -> db.summarize(item.code())).toList(), db.warehouse());
    }

    /**
     * 선택한 상품의 재고 요약과 판매된 낱개를 포함한 전체 낱개 목록을 조회
     * @param code 조회할 등록 상품의 P00001~P99999 형식 논리코드
     * @return 상품 요약과 접미번호 오름차순의 낱개 목록을 담은 조회 결과
     */
    public ItemDetail findOne(String code) {
        return new ItemDetail(db.summarize(code), db.findPhysicalItems(code));
    }
}
