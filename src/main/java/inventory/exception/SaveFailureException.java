package inventory.exception;

/**
 * EntityWriter에서 발생, Console.run에서 안내 후 종료 1, 실패 시 파일 보존은 보장하지 않음
 */
public class SaveFailureException extends RuntimeException {
    /** 객체 직렬화 코드로, 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /** 저장에 실패한 대상 파일명 */
    private final String fileName;

    /**
     * 저장에 실패한 파일명과 원인 예외를 보관
     * @param fileName 저장 대상인 logical_item.txt 또는 physical_item.txt의 파일명
     * @param cause 쓰기·버퍼 반영·닫기 또는 파일 접근 중 발생한 null이 아닌 원인 예외
     */
    public SaveFailureException(String fileName, Throwable cause) {
        super(fileName + " 저장 실패", cause);
        this.fileName = fileName;
    }

    /**
     * 저장에 실패한 대상 파일의 이름을 조회
     * @return 예외 생성 시 전달한 논리 파일 또는 물리 파일의 이름
     */
    public String fileName() {
        return fileName;
    }
}
