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

    public StartupDataException(List<String> fileNames, String reason) {
        this(fileNames, reason, null);
    }

    // 파일명, 오류 이유, 원인을 보관. 원인이 없는 문법/의미 오류는 cause에 null 사용
    public StartupDataException(List<String> fileNames, String reason, Throwable cause) {
        super(reason, cause);
        this.fileNames = fileNames;
        this.reason = reason;
    }

    public List<String> fileNames() {
        return fileNames;
    }

    public String reason() {
        return reason;
    }
}
