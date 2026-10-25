package inventory.validation;

import inventory.literal.Field;
import inventory.literal.ErrorCode;
import inventory.literal.InputPatterns;
import inventory.literal.Limits;
import inventory.exception.UserInputException;


public class InputValidator {

    public InputValidator() {
    }

   
    // 양 끝의 일반 공백(U+0020)만 제거. 탭은 남김
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

    // 주 메뉴: 1~5
    public int parseMainMenu(String raw) {
        String value = requirePattern(raw, InputPatterns.MAIN_MENU, ErrorCode.MENU_SYNTAX);
        return value.charAt(0) - '0';
    }

    
    // 조회 방식: 1은 전체, 2는 단일 상품
    public int parseQueryMode(String raw) {
        String value = requirePattern(raw, InputPatterns.QUERY_MODE, ErrorCode.QUERY_MODE_SYNTAX);
        return value.charAt(0) - '0';
    }

    // 소문자 y/n만 허용. y면 true
    public boolean parseRetry(String raw) {
        String value = requirePattern(raw, InputPatterns.RETRY, ErrorCode.RETRY_SYNTAX);
        return value.equals("y");
    }

    // 상품명: 허용 문자로 1~30자. 내부 공백은 유지
    public String parseName(String raw) {
        return requirePattern(raw, InputPatterns.NAME, ErrorCode.NAME_SYNTAX);
    }

    // P + 숫자 다섯 자리인지 검사한 뒤 P00000 거절
    public String parseLogicalCode(String raw) {
        String value = requirePattern(raw, InputPatterns.LOGICAL_CODE, ErrorCode.CODE_SYNTAX);
        int number = Integer.parseInt(value.substring(1));
        if (number == 0) {
            throw new UserInputException(ErrorCode.CODE_RANGE);
        }
        return value;
    }

    // 숫자 1~10자리와 선행 0 허용. 부호나 소수점은 불가
    // 최대 9999999999까지 읽어야 하므로 long 사용. 범위는 각 항목에서 검사
    public long parseUnsignedDecimal(String raw, Field field) {
        ErrorCode syntaxCode = switch (field) {
            case SIZE -> ErrorCode.SIZE_SYNTAX;
            case PRICE -> ErrorCode.PRICE_SYNTAX;
            case INBOUND_QUANTITY -> ErrorCode.INBOUND_SYNTAX;
            case SALE_QUANTITY -> ErrorCode.SALE_SYNTAX;
            default -> throw new IllegalArgumentException("숫자 입력 필드가 아닙니다: " + field);
        };
        String value = requirePattern(raw, InputPatterns.INPUT_DECIMAL, syntaxCode);
        return Long.parseLong(value);
    }

    // 숫자 형식 검사 후 1~1000 범위 확인
    public int parseSize(String raw) {
        long value = parseUnsignedDecimal(raw, Field.SIZE);
        if (value < Limits.MIN_SIZE || value > Limits.MAX_SIZE) {
            throw new UserInputException(ErrorCode.SIZE_RANGE);
        }
        return (int) value;
    }

    // 숫자 형식 → 10~10000000 범위 → 10의 배수 순서로 검사
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

    // 입고/판매 수량: 숫자 형식 검사 후 1~100 범위 확인
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

    // 양 끝의 일반 공백을 제거한 뒤 문자열 전체가 정규식과 맞는지 확인
    private String requirePattern(String raw, String pattern, ErrorCode code) {
        String value = normalizeSpaces(raw);
        if (!value.matches(pattern)) {
            throw new UserInputException(code);
        }
        return value;
    }
}
