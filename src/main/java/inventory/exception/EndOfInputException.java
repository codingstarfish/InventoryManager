package inventory.exception;

/**
 * Input.readLine에서 발생, Console.run에서 안내 후 종료 0. 추가 데이터 없음.
 */
public class EndOfInputException extends RuntimeException {
    /** 객체 직렬화 코드로, 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /**
     * 입력에서 EOF를 확인했음을 나타내는 예외를 생성
     */
    public EndOfInputException() {
        super("입력 종료");
    }
}
