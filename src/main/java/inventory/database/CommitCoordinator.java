package inventory.database;

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
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 후보 검증·불변 복사·전체 물리 목록 준비 → writer.writePhysical
     * → close 성공 후 db.publish. 실패 시 게시 금지; 논리 파일 쓰기 금지.
     * @param candidate 논리상품 값 동일, 낱개 추가 또는 FIFO sold 상태 변경
     * @throws inventory.exception.SaveFailureException 물리 파일 저장 실패
     */
    public void commitPhysical(InventorySnapshot candidate) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
