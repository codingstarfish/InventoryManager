package inventory.repository;

import java.util.Comparator;
import inventory.dto.InventorySnapshot;
import inventory.validation.DomainRules;
import inventory.database.InventoryDatabase;
import inventory.entity.LogicalItem;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/**
 * 논리상품 읽기 전용 저장소. 파일 접근·저장·ID 발급 없음.
 */
public class LogicalItemRepository {
    /** 공유 Database; 별도 목록 소유 금지 */
    private final InventoryDatabase db;

    /**
     * 의존 부품을 보관합니다. 생성 시 파일 접근·입력·저장 없음.
     * @param db 공유 Database; 별도 목록 소유 금지
     */
    public LogicalItemRepository(InventoryDatabase db) {
        this.db = Objects.requireNonNull(db, "db");
    }

    /**
     * 호출 시 현재 db.snapshot()을 한 번 읽어 검색합니다.
     * @param code 검증된 논리코드
     * @return 해당 상품, 미등록이면 Optional.empty()
     */
    public Optional<LogicalItem> findByCode(String code) {
        DomainRules.validateLogicalCode(code);
        InventorySnapshot state = db.snapshot();
        return Optional.ofNullable(state.logicalItems().get(code));
    }

    /**
     * 호출 시 현재 snapshot의 논리상품을 복사·정렬합니다.
     * @return 논리코드 오름차순의 수정 불가 목록; 없으면 빈 목록
     */
    public List<LogicalItem> findAllOrdered() {
        InventorySnapshot state = db.snapshot();
        return state.logicalItems().values().stream().sorted(Comparator.comparing(LogicalItem::code)).toList();
    }

    /**
     * 현재 snapshot의 등록 상품 수를 조회합니다.
     * @return 0~99999
     */
    public int size() {
        return db.snapshot().logicalItems().size();
    }
}
