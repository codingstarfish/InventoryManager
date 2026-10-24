package inventory.validation;

import inventory.literal.Field;
import inventory.literal.ErrorCode;
import inventory.literal.InputPatterns;
import inventory.literal.Limits;
import inventory.exception.UserInputException;


public class InputValidator {

    public InputValidator() {
    }

   
    // 양 끝의 U+0020를 제거함. tap은 제거하지 않음.
    public String normalizeSpaces(String raw) {
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

    // [1-5]중 하나여야됨
    public int parseMainMenu(String raw) {
        String value = requirePattern(raw, InputPatterns.MAIN_MENU, ErrorCode.MENU_SYNTAX);
        return value.charAt(0) - '0';
    }

    
    // 1, 2중 하나여야됨
    public int parseQueryMode(String raw) {
        String value = requirePattern(raw, InputPatterns.QUERY_MODE, ErrorCode.QUERY_MODE_SYNTAX);
        return value.charAt(0) - '0';
    }

    // y, n중 하나여야됨
    public boolean parseRetry(String raw) {
        String value = requirePattern(raw, InputPatterns.RETRY, ErrorCode.RETRY_SYNTAX);
        return value.equals("y");
    }

    // 정규화 후 완성형 한글·영문·숫자·U+0020만 1~30자
    public String parseName(String raw) {
        return requirePattern(raw, InputPatterns.NAME, ErrorCode.NAME_SYNTAX);
    }

    // 정규화 → P[0-9]{5} → 번호 1~99999 순으로 검사
    public String parseLogicalCode(String raw) {
        String value = requirePattern(raw, InputPatterns.LOGICAL_CODE, ErrorCode.CODE_SYNTAX);
        int number = Integer.parseInt(value.substring(1));
        if (number == 0) {
            throw new UserInputException(ErrorCode.CODE_RANGE);
        }
        return value;
    }

    // 정규화 후 ASCII 숫자 1~10자리. 선행 0 허용
    public long parseUnsignedDecimal(String raw, Field field) {
        ErrorCode syntaxCode = switch (field) {
            case SIZE -> ErrorCode.SIZE_SYNTAX;
            case PRICE -> ErrorCode.PRICE_SYNTAX;
            case INBOUND_QUANTITY -> ErrorCode.INBOUND_SYNTAX;
            case SALE_QUANTITY -> ErrorCode.SALE_SYNTAX;
            default -> throw new IllegalArgumentException("숫자 입력 필드가 아닙니다: " + field);
        };
        String value = requirePattern(raw, InputPatterns.INPUT_DECIMAL, syntaxCode);
        // 숫자 문법의 최대 10자리는 long 범위 안입니다. 의미 검사 전에 int로 줄이지 않습니다.
        return Long.parseLong(value);
    }

    // 숫자 문법 검사 -> 1~1000 범위 검사
    public int parseSize(String raw) {
        long value = parseUnsignedDecimal(raw, Field.SIZE);
        if (value < Limits.MIN_SIZE || value > Limits.MAX_SIZE) {
            throw new UserInputException(ErrorCode.SIZE_RANGE);
        }
        return (int) value;
    }

    // 숫자 문법 검사 → 10~10000000 범위 검사 → 10의 배수 검사
    public int parsePrice(String raw) {
        long value = parseUnsignedDecimal(raw, Field.PRICE);
        if (value < Limits.MIN_PRICE || value > Limits.MAX_PRICE) {
            throw new UserInputException(ErrorCode.PRICE_RANGE);
        }
        if (value % Limits.PRICE_UNIT != 0) {
            throw new UserInputException(ErrorCode.PRICE_UNIT);
        }
        return (int) value;
    }

    // 숫자 문법 검사 → 1~100 범위 검사
    public int parseQuantity(String raw, Field field) {
        ErrorCode rangeCode = switch (field) {
            case INBOUND_QUANTITY -> ErrorCode.INBOUND_RANGE;
            case SALE_QUANTITY -> ErrorCode.SALE_RANGE;
            default -> throw new IllegalArgumentException("수량 입력 필드가 아닙니다: " + field);
        };
        long value = parseUnsignedDecimal(raw, field);
        if (value < Limits.MIN_QUANTITY || value > Limits.MAX_QUANTITY) {
            throw new UserInputException(rangeCode);
        }
        return (int) value;
    }

    // 공백을 제거한 후 문자열 검사
    private String requirePattern(String raw, String pattern, ErrorCode code) {
        String value = normalizeSpaces(raw);
        if (!value.matches(pattern)) {
            throw new UserInputException(code);
        }
        return value;
    }
}
