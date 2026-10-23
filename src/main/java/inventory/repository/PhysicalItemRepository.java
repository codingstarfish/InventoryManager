package inventory.repository;

import java.util.OptionalInt;
import inventory.literal.Limits;
import inventory.dto.InventorySnapshot;
import inventory.validation.DomainRules;
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
        DomainRules.validateLogicalCode(code);
        InventorySnapshot state = db.snapshot();
        return state.physicalByCode().getOrDefault(code, List.of());
    }

    /**
     * 하나의 현재 snapshot을 고정하여 H=전체 수, T=판매 수, Q=미판매 수,
     * A=999-H 및 nextSuffix=H+1 (H=999이면 empty)를 계산합니다.
     * @param item 현재 db에 등록된 동일 값의 상품
     * @return H=T+Q, A=999-H를 만족하는 상품별 수량
     */
    public StockSummary summarize(LogicalItem item) {
        Objects.requireNonNull(item, "item");
        InventorySnapshot state = db.snapshot();
        if (!item.equals(state.logicalItems().get(item.code()))) {
            throw new IllegalArgumentException("현재 상태에 등록된 상품이 아닙니다.");
        }
        List<PhysicalItem> rows = state.physicalByCode().get(item.code());
        int received = rows.size();
        int sold = (int) rows.stream().filter(PhysicalItem::sold).count();
        return new StockSummary(item, received, sold, received - sold, Limits.MAX_SUFFIX - received,
                received == Limits.MAX_SUFFIX ? OptionalInt.empty() : OptionalInt.of(received + 1));
    }

    /**
     * 미판매 낱개만 계산. U=sum((long)size*Q), R=1000000-U.
     * @return 사용 용량과 남은 용량; long 곱·합
     */
    public WarehouseSummary warehouse() {
        InventorySnapshot state = db.snapshot();
        long used = 0;
        for (var entry : state.logicalItems().entrySet()) {
            long current = state.physicalByCode().get(entry.getKey()).stream().filter(row -> !row.sold()).count();
            used += (long) entry.getValue().size() * current;
        }
        return new WarehouseSummary(used, Limits.MAX_CAPACITY - used);
    }
}
