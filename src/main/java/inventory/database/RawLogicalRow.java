package inventory.database;

/**
 * lineNumber: 원본의 1부터 시작하는 행 번호.
 * name: 문법 통과한 이름.
 * code: P와 숫자 다섯 자리.
 * size: 파일 문법 통과한 크기.
 * price: 파일 문법 통과한 가격.
 * Entity가 아닌 문법 단계 중간 행. P00000, size=1001, price=1도 의미 검사 전 허용.
 * 생성자는 null·행 번호·문법 범위만 확인하며 DomainRules 의미 검사를 호출하지 않습니다.
 */
public record RawLogicalRow(
        int lineNumber,
        String name,
        String code,
        int size,
        int price
) {
    /**
     * 생성 시 null·원본 행 번호·문법 범위만 검사합니다. 의미 검사는 하지 않습니다.
     * Entity가 아닌 문법 단계 중간 행. P00000, size=1001, price=1도 의미 검사 전 허용.
     * 생성자는 null·행 번호·문법 범위만 확인하며 DomainRules 의미 검사를 호출하지 않습니다.
     * @param lineNumber 원본의 1부터 시작하는 행 번호
     * @param name 문법 통과한 이름
     * @param code P와 숫자 다섯 자리
     * @param size 파일 문법 통과한 크기
     * @param price 파일 문법 통과한 가격
     * @throws IllegalArgumentException 행 번호 또는 문법 범위 조건 위반
     * @throws NullPointerException 문자열 필드가 null
     */
    public RawLogicalRow {
        // TODO: 위 문법 단계의 생성자 검증만 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
