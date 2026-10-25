package inventory.exception;

import inventory.literal.ErrorCode;
import java.util.Map;

/**
 * 업무 오류 코드와 화면에 표시할 상세값을 전달
 */
public class BusinessRuleException extends RuntimeException {

    /** 객체 직렬화 코드로, 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /** 업무 오류 코드 */
    private final ErrorCode code;

    /** issuable / required,remaining / current */
    private final Map<String, Long> details;

    /**
     * 업무 조건 위반의 오류 코드와 화면 표시용 상세 수치를 보관
     * @param code 접미번호,창고 용량과 재고 부족,대상 상품 부재 등을 나타내는 업무 오류 코드
     * @param details 오류별 표시 수치, issuable, required, remaining, current 키를 필요에 따라 사용하고, 나머지 오류는 안씀
     */
    public BusinessRuleException(ErrorCode code, Map<String, Long> details) {
        super(code.name());
        this.code = code;
        this.details = details;
    }

    /**
     * 예외가 나타내는 오류 코드를 조회
     * @return 예외를 생성할 때 전달한 오류 코드
     */
    public ErrorCode code() {
        return code;
    }

    /**
     * 업무 오류 안내에 필요한 상세 수치를 조회
     * @return 생성 시 전달한 상세 수치 Map으로, 오류에 따라 발급 가능 수 or 필요/남은 용량 or 현재 재고가 들있음
     */
    public Map<String, Long> details() {
        return details;
    }
}
