package inventory.exception;

import inventory.literal.ErrorCode;

/**
 * 화면에 표시할 입력 오류 코드를 전달
 */
public class UserInputException extends RuntimeException {
    /** 객체 직렬화 코드로, 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /** 해당 필드의 입력 오류 코드 */
    private final ErrorCode code;

    /**
     * 입력 검증에서 확인한 오류 코드를 보관
     * @param code 입력한 필드의 문법 또는 값 범위 위반을 나타내는 오류 코드
     */
    public UserInputException(ErrorCode code) {
        super(code.name());
        this.code = code;
    }

    /**
     * 예외가 나타내는 오류 코드를 조회
     * @return 예외를 생성할 때 전달한 오류 코드
     */
    public ErrorCode code() {
        return code;
    }
}
