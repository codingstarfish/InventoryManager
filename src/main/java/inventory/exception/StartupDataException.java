package inventory.exception;

import java.util.List;

/**
 * Main의 시작 경계에서만 처리. 기존 파일 미변경, 시작 중 생성된 빈 파일은 유지.
 */
public class StartupDataException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 문제 파일명, 최소 1개; 관계 오류는 관련 파일들 */
    private final List<String> fileNames;
    /** 기획서 원판의 구체 오류 이유 */
    private final String reason;

    /**
     * 원인 예외 없는 문법·의미 오류.
     * @param fileNames 문제 파일명, 최소 1개
     * @param reason 구체적인 허용 규칙 또는 관계 위반 이유
     */
    public StartupDataException(List<String> fileNames, String reason) {
        this(fileNames, reason, null);
    }

    /**
     * 파일 접근·읽기 실패의 원인 예외도 보존합니다.
     * @param fileNames 문제 파일명, 최소 1개
     * @param reason 구체 오류 이유
     * @param cause 원인 예외; 문법·의미 오류는 null
     */
    public StartupDataException(List<String> fileNames, String reason, Throwable cause) {
        super(reason, cause);
        this.fileNames = fileNames;
        this.reason = reason;
    }

    /**
     * 시작 오류 메타데이터를 반환합니다.
     * @return 문제 파일명 목록
     */
    public List<String> fileNames() {
        return fileNames;
    }

    /**
     * 시작 오류 메타데이터를 반환합니다.
     * @return 구체 오류 이유
     */
    public String reason() {
        return reason;
    }
}
