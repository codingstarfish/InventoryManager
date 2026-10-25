package inventory.validation;

import java.util.Locale;
// 논리코드와 접미번호 자릿수 맞추기. 값은 다른 검증기에서 먼저 확인
public final class DomainRules {
    private DomainRules() {
    }

    // 1~99999를 P00001~P99999 형태로 변환
    public static String formatLogicalCode(int number) {
        return String.format(Locale.ROOT, "P%05d", number);
    }

    // 1~999를 001~999 형태로 변환
    public static String formatSuffix(int suffix) {
        return String.format(Locale.ROOT, "%03d", suffix);
    }
}
