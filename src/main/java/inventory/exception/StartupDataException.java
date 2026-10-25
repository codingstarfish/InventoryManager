package inventory.exception;

import java.util.List;

// 데이터 시작 검사 시 오류가 났을 때 사용
public class StartupDataException extends RuntimeException {
    /** 객체 직렬화 코드로, 필요는 없는데 이거 없음 경고뜸*/
    private static final long serialVersionUID = 1L;

    /** 문제 파일명, 최소 1개; 관계 오류는 관련 파일들 */
    private final List<String> fileNames;
    /** 기획서의 구체 오류 이유 */
    private final String reason;

    /**
     * 별도 원인 예외 없이 시작 데이터 오류를 보관
     * @param fileNames 문제가 있는 파일명 목록(최소 1개). 관계 오류는 관련 파일들을 포함
     * @param reason 시작 검사를 통과하지 못한 구체적인 이유
     */
    public StartupDataException(List<String> fileNames, String reason) {
        this(fileNames, reason, null);
    }

    /**
     * 파일명, 오류 이유, 원인을 보관. 원인이 없는 문법/의미 오류는 cause에 null 사용
     * @param fileNames 문제가 있는 파일명 목록(최소 1개). 관계 오류는 관련 파일들을 포함
     * @param reason 시작 검사를 통과하지 못한 구체적인 이유
     * @param cause 원인 예외. 별도 원인이 없는 문법/의미 오류이면 null
     */
    public StartupDataException(List<String> fileNames, String reason, Throwable cause) {
        super(reason, cause);
        this.fileNames = fileNames;
        this.reason = reason;
    }

    /**
     * @return 문제가 있는 파일명 목록. 관계 오류이면 관련 파일들을 포함
     */
    public List<String> fileNames() {
        return fileNames;
    }

    /**
     * @return 시작 데이터 오류의 구체적인 이유
     */
    public String reason() {
        return reason;
    }
}
