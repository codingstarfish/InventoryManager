package inventory.service;

import inventory.repository.LogicalItemRepository;
import inventory.repository.PhysicalItemRepository;
import inventory.dto.AllInventoryResult;
import inventory.dto.ItemDetail;
import inventory.exception.BusinessRuleException;
import java.util.Objects;

/**
 * 메모리 상태 조회만 수행. 사용자 입력·출력·파일 쓰기 없음.
 */
public class QueryService {
    /** 공유 db의 논리 저장소 */
    private final LogicalItemRepository logicalRepo;
    /** 동일 db의 낱개 저장소 */
    private final PhysicalItemRepository physicalRepo;

    /**
     * 의존 부품을 보관합니다. 생성 시 파일 접근·입력·저장 없음.
     * @param logicalRepo 공유 db의 논리 저장소
     * @param physicalRepo 동일 db의 낱개 저장소
     */
    public QueryService(LogicalItemRepository logicalRepo, PhysicalItemRepository physicalRepo) {
        this.logicalRepo = Objects.requireNonNull(logicalRepo, "logicalRepo");
        this.physicalRepo = Objects.requireNonNull(physicalRepo, "physicalRepo");
    }

    /**
     * 전체 상품을 수량 정보와 창고 합계로 조회합니다.
     * 재고 0·중복 이름 상품도 각각 유지. 파일 재읽기·저장 없음.
     * @return 논리코드순 전체 상품 및 창고 합계; 상품 없음도 정상
     */
    public AllInventoryResult findAll() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 선택 상품과 판매 기록을 포함한 전체 낱개 조회. 창고 합계 추가 없음.
     * @param code 검증된 논리코드
     * @return 상품 수량·접미번호순 불변 낱개 목록
     * @throws BusinessRuleException CODE_NOT_FOUND
     */
    public ItemDetail findOne(String code) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
