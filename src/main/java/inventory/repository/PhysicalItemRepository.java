package inventory.repository;

import inventory.database.InventoryDatabase;
import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import inventory.dto.StockSummary;
import inventory.dto.WarehouseSummary;
import java.util.List;
import java.util.Objects;

/**
 * 낱개 읽기 전용 저장소. 생성 시 상태 복사 금지, 파일 접근 없음.
 */
public class PhysicalItemRepository {
    /** 공유 Database; 메서드마다 현재 snapshot 사용 */
    private final InventoryDatabase db;

    /**
     * 의존 부품을 보관합니다. 생성 시 파일 접근·입력·저장 없음.
     * @param db 공유 Database; 메서드마다 현재 snapshot 사용
     */
    public PhysicalItemRepository(InventoryDatabase db) {
        this.db = Objects.requireNonNull(db, "db");
    }

    /**
     * 판매 완료를 포함해 해당 낱개를 조회합니다.
     * @param code 검증된 논리코드
     * @return 접미번호순 불변 목록; 낱개 없음/미등록 코드 모두 빈 목록
     */
    public List<PhysicalItem> findByLogicalCodeOrdered(String code) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 하나의 현재 snapshot을 고정하여 H=전체 수, T=판매 수, Q=미판매 수,
     * A=999-H 및 nextSuffix=H+1 (H=999이면 empty)를 계산합니다.
     * @param item 현재 db에 등록된 동일 값의 상품
     * @return H=T+Q, A=999-H를 만족하는 상품별 수량
     */
    public StockSummary summarize(LogicalItem item) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 미판매 낱개만 계산. U=sum((long)size*Q), R=1000000-U.
     * @return 사용 용량과 남은 용량; long 곱·합
     */
    public WarehouseSummary warehouse() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
