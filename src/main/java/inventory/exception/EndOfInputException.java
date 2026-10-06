package inventory.exception;

/**
 * Input.readLine에서 발생, Console.run에서 안내 후 종료 0. 추가 데이터 없음.
 */
public class EndOfInputException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * EOF를 표시합니다. 빈 문자열 입력에는 사용하지 않습니다.
     */
    public EndOfInputException() {
        super("입력 종료");
    }
}
