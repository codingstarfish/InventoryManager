package inventory.validation;

import inventory.database.ParsedFiles;
import inventory.dto.InventorySnapshot;
import inventory.exception.StartupDataException;

/**
 * 시작 의미·관계 검사. 파일 접근·저장·상태 게시 없음. 구체 ErrorCode·reason 사용.
 */
public class FileIntegrityValidator {
    /** 상태 없는 시작 의미·관계 검증기를 생성합니다. */
    public FileIntegrityValidator() {
    }

    /**
     * 양쪽 문법 통과 후에만 실행. 개별 범위·가격 단위 → 논리 중복·P00001부터 연속
     * → 등록 참조 → code/suffix 조합 중복 → 상품별 001부터 연속 → 접미번호순 FIFO
     * → H=T+Q, A=999-H, 각 0~999 → sum((long)size*Q)&lt;=1000000.
     * 중복 검사 전에 Map.put으로 원본을 덮어쓰지 않습니다.
     * 파일의 원래 행 순서는 자유. 미판매를 만난 뒤 판매 완료가 나오면 FIFO 오류.
     * 유효 Entity와 상품별 빈 목록도 포함한 깊은 불변 Snapshot 구성.
     * @param files 두 파일의 모든 문법 검사를 통과한 중간 행 목록
     * @return 모든 의미·관계 규칙을 만족하는 초기 상태
     * @throws StartupDataException 각 FILE_*_RANGE/PRICE_UNIT, DUPLICATE_CODE/CODE_GAP, UNKNOWN_REFERENCE, DUPLICATE_PHYSICAL/SUFFIX_GAP, FIFO, CAPACITY
     */
    public InventorySnapshot validateAndBuild(ParsedFiles files) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
