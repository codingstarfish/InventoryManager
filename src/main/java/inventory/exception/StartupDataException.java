package inventory.exception;

import java.util.List;

/**
 * Main의 시작 경계에서만 처리. 기존 파일 미변경, 시작 중 생성된 빈 파일은 유지
 */
public class StartupDataException extends RuntimeException {
    /** 객체 직렬화 코드로, 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /** 문제 파일명, 최소 1개; 관계 오류는 관련 파일들 */
    private final List<String> fileNames;
    /** 기획서의 구체 오류 이유 */
    private final String reason;

    /**
     * 시작 파일 검사에서 발생한 오류의 파일명과 이유를 보관합니다.
     * @param fileNames 문제가 있는 파일명 목록
     * @param reason 위반한 문법,의미 규칙 또는 파일 접근 실패를 설명하는 구체적인 안내 문구
     */
    public StartupDataException(List<String> fileNames, String reason) {
        this(fileNames, reason, null);
    }

    /**
     * 시작 검사 오류의 파일명과 이유 및 원인 예외를 보관합니다.
     * @param fileNames 문제가 있는 파일명 목록
     * @param reason 사용자에게 표시할 구체적인 오류 이유
     * @param cause 파일 입출력이나 접근 실패의 원인 예외, 원인 예외가 없는 문법,의미 오류이면 null입니다.
     */
    public StartupDataException(List<String> fileNames, String reason, Throwable cause) {
        super(reason, cause);
        this.fileNames = fileNames;
        this.reason = reason;
    }

    /**
     * 시작 검사 오류와 관련된 파일명 조회
     * @return 생성 시 전달한 오류 파일명 목록
     */
    public List<String> fileNames() {
        return fileNames;
    }

    /**
     * 시작 검사 오류의 구체적인 이유를 조회합니다.
     * @return 위반한 규칙이나 파일 접근 실패 이유를 설명하는 안내 문자열
     */
    public String reason() {
        return reason;
    }
}
