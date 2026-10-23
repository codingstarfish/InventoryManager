package inventory.exception;

import inventory.literal.ErrorCode;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 업무 오류 코드와 필수 상세값을 검증하여 전달합니다. 출력·종료·파일 변경 없음.
 */
public class BusinessRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 업무 오류 코드 */
    private final ErrorCode code;
    /** issuable / required,remaining / current; 그 외 Map.of() */
    private final Map<String, Long> details;

    /**
     * 오류 메타데이터를 보관합니다. 필드·코드 및 필수 details key는 호출자가 맞춰야 합니다.
     * @param code 업무 오류 코드
     * @param details issuable / required,remaining / current; 그 외 Map.of()
     */
    public BusinessRuleException(ErrorCode code, Map<String, Long> details) {
        super(Objects.requireNonNull(code, "code").name());
        this.code = Objects.requireNonNull(code, "code");
        this.details = Map.copyOf(details);
        Set<String> required = switch (code) {
            case CODE_NOT_FOUND, LIMIT_LOGICAL_CODE, LIMIT_PHYSICAL_CODE -> Set.of();
            case SUFFIX_SHORTAGE -> Set.of("issuable");
            case CAPACITY_SHORTAGE -> Set.of("required", "remaining");
            case STOCK_SHORTAGE -> Set.of("current");
            default -> throw new IllegalArgumentException("업무 오류 코드가 아닙니다: " + code);
        };
        if (!this.details.keySet().containsAll(required) || this.details.values().stream().anyMatch(value -> value < 0)) {
            throw new IllegalArgumentException("업무 오류 상세값이 없거나 유효하지 않습니다.");
        }
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
