package inventory.validation;

import java.util.Locale;
/**
 * 검증을 마친 코드와 접미번호를 정해진 자릿수로 표시
 * 값 검사는 InputValidator와 FileIntegrityValidator가 함
 */
public final class DomainRules {
    /**
     * 외부 인스턴스 만들기 막음
     */
    private DomainRules() {
    }

    /**
     * 검증된 논리번호를 Locale.ROOT 기준의 고정 자릿수 코드로 표시, 값의 범위를 다시 검사는 안함
     * @param number 범위 검사를 마친 1~99999 사이의 논리번호
     * @return 대문자 P와 숫자 다섯 자리로 구성된 P00001~P99999 형식의 문자열
     */
    public static String formatLogicalCode(int number) {
        return String.format(Locale.ROOT, "P%05d", number);
    }

    /**
     * 검증된 접미번호를 Locale.ROOT 기준의 세 자리 숫자로 표시합니다, 값의 범위를 다시 검사는 안함
     * @param suffix 범위 검사를 마친 1~999 사이의 접미번호
     * @return 선행 0을 유지하는 001~999 형식의 문자열
     */
    public static String formatSuffix(int suffix) {
        return String.format(Locale.ROOT, "%03d", suffix);
    }
}
