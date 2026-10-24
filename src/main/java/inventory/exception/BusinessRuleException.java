package inventory.exception;

import inventory.literal.ErrorCode;
import java.util.Map;

/**
 * 업무 오류 코드와 화면에 표시할 상세값을 전달합니다. 출력·종료·파일 변경 없음.
 */
public class BusinessRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 업무 오류 코드 */
    private final ErrorCode code;
    /** issuable / required,remaining / current; 그 외 Map.of() */
    private final Map<String, Long> details;

    /**
     * 서비스가 결정한 업무 오류 코드와 출력에 필요한 상세값을 보관합니다.
     * @param code 업무 오류 코드
     * @param details issuable / required,remaining / current; 그 외 Map.of()
     */
    public BusinessRuleException(ErrorCode code, Map<String, Long> details) {
        super(code.name());
        this.code = code;
        this.details = details;
    }

    /**
     * 오류 정보를 반환합니다.
     * @return 업무 오류 코드
     */
    public ErrorCode code() {
        return code;
    }

    /**
     * 오류 정보를 반환합니다.
     * @return issuable / required,remaining / current; 그 외 Map.of()
     */
    public Map<String, Long> details() {
        return details;
    }
}
