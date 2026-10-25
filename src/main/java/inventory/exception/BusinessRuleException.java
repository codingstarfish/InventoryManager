package inventory.exception;

import inventory.literal.ErrorCode;
import java.util.Map;

// 업무 조건에 맞지 않을 때 사용
// code: CODE_NOT_FOUND, LIMIT_PHYSICAL_CODE, SUFFIX_SHORTAGE, CAPACITY_SHORTAGE, STOCK_SHORTAGE
public class BusinessRuleException extends RuntimeException {

    /** 객체 직렬화 코드. 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /** 업무 오류 코드 */
    private final ErrorCode code;

    /** issuable / required,remaining / current */
    private final Map<String, Long> details;

    public BusinessRuleException(ErrorCode code, Map<String, Long> details) {
        super(code.name());
        this.code = code;
        this.details = details;
    }

    public ErrorCode code() {
        return code;
    }

    public Map<String, Long> details() {
        return details;
    }
}
