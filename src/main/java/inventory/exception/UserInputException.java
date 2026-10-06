package inventory.exception;

import inventory.literal.ErrorCode;
import inventory.literal.Field;
import java.util.Map;
import java.util.Objects;

/**
 * 오류 전달용 예외. 생성자와 접근자는 연결용으로 준비됨. 출력·종료·파일 변경 없음.
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
