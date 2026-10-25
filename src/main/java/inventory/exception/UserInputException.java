package inventory.exception;

import inventory.literal.ErrorCode;

// 입력 문법이나 값 범위, 단위 규칙을 어겼을 때 사용
public class UserInputException extends RuntimeException {
    /** 객체 직렬화 코드로, 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /** 해당 필드의 입력 오류 코드 */
    private final ErrorCode code;

    /**
     * 입력값의 문법·범위·단위 오류 코드를 보관
     * @param code 입력 필드가 위반한 문법, 값 범위 또는 단위 규칙의 오류 코드
     */
    public UserInputException(ErrorCode code) {
        super(code.name());
        this.code = code;
    }

    /**
     * @return 입력값이 위반한 규칙을 나타내는 오류 코드
     */
    public ErrorCode code() {
        return code;
    }
}
