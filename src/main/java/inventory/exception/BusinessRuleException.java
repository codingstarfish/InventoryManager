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

    /**
     * 업무 오류 코드와 화면 안내에 필요한 수치를 보관
     * @param code 상품 부재, 발급 한도 또는 수량·용량 부족을 나타내는 업무 오류 코드
     * @param details 오류별 안내 수치. issuable/current는 개수, required/remaining은 용량 단위이며 필요 없으면 빈 Map
     */
    public BusinessRuleException(ErrorCode code, Map<String, Long> details) {
        super(code.name());
        this.code = code;
        this.details = details;
    }

    /**
     * @return 예외가 나타내는 업무 오류 코드
     */
    public ErrorCode code() {
        return code;
    }

    /**
     * @return 생성 시 전달한 오류별 안내 수치 Map(발급 가능 수, 필요·남은 용량 또는 현재 재고)
     */
    public Map<String, Long> details() {
        return details;
    }
}
