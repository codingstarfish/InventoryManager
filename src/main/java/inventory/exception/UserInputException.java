package inventory.exception;

import inventory.literal.ErrorCode;
import inventory.literal.Field;
import java.util.Map;
import java.util.Objects;

/**
 * 입력 오류 코드와 필드를 검증하고 불변 메타데이터를 전달합니다. 출력·종료·파일 변경 없음.
 */
public class UserInputException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 입력 대상 필드 */
    private final Field field;
    /** 해당 필드의 입력 오류 코드 */
    private final ErrorCode code;
    /** 필수 숫자 상세값, 없으면 Map.of() */
    private final Map<String, Long> details;

    /**
     * 오류 메타데이터를 보관합니다. 필드·코드 및 필수 details key는 호출자가 맞춰야 합니다.
     * @param field 입력 대상 필드
     * @param code 해당 필드의 입력 오류 코드
     * @param details 필수 숫자 상세값, 없으면 Map.of()
     */
    public UserInputException(Field field, ErrorCode code, Map<String, Long> details) {
        super(Objects.requireNonNull(code, "code").name());
        this.field = Objects.requireNonNull(field, "field");
        this.code = Objects.requireNonNull(code, "code");
        this.details = Map.copyOf(details);
        boolean matchesField = switch (field) {
            case MENU -> code == ErrorCode.MENU_SYNTAX;
            case NAME -> code == ErrorCode.NAME_SYNTAX;
            case SIZE -> code == ErrorCode.SIZE_SYNTAX || code == ErrorCode.SIZE_RANGE;
            case PRICE -> code == ErrorCode.PRICE_SYNTAX || code == ErrorCode.PRICE_RANGE
                    || code == ErrorCode.PRICE_UNIT;
            case LOGICAL_CODE -> code == ErrorCode.CODE_SYNTAX || code == ErrorCode.CODE_RANGE;
            case INBOUND_QUANTITY -> code == ErrorCode.INBOUND_SYNTAX || code == ErrorCode.INBOUND_RANGE;
            case SALE_QUANTITY -> code == ErrorCode.SALE_SYNTAX || code == ErrorCode.SALE_RANGE;
            case QUERY_MODE -> code == ErrorCode.QUERY_MODE_SYNTAX;
            case RETRY -> code == ErrorCode.RETRY_SYNTAX;
        };
        if (!matchesField) {
            throw new IllegalArgumentException("입력 필드와 오류 코드가 일치하지 않습니다.");
        }
    }

    /**
     * 오류 정보를 반환합니다.
     * @return 입력 대상 필드
     */
    public Field field() {
        return field;
    }

    /**
     * 오류 정보를 반환합니다.
     * @return 해당 필드의 입력 오류 코드
     */
    public ErrorCode code() {
        return code;
    }

    /**
     * 오류 정보를 반환합니다.
     * @return 필수 숫자 상세값, 없으면 Map.of()
     */
    public Map<String, Long> details() {
        return details;
    }
}
