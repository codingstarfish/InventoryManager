package inventory.service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import inventory.entity.PhysicalItem;
import inventory.dto.InventorySnapshot;
import inventory.validation.DomainRules;
import inventory.literal.ErrorCode;
import inventory.literal.Limits;
import inventory.database.InventoryDatabase;
import inventory.database.CommitCoordinator;
import inventory.repository.LogicalItemRepository;
import inventory.entity.LogicalItem;
import inventory.dto.RegisterResult;
import inventory.exception.BusinessRuleException;
import inventory.exception.SaveFailureException;
import java.util.Objects;

/**
 * 신규 상품 등록. 논리 파일만 저장하며 물리 파일은 변경하지 않습니다. 화면 입출력 없음.
 */
public class ProductService {
    /** 전체 공유 상태 */
    private final InventoryDatabase db;
    /** 같은 db를 사용하는 논리 저장소 */
    private final LogicalItemRepository logicalRepo;
    /** 같은 db를 사용하는 저장·게시 담당 */
    private final CommitCoordinator commit;

    /**
     * 의존 부품을 보관합니다. 생성 시 파일 접근·입력·저장 없음.
     * @param db 전체 공유 상태
     * @param logicalRepo 같은 db를 사용하는 논리 저장소
     * @param commit 같은 db를 사용하는 저장·게시 담당
     */
    public ProductService(InventoryDatabase db, LogicalItemRepository logicalRepo, CommitCoordinator commit) {
        this.db = Objects.requireNonNull(db, "db");
        this.logicalRepo = Objects.requireNonNull(logicalRepo, "logicalRepo");
        this.commit = Objects.requireNonNull(commit, "commit");
    }

    /**
     * 등록 시작 전에 논리코드 여유를 확인합니다. 저장 없음.
     * @return 상품 수가 99999 미만이면 true
     */
    public boolean canRegister() {
        return logicalRepo.size() < Limits.MAX_PRODUCTS;
    }

    /**
     * 입고·판매 시작 전에 상품 존재를 확인합니다. 저장 없음.
     * @return 등록 상품이 하나 이상이면 true
     */
    public boolean hasProducts() {
        return logicalRepo.size() > 0;
    }

    /**
     * 유효 코드의 등록 여부를 확인합니다.
     * @param code InputValidator를 통과한 코드
     * @return 등록된 상품
     * @throws BusinessRuleException CODE_NOT_FOUND; details=Map.of()
     */
    public LogicalItem requireExisting(String code) {
        DomainRules.validateLogicalCode(code);
        return logicalRepo.findByCode(code)
                .orElseThrow(() -> new BusinessRuleException(ErrorCode.CODE_NOT_FOUND, Map.of()));
    }

    /**
     * 인자 검증 및 코드 여유 재확인 → K+1로 코드 생성 → 맵 복사·신규 상품과 빈 낱개 목록 추가
     * → 불변 후보 Snapshot 생성 → commit.commitLogical(candidate) → 결과 반환.
     * 동일 속성·이름 중복 허용. 창고가 가득 차도 등록 가능. 번호 선예약 금지.
     * @param name 검증된 정규화 이름
     * @param size 1~1000
     * @param price 10~10000000, 10의 배수
     * @return 저장·close·메모리 게시를 완료한 신규 상품
     * @throws BusinessRuleException LIMIT_LOGICAL_CODE
     * @throws SaveFailureException 논리 파일 저장 실패; 완료 출력 금지
     * @throws IllegalArgumentException 개발 호출의 인자가 공통 규칙 위반
     */
    public RegisterResult register(String name, int size, int price) {
        DomainRules.validateName(name);
        DomainRules.validateSize(size);
        DomainRules.validatePrice(price);
        InventorySnapshot before = db.snapshot();
        if (before.logicalItems().size() >= Limits.MAX_PRODUCTS) {
            throw new BusinessRuleException(ErrorCode.LIMIT_LOGICAL_CODE, Map.of());
        }
        LogicalItem item = new LogicalItem(DomainRules.formatLogicalCode(before.logicalItems().size() + 1), name, size, price);
        Map<String, LogicalItem> logical = new HashMap<>(before.logicalItems());
        Map<String, List<PhysicalItem>> physical = new HashMap<>(before.physicalByCode());
        logical.put(item.code(), item);
        physical.put(item.code(), List.of());
        InventorySnapshot candidate = new InventorySnapshot(logical, physical);
        RegisterResult result = new RegisterResult(item);
        commit.commitLogical(candidate);
        return result;
    }
}
