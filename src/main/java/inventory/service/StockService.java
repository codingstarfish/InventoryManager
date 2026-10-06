package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.database.CommitCoordinator;
import inventory.repository.LogicalItemRepository;
import inventory.repository.PhysicalItemRepository;
import inventory.dto.StockSummary;
import inventory.dto.StockChangeResult;
import inventory.exception.BusinessRuleException;
import inventory.exception.SaveFailureException;
import java.util.Objects;

/**
 * 입고·FIFO 판매. 물리 파일만 저장, 논리 파일 미변경. 화면 입출력 없음.
 */
public class StockService {
    /** 전체 공유 상태 */
    private final InventoryDatabase db;
    /** 같은 db를 사용하는 논리 저장소 */
    private final LogicalItemRepository logicalRepo;
    /** 같은 db를 사용하는 낱개 저장소 */
    private final PhysicalItemRepository physicalRepo;
    /** 같은 db를 사용하는 저장·게시 담당 */
    private final CommitCoordinator commit;

    /**
     * 의존 부품을 보관합니다. 생성 시 파일 접근·입력·저장 없음.
     * @param db 전체 공유 상태
     * @param logicalRepo 같은 db를 사용하는 논리 저장소
     * @param physicalRepo 같은 db를 사용하는 낱개 저장소
     * @param commit 같은 db를 사용하는 저장·게시 담당
     */
    public StockService(InventoryDatabase db, LogicalItemRepository logicalRepo, PhysicalItemRepository physicalRepo, CommitCoordinator commit) {
        this.db = Objects.requireNonNull(db, "db");
        this.logicalRepo = Objects.requireNonNull(logicalRepo, "logicalRepo");
        this.physicalRepo = Objects.requireNonNull(physicalRepo, "physicalRepo");
        this.commit = Objects.requireNonNull(commit, "commit");
    }

    /**
     * 상품 존재 → H&lt;999 확인. H=999는 수량 입력 전에 주 메뉴로 복귀할 전용 오류.
     * @param code 검증된 논리코드
     * @return 상품명·현재 재고 표시용 수량 정보
     * @throws BusinessRuleException CODE_NOT_FOUND 또는 LIMIT_PHYSICAL_CODE; details=Map.of()
     */
    public StockSummary selectInbound(String code) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 상품 존재를 확인합니다. Q=0이어도 수량 입력을 위해 정상 반환합니다.
     * @param code 검증된 논리코드
     * @return 선택 상품 수량 정보
     * @throws BusinessRuleException CODE_NOT_FOUND
     */
    public StockSummary selectSale(String code) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 검사 순서: 인자 유효성 → 상품 존재 → H=999 → quantity&lt;=999-H
     * → (long)size*quantity&lt;=remaining. 부족하면 전량 거절.
     * H+1~H+quantity 접미번호로 sold=false 낱개를 추가한 후보 상태 생성.
     * commitPhysical 성공 후 결과 반환. 기존 낱개·다른 상품 유지.
     * @param code 검증된 논리코드
     * @param quantity 1~100의 검증된 요청 수량
     * @return 입고 수량·입고 후 Q·새 전체 물리코드의 접미번호순 목록
     * @throws BusinessRuleException CODE_NOT_FOUND, LIMIT_PHYSICAL_CODE, SUFFIX_SHORTAGE(issuable), CAPACITY_SHORTAGE(required, remaining)
     * @throws SaveFailureException 물리 파일 저장 실패; 결과 반환/완료 출력 없음
     * @throws IllegalArgumentException 개발 호출의 유효하지 않은 인자
     */
    public StockChangeResult receive(String code, int quantity) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 인자 유효성 → 상품 존재 → quantity&lt;=Q 확인. 부족하면 전량 거절.
     * 미판매 낱개 중 접미번호가 가장 작은 quantity개를 markSold로 교체.
     * 기존 판매 기록·다른 상품 유지. commitPhysical 성공 후 반환.
     * 누적 입고 H·접미번호·발급 가능 수 A는 그대로.
     * @param code 검증된 논리코드
     * @param quantity 1~100의 검증된 요청 수량
     * @return 판매 수량·판매 후 Q·변경된 전체 물리코드의 접미번호순 목록
     * @throws BusinessRuleException CODE_NOT_FOUND 또는 STOCK_SHORTAGE(current)
     * @throws SaveFailureException 물리 파일 저장 실패
     * @throws IllegalArgumentException 개발 호출의 유효하지 않은 인자
     */
    public StockChangeResult sell(String code, int quantity) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
