package inventory.validation;

import inventory.literal.Field;
import inventory.literal.ErrorCode;
import inventory.literal.InputPatterns;
import inventory.literal.Limits;
import inventory.exception.UserInputException;
import java.util.Map;
import java.util.Objects;

/**
 * 사용자 입력 전용 순수 검증. 파일·콘솔·전역 상태 접근 없음. 모든 raw는 null 금지.
 */
public class InputValidator {
    /** 상태 없는 사용자 입력 검증기를 생성합니다. */
    public InputValidator() {
    }

    /**
     * 양 끝의 U+0020만 제거합니다. trim/strip/소문자 변환 금지.
     * 내부 공백·탭·다른 공백 문자는 보존. 파일 입력에 사용하지 않습니다.
     * @param raw null이 아닌 사용자 입력 원문
     * @return 정규화된 문자열; 표준 공백만 있으면 빈 문자열
     */
    public String normalizeSpaces(String raw) {
        Objects.requireNonNull(raw, "raw");
        int start = 0;
        int end = raw.length();
        while (start < end && raw.charAt(start) == ' ') {
            start++;
        }
        while (end > start && raw.charAt(end - 1) == ' ') {
            end--;
        }
        return raw.substring(start, end);
    }

    /**
     * 정규화 후 [1-5] 하나를 검사합니다.
     * @param raw 사용자 입력 원문
     * @return 메뉴 번호 1~5
     * @throws UserInputException MENU_SYNTAX
     */
    public int parseMainMenu(String raw) {
        String value = requirePattern(raw, InputPatterns.MAIN_MENU,
                Field.MENU, ErrorCode.MENU_SYNTAX);
        return value.charAt(0) - '0';
    }

    /**
     * 정규화 후 [12] 하나를 검사합니다.
     * @param raw 사용자 입력 원문
     * @return 1=전체, 2=단일
     * @throws UserInputException QUERY_MODE_SYNTAX
     */
    public int parseQueryMode(String raw) {
        String value = requirePattern(raw, InputPatterns.QUERY_MODE,
                Field.QUERY_MODE, ErrorCode.QUERY_MODE_SYNTAX);
        return value.charAt(0) - '0';
    }

    /**
     * 정규화 후 소문자 y/n 하나를 검사합니다. 대문자 거절.
     * @param raw 사용자 입력 원문
     * @return y이면 true, n이면 false
     * @throws UserInputException RETRY_SYNTAX
     */
    public boolean parseRetry(String raw) {
        String value = requirePattern(raw, InputPatterns.RETRY,
                Field.RETRY, ErrorCode.RETRY_SYNTAX);
        return value.equals("y");
    }

    /**
     * 정규화 후 완성형 한글·영문·숫자·U+0020만 1~30자.
     * 길이는 codePointCount로 계산; 내부 연속 공백 유지.
     * @param raw 사용자 입력 원문
     * @return 검증된 정규화 이름
     * @throws UserInputException NAME_SYNTAX
     */
    public String parseName(String raw) {
        String value = requirePattern(raw, InputPatterns.NAME,
                Field.NAME, ErrorCode.NAME_SYNTAX);
        int length = value.codePointCount(0, value.length());
        if (length < 1 || length > Limits.MAX_NAME_LENGTH) {
            throw inputError(Field.NAME, ErrorCode.NAME_SYNTAX);
        }
        return value;
    }

    /**
     * 정규화 → P[0-9]{5} → 번호 1~99999 순으로 검사. 등록 여부는 Service 담당.
     * @param raw 사용자 입력 원문
     * @return 검증된 대문자 논리코드
     * @throws UserInputException CODE_SYNTAX 다음 CODE_RANGE
     */
    public String parseLogicalCode(String raw) {
        String value = requirePattern(raw, InputPatterns.LOGICAL_CODE,
                Field.LOGICAL_CODE, ErrorCode.CODE_SYNTAX);
        int number = Integer.parseInt(value.substring(1));
        if (number < 1 || number > Limits.MAX_PRODUCTS) {
            throw inputError(Field.LOGICAL_CODE, ErrorCode.CODE_RANGE);
        }
        return value;
    }

    /**
     * 정규화 후 ASCII 숫자 1~10자리. 선행 0 허용. Long.parseLong 사용.
     * int로 먼저 변환하지 않습니다. 값의 의미 범위 검사는 호출자가 수행.
     * @param raw 사용자 입력 원문
     * @param field SIZE, PRICE, INBOUND_QUANTITY, SALE_QUANTITY 중 하나
     * @return 0~9999999999의 정확한 long 값
     * @throws UserInputException SIZE_SYNTAX / PRICE_SYNTAX / INBOUND_SYNTAX / SALE_SYNTAX
     * @throws IllegalArgumentException 허용하지 않은 field를 개발자가 전달
     */
    public long parseUnsignedDecimal(String raw, Field field) {
        Objects.requireNonNull(field, "field");
        ErrorCode syntaxCode = switch (field) {
            case SIZE -> ErrorCode.SIZE_SYNTAX;
            case PRICE -> ErrorCode.PRICE_SYNTAX;
            case INBOUND_QUANTITY -> ErrorCode.INBOUND_SYNTAX;
            case SALE_QUANTITY -> ErrorCode.SALE_SYNTAX;
            default -> throw new IllegalArgumentException("숫자 입력 필드가 아닙니다: " + field);
        };
        String value = requirePattern(raw, InputPatterns.INPUT_DECIMAL, field, syntaxCode);
        // 숫자 문법의 최대 10자리는 long 범위 안입니다. 의미 검사 전에 int로 줄이지 않습니다.
        return Long.parseLong(value);
    }

    /**
     * 숫자 문법 → 1~1000 범위 검사 → int 변환.
     * @param raw 사용자 입력 원문
     * @return 검증된 크기
     * @throws UserInputException SIZE_SYNTAX 다음 SIZE_RANGE
     */
    public int parseSize(String raw) {
        long value = parseUnsignedDecimal(raw, Field.SIZE);
        if (value < Limits.MIN_SIZE || value > Limits.MAX_SIZE) {
            throw inputError(Field.SIZE, ErrorCode.SIZE_RANGE);
        }
        return (int) value;
    }

    /**
     * 숫자 문법 → 10~10000000 범위 → 10의 배수 순으로 검사.
     * @param raw 사용자 입력 원문
     * @return 검증된 원 단위 가격
     * @throws UserInputException PRICE_SYNTAX, PRICE_RANGE, PRICE_UNIT 순
     */
    public int parsePrice(String raw) {
        long value = parseUnsignedDecimal(raw, Field.PRICE);
        if (value < Limits.MIN_PRICE || value > Limits.MAX_PRICE) {
            throw inputError(Field.PRICE, ErrorCode.PRICE_RANGE);
        }
        if (value % Limits.PRICE_UNIT != 0) {
            throw inputError(Field.PRICE, ErrorCode.PRICE_UNIT);
        }
        return (int) value;
    }

    /**
     * 숫자 문법 → 1~100 범위 → int 변환.
     * @param raw 사용자 입력 원문
     * @param field INBOUND_QUANTITY 또는 SALE_QUANTITY
     * @return 검증된 요청 수량
     * @throws UserInputException 해당 INBOUND/SALE_SYNTAX 다음 RANGE
     * @throws IllegalArgumentException 허용하지 않은 field
     */
    public int parseQuantity(String raw, Field field) {
        Objects.requireNonNull(field, "field");
        ErrorCode rangeCode = switch (field) {
            case INBOUND_QUANTITY -> ErrorCode.INBOUND_RANGE;
            case SALE_QUANTITY -> ErrorCode.SALE_RANGE;
            default -> throw new IllegalArgumentException("수량 입력 필드가 아닙니다: " + field);
        };
        long value = parseUnsignedDecimal(raw, field);
        if (value < Limits.MIN_QUANTITY || value > Limits.MAX_QUANTITY) {
            throw inputError(field, rangeCode);
        }
        return (int) value;
    }

    /** 표준 공백 정규화 후 전체 문자열의 문법을 검사합니다. */
    private String requirePattern(String raw, String pattern, Field field, ErrorCode code) {
        String value = normalizeSpaces(raw);
        if (!value.matches(pattern)) {
            throw inputError(field, code);
        }
        return value;
    }

    /** 입력 오류에는 숫자 상세값이 필요하지 않으므로 빈 불변 맵을 사용합니다. */
    private UserInputException inputError(Field field, ErrorCode code) {
        return new UserInputException(field, code, Map.of());
    }
}
