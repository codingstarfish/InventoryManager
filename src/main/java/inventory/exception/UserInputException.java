package inventory.exception;

import inventory.literal.ErrorCode;

// 입력 문법이나 값 범위, 단위 규칙을 어겼을 때 사용
public class UserInputException extends RuntimeException {
    /** 객체 직렬화 코드로, 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /** 해당 필드의 입력 오류 코드 */
    private final ErrorCode code;

    public UserInputException(ErrorCode code) {
        super(code.name());
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
