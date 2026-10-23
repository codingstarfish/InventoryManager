package inventory.exception;

import inventory.literal.ErrorCode;

/**
 * 화면에 표시할 입력 오류 코드를 전달합니다. 출력·종료·파일 변경 없음.
 */
public class UserInputException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 해당 필드의 입력 오류 코드 */
    private final ErrorCode code;

    /**
     * 입력 검증에서 결정한 오류 코드를 보관합니다.
     * @param code 해당 필드의 입력 오류 코드
     */
    public UserInputException(ErrorCode code) {
        super(code.name());
        this.code = code;
    }

    /**
     * 오류 정보를 반환합니다.
     * @return 해당 필드의 입력 오류 코드
     */
    public ErrorCode code() {
        return code;
    }
}
