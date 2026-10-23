package inventory.validation;

import java.util.Locale;
/**
 * 검증을 마친 코드와 접미번호를 정해진 자릿수로 표시합니다.
 * 값 검사는 InputValidator와 FileIntegrityValidator가 담당합니다.
 */
public final class DomainRules {
    private DomainRules() {
    }

    /**
     * Locale.ROOT로 P와 숫자 다섯 자리 조합.
     * @param number 1~99999
     * @return P00001~P99999
     */
    public static String formatLogicalCode(int number) {
        return String.format(Locale.ROOT, "P%05d", number);
    }

    /**
     * Locale.ROOT로 숫자 세 자리 포맷.
     * @param suffix 1~999
     * @return 001~999
     */
    public static String formatSuffix(int suffix) {
        return String.format(Locale.ROOT, "%03d", suffix);
    }
}
