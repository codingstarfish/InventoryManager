package inventory.validation;

/**
 * 순수한 공통 값 검증·포맷. static 메서드, 가변 상태 없음.
 * 입력/파일 검증기는 실패를 자신의 예외·ErrorCode로 변환합니다.
 */
public final class DomainRules {
    private DomainRules() {
    }

    /**
     * 공통 값 조건을 검사합니다. 허용 문자 1~30자, 양끝 U+0020 금지. 길이는 codePointCount.
     * @param normalized 검사할 값; 문자열은 null 금지
     * @throws IllegalArgumentException 허용 조건 위반
     */
    public static void validateName(String normalized) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 공통 값 조건을 검사합니다. P[0-9]{5}, 번호 1~99999.
     * @param code 검사할 값; 문자열은 null 금지
     * @throws IllegalArgumentException 허용 조건 위반
     */
    public static void validateLogicalCode(String code) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 공통 값 조건을 검사합니다. 1~1000.
     * @param value 검사할 값; 문자열은 null 금지
     * @throws IllegalArgumentException 허용 조건 위반
     */
    public static void validateSize(long value) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 공통 값 조건을 검사합니다. 10~10000000 범위 다음 10의 배수 검사.
     * @param value 검사할 값; 문자열은 null 금지
     * @throws IllegalArgumentException 허용 조건 위반
     */
    public static void validatePrice(long value) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 공통 값 조건을 검사합니다. 1~100.
     * @param value 검사할 값; 문자열은 null 금지
     * @throws IllegalArgumentException 허용 조건 위반
     */
    public static void validateQuantity(long value) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * Locale.ROOT로 P와 숫자 다섯 자리 조합.
     * @param number 1~99999
     * @return P00001~P99999
     * @throws IllegalArgumentException 번호 범위 밖
     */
    public static String formatLogicalCode(int number) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * Locale.ROOT로 숫자 세 자리 포맷.
     * @param suffix 1~999
     * @return 001~999
     * @throws IllegalArgumentException 접미번호 범위 밖
     */
    public static String formatSuffix(int suffix) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
