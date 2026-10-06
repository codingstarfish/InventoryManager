package inventory.database;

/**
 * lineNumber: 원본의 1부터 시작하는 행 번호.
 * logicalCode: 문법 통과한 논리코드.
 * suffix: 세 자리 숫자를 해석한 0~999.
 * sold: 파일 0=false, 1=true.
 * Entity가 아닌 문법 단계 중간 행. P00000, suffix=0도 의미 검사 전 허용.
 * 생성자는 null·행 번호·문법 범위만 확인하며 등록 존재·suffix 의미 범위를 검사하지 않습니다.
 */
public record RawPhysicalRow(
        int lineNumber,
        String logicalCode,
        int suffix,
        boolean sold
) {
    /**
     * 생성 시 null·원본 행 번호·문법 범위만 검사합니다. 의미 검사는 하지 않습니다.
     * Entity가 아닌 문법 단계 중간 행. P00000, suffix=0도 의미 검사 전 허용.
     * 생성자는 null·행 번호·문법 범위만 확인하며 등록 존재·suffix 의미 범위를 검사하지 않습니다.
     * @param lineNumber 원본의 1부터 시작하는 행 번호
     * @param logicalCode 문법 통과한 논리코드
     * @param suffix 세 자리 숫자를 해석한 0~999
     * @param sold 파일 0=false, 1=true
     * @throws IllegalArgumentException 행 번호 또는 문법 범위 조건 위반
     * @throws NullPointerException 문자열 필드가 null
     */
    public RawPhysicalRow {
        // TODO: 위 문법 단계의 생성자 검증만 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
