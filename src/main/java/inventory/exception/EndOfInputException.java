package inventory.exception;

// EOF 발생 시 예외
public class EndOfInputException extends RuntimeException {
    /** 객체 직렬화 코드. 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    public EndOfInputException() {
        super("입력 종료");
    }
}
