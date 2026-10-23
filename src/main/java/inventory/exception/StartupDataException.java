package inventory.exception;

import inventory.literal.ErrorCode;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Main의 시작 경계에서만 처리. 기존 파일 미변경, 시작 중 생성된 빈 파일은 유지.
 */
public class StartupDataException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 문제 파일명, 최소 1개; 관계 오류는 관련 파일들 */
    private final List<String> fileNames;
    /** 시작 오류 코드 */
    private final ErrorCode code;
    /** 기획서 원판의 구체 오류 이유 */
    private final String reason;
    /** 1부터 시작하는 진단 행 번호, 없으면 empty */
    private final OptionalInt lineNumber;

    /**
     * 원인 예외 없는 문법·의미 오류.
     * @param fileNames 문제 파일명, 최소 1개
     * @param code 시작 오류 코드
     * @param reason 구체적인 허용 규칙 또는 관계 위반 이유
     * @param lineNumber 행 번호 1 이상 또는 OptionalInt.empty()
     */
    public StartupDataException(List<String> fileNames, ErrorCode code, String reason, OptionalInt lineNumber) {
        super(Objects.requireNonNull(reason, "reason"));
        this.fileNames = List.copyOf(fileNames);
        this.code = Objects.requireNonNull(code, "code");
        this.reason = reason;
        this.lineNumber = Objects.requireNonNull(lineNumber, "lineNumber");
        if (this.fileNames.isEmpty() || (lineNumber.isPresent() && lineNumber.getAsInt() < 1)) {
            throw new IllegalArgumentException("파일명 또는 행 번호 계약 위반");
        }
        if (this.fileNames.stream().anyMatch(String::isEmpty) || reason.isEmpty()
                || !code.name().startsWith("FILE_")) {
            throw new IllegalArgumentException("시작 오류 메타데이터 계약 위반");
        }
    }

    /**
     * 파일 접근·읽기 실패의 원인 예외도 보존합니다.
     * @param fileNames 문제 파일명, 최소 1개
     * @param code 시작 오류 코드
     * @param reason 구체 오류 이유
     * @param lineNumber 행 번호 또는 empty
     * @param cause null이 아닌 원인 예외
     */
    public StartupDataException(List<String> fileNames, ErrorCode code, String reason, OptionalInt lineNumber, Throwable cause) {
        this(fileNames, code, reason, lineNumber);
        initCause(Objects.requireNonNull(cause, "cause"));
    }

    /**
     * 시작 오류 메타데이터를 반환합니다.
     * @return 불변 파일명 목록
     */
    public List<String> fileNames() {
        return fileNames;
    }

    /**
     * 시작 오류 메타데이터를 반환합니다.
     * @return 시작 오류 코드
     */
    public ErrorCode code() {
        return code;
    }

    /**
     * 시작 오류 메타데이터를 반환합니다.
     * @return 구체 오류 이유
     */
    public String reason() {
        return reason;
    }

    /**
     * 시작 오류 메타데이터를 반환합니다.
     * @return 진단 행 번호 또는 empty
     */
    public OptionalInt lineNumber() {
        return lineNumber;
    }
}
