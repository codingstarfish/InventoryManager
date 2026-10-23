package inventory.database;

import inventory.dto.InventorySnapshot;
import java.util.Objects;

/**
 * 단 하나의 상태 소유자. 모든 Repository·Service·CommitCoordinator가 같은 인스턴스 사용.
 */
public class InventoryDatabase {
    /** 전체 프로그램이 공유하는 현재 불변 상태. 가변 snapshot은 이 클래스만 소유. */
    private InventorySnapshot snapshot;

    /**
     * StartupLoader가 검증한 초기 상태를 보관합니다.
     * @param initial 검증·불변 복사를 마친 초기 상태
     */
    public InventoryDatabase(InventorySnapshot initial) {
        this.snapshot = Objects.requireNonNull(initial, "initial");
    }

    /**
     * 파일을 읽지 않고 현재 게시된 불변 상태를 반환합니다.
     * @return 현재 snapshot; 가변 컬렉션 노출 금지
     */
    public InventorySnapshot snapshot() {
        return snapshot;
    }

    /**
     * CommitCoordinator만 저장·close 성공 후 호출합니다.
     * 검증·복사는 저장 전에 완료되어야 하므로 여기서는 상태 참조만 교체합니다.
     * @param next 검증·불변 복사가 완료된 null 아닌 후보 상태
     */
    void publish(InventorySnapshot next) {
        snapshot = Objects.requireNonNull(next, "next");
    }

}
