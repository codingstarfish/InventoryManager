package inventory.validation;

import java.util.Locale;
import inventory.literal.Limits;
import inventory.literal.InputPatterns;
import java.util.Objects;
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
        Objects.requireNonNull(normalized, "normalized");
        int length = normalized.codePointCount(0, normalized.length());
        if (!normalized.matches(InputPatterns.NAME) || length < 1 || length > Limits.MAX_NAME_LENGTH
                || normalized.startsWith(" ") || normalized.endsWith(" ")) {
            throw new IllegalArgumentException("상품명 규칙 위반");
        }
    }

    /**
     * 공통 값 조건을 검사합니다. P[0-9]{5}, 번호 1~99999.
     * @param code 검사할 값; 문자열은 null 금지
     * @throws IllegalArgumentException 허용 조건 위반
     */
    public static void validateLogicalCode(String code) {
        Objects.requireNonNull(code, "code");
        if (!code.matches(InputPatterns.LOGICAL_CODE)) {
            throw new IllegalArgumentException("논리코드 문법 위반");
        }
        int number = Integer.parseInt(code.substring(1));
        if (number < 1 || number > Limits.MAX_PRODUCTS) {
            throw new IllegalArgumentException("논리코드 번호 범위 위반");
        }
    }

    /**
     * 공통 값 조건을 검사합니다. 1~1000.
     * @param value 검사할 값; 문자열은 null 금지
     * @throws IllegalArgumentException 허용 조건 위반
     */
    public static void validateSize(long value) {
        if (value < Limits.MIN_SIZE || value > Limits.MAX_SIZE) {
            throw new IllegalArgumentException("허용 범위 위반: " + value);
        }
    }

    /**
     * 공통 값 조건을 검사합니다. 10~10000000 범위 다음 10의 배수 검사.
     * @param value 검사할 값; 문자열은 null 금지
     * @throws IllegalArgumentException 허용 조건 위반
     */
    public static void validatePrice(long value) {
        if (value < Limits.MIN_PRICE || value > Limits.MAX_PRICE) {
            throw new IllegalArgumentException("가격 범위 위반: " + value);
        }
        if (value % Limits.PRICE_UNIT != 0) {
            throw new IllegalArgumentException("가격은 10원 단위여야 합니다.");
        }
    }

    /**
     * 공통 값 조건을 검사합니다. 1~100.
     * @param value 검사할 값; 문자열은 null 금지
     * @throws IllegalArgumentException 허용 조건 위반
     */
    public static void validateQuantity(long value) {
        if (value < Limits.MIN_QUANTITY || value > Limits.MAX_QUANTITY) {
            throw new IllegalArgumentException("허용 범위 위반: " + value);
        }
    }

    /**
     * Locale.ROOT로 P와 숫자 다섯 자리 조합.
     * @param number 1~99999
     * @return P00001~P99999
     * @throws IllegalArgumentException 번호 범위 밖
     */
    public static String formatLogicalCode(int number) {
        if (number < 1 || number > Limits.MAX_PRODUCTS) {
            throw new IllegalArgumentException("논리번호 범위 위반");
        }
        return String.format(Locale.ROOT, "P%05d", number);
    }

    /**
     * Locale.ROOT로 숫자 세 자리 포맷.
     * @param suffix 1~999
     * @return 001~999
     * @throws IllegalArgumentException 접미번호 범위 밖
     */
    public static String formatSuffix(int suffix) {
        if (suffix < 1 || suffix > Limits.MAX_SUFFIX) {
            throw new IllegalArgumentException("접미번호 범위 위반");
        }
        return String.format(Locale.ROOT, "%03d", suffix);
    }
}
