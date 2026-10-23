package inventory.database;

import java.util.Comparator;
import java.util.List;
import inventory.literal.Limits;
import inventory.validation.DomainRules;
import inventory.entity.PhysicalItem;
import inventory.entity.LogicalItem;
import inventory.dto.InventorySnapshot;
import java.util.Objects;

/**
 * 저장 후 상태 게시의 단일 경로. 백업·임시 파일 복원 없음.
 */
public class CommitCoordinator {
    /** 모든 부품과 공유하는 상태 */
    private final InventoryDatabase db;
    /** 파일 저장 단일 담당 */
    private final EntityWriter writer;

    /**
     * 의존 부품을 보관합니다. 생성 시 파일 접근·입력·저장 없음.
     * @param db 모든 부품과 공유하는 상태
     * @param writer 파일 저장 단일 담당
     */
    public CommitCoordinator(InventoryDatabase db, EntityWriter writer) {
        this.db = Objects.requireNonNull(db, "db");
        this.writer = Objects.requireNonNull(writer, "writer");
    }

    /**
     * 후보 검증·깊은 불변 복사·목록 준비를 저장 전에 완료.
     * writer.writeLogical 성공(close 포함) → db.publish 순.
     * 실패 시 publish하지 않고 예외 그대로 전달; 물리 파일 쓰기 금지.
     * @param candidate 현재 상태에 논리상품 하나·빈 낱개 목록 추가, 기존 물리 레코드 값 동일
     * @throws inventory.exception.SaveFailureException 논리 파일 저장 실패
     */
    public void commitLogical(InventorySnapshot candidate) {
        Objects.requireNonNull(candidate, "candidate");
        InventorySnapshot before = db.snapshot();
        if (candidate.logicalItems().size() != before.logicalItems().size() + 1) {
            throw new IllegalArgumentException("등록 후보는 논리상품 하나만 추가해야 합니다.");
        }
        for (var entry : before.logicalItems().entrySet()) {
            if (!entry.getValue().equals(candidate.logicalItems().get(entry.getKey()))
                    || !before.physicalByCode().get(entry.getKey()).equals(candidate.physicalByCode().get(entry.getKey()))) {
                throw new IllegalArgumentException("등록 후보의 기존 상품 또는 낱개가 변경되었습니다.");
            }
        }
        String newCode = DomainRules.formatLogicalCode(candidate.logicalItems().size());
        if (!candidate.physicalByCode().get(newCode).isEmpty()) {
            throw new IllegalArgumentException("신규 상품에는 낱개가 없어야 합니다.");
        }
        List<LogicalItem> rows = candidate.logicalItems().values().stream()
                .sorted(Comparator.comparing(LogicalItem::code)).toList();
        writer.writeLogical(rows);
        db.publish(candidate);
    }

    /**
     * 후보 검증·불변 복사·전체 물리 목록 준비 → writer.writePhysical
     * → close 성공 후 db.publish. 실패 시 게시 금지; 논리 파일 쓰기 금지.
     * @param candidate 논리상품 값 동일, 낱개 추가 또는 FIFO sold 상태 변경
     * @throws inventory.exception.SaveFailureException 물리 파일 저장 실패
     */
    public void commitPhysical(InventorySnapshot candidate) {
        Objects.requireNonNull(candidate, "candidate");
        InventorySnapshot before = db.snapshot();
        if (!candidate.logicalItems().equals(before.logicalItems())) {
            throw new IllegalArgumentException("입고·판매 후보는 논리상품을 바꿀 수 없습니다.");
        }
        int changedProducts = 0;
        int added = 0;
        int sold = 0;
        for (String code : before.logicalItems().keySet()) {
            List<PhysicalItem> oldRows = before.physicalByCode().get(code);
            List<PhysicalItem> newRows = candidate.physicalByCode().get(code);
            if (!oldRows.equals(newRows)) {
                changedProducts++;
            }
            if (newRows.size() < oldRows.size()) {
                throw new IllegalArgumentException("낱개 기록을 삭제할 수 없습니다.");
            }
            for (int i = 0; i < oldRows.size(); i++) {
                PhysicalItem oldRow = oldRows.get(i);
                PhysicalItem newRow = newRows.get(i);
                if (oldRow.sold() && !newRow.sold()) {
                    throw new IllegalArgumentException("판매 상태를 되돌릴 수 없습니다.");
                }
                if (!oldRow.sold() && newRow.sold()) {
                    sold++;
                }
            }
            for (int i = oldRows.size(); i < newRows.size(); i++) {
                if (newRows.get(i).sold()) {
                    throw new IllegalArgumentException("입고 낱개는 미판매 상태여야 합니다.");
                }
                added++;
            }
        }
        if (changedProducts != 1 || (added > 0 && sold > 0) || added + sold < 1
                || added + sold > Limits.MAX_QUANTITY) {
            throw new IllegalArgumentException("한 상품의 입고 또는 판매 1~100개만 저장할 수 있습니다.");
        }
        List<PhysicalItem> rows = candidate.physicalByCode().values().stream().flatMap(List::stream)
                .sorted(Comparator.comparing(PhysicalItem::logicalCode).thenComparingInt(PhysicalItem::suffix)).toList();
        writer.writePhysical(rows);
        db.publish(candidate);
    }
}
