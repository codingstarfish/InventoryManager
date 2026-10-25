package inventory.exception;

// writeLines에서 저장 실패 시 발생. Console.run에서 안내 후 종료 코드 1 반환
// 실패한 파일의 보존이나 복구는 보장하지 않음
public class SaveFailureException extends RuntimeException {
    /** 객체 직렬화 코드. 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /** 저장에 실패한 대상 파일명 */
    private final String fileName;

    // 실패한 파일명과 실제 원인(IOException/SecurityException)을 같이 보관
    public SaveFailureException(String fileName, Throwable cause) {
        super(fileName + " 저장 실패", cause);
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }
}
