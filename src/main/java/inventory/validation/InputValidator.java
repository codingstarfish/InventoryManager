package inventory.validation;

import inventory.literal.Field;
import inventory.literal.ErrorCode;
import inventory.literal.InputPatterns;
import inventory.literal.Limits;
import inventory.exception.UserInputException;


public class InputValidator {

    public InputValidator() {
    }

   
    /**
     * 문자열 양 끝의 표준 공백 U+0020만 제거
     * @param raw 사용자가 입력한 null이 아닌 원문 문자열
     * @return 양 끝의 표준 공백만 제거한 문자열
     */
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

    /**
     * 양 끝의 표준 공백을 제거하고 주 메뉴 번호의 문법을 검사
     * @param raw 주 메뉴에서 입력한 원문 문자열, 공백 제거 후 한 자리 숫자 1~5여야 함
     * @return 선택한 메뉴를 나타내는 1~5 사이의 정수
     */
    public int parseMainMenu(String raw) {
        String value = requirePattern(raw, InputPatterns.MAIN_MENU, ErrorCode.MENU_SYNTAX);
        return value.charAt(0) - '0';
    }

    
    /**
     * 양 끝의 표준 공백을 제거하고 조회 방식의 문법을 검사
     * @param raw 조회 방식으로 입력한 원문 문자열입니다. 공백 제거 후 1 or 2여야됨
     * @return 전체 조회이면 1, 단일 상품 조회이면 2
     */
    public int parseQueryMode(String raw) {
        String value = requirePattern(raw, InputPatterns.QUERY_MODE, ErrorCode.QUERY_MODE_SYNTAX);
        return value.charAt(0) - '0';
    }

    /**
     * 양 끝의 표준 공백을 제거하고 소문자 y 또는 n인지 검사합니다.
     * @param raw 재입력 확인 단계의 원문 문자열
     * @return 재입력을 선택한 y이면 true, 작업 취소를 선택한 n이면 false
     */
    public boolean parseRetry(String raw) {
        String value = requirePattern(raw, InputPatterns.RETRY, ErrorCode.RETRY_SYNTAX);
        return value.equals("y");
    }

    /**
     * 양 끝의 표준 공백을 제거하고 상품명의 허용 문자와 길이를 검사
     * @param raw 상품명으로 입력한 원문 문자열, 내부의 연속 공백은 유지
     * @return 상품명
     */
    public String parseName(String raw) {
        return requirePattern(raw, InputPatterns.NAME, ErrorCode.NAME_SYNTAX);
    }

    /**
     * 논리코드의 문법을 검사한 뒤 번호 범위를 검사
     * @param raw 상품 선택 단계에서 입력한 원문 문자열, 공백 제거 후 대문자 P와 숫자 다섯 자리여야 함
     * @return P00001~P99999 형식의 논리코드 문자열
     */
    public String parseLogicalCode(String raw) {
        String value = requirePattern(raw, InputPatterns.LOGICAL_CODE, ErrorCode.CODE_SYNTAX);
        int number = Integer.parseInt(value.substring(1));
        if (number == 0) {
            throw new UserInputException(ErrorCode.CODE_RANGE);
        }
        return value;
    }

    /**
     * 공통 숫자 문법을 검사하고 선행 0을 허용하여 십진 정수로 해석합, 필드별 범위는 검사하지 안함
     * @param raw 양 끝의 표준 공백 제거 후 ASCII 숫자 1~10자리로 구성되어야 하는 원문 입력
     * @param field 오류 코드를 구분할 숫자 입력 항목, SIZE, PRICE, INBOUND_QUANTITY, SALE_QUANTITY 중 하나
     * @return 입력 문자열을 해석한 0~9999999999 사이의 long 값
     */
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

    /**
     * 숫자 문법을 검사한 뒤 상품 크기의 허용 범위를 검사
     * @param raw 상품 크기로 입력한 원문 문자열입니다. 선행 0은 허용
     * @return 1~1000 용량 단위의 상품 크기
     */
    public int parseSize(String raw) {
        long value = parseUnsignedDecimal(raw, Field.SIZE);
        if (value < Limits.MIN_SIZE || value > Limits.MAX_SIZE) {
            throw new UserInputException(ErrorCode.SIZE_RANGE);
        }
        return (int) value;
    }

    /**
     * 숫자 문법, 가격 범위, 10원 단위 조건 순서로 검사
     * @param raw 판매 가격으로 입력한 원문 문자열, 쉼표와 단위 문자는 허용안함
     * @return 10~10000000원 사이의 10의 배수인 판매 가격
     */
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

    /**
     * 숫자 문법을 검사한 뒤 한 번의 요청 수량 범위를 검사
     * @param raw 입고 또는 판매 수량으로 입력한 원문 문자열
     * @param field 수량의 용도를 나타내는 INBOUND_QUANTITY 또는 SALE_QUANTITY
     * @return 1~100개 사이의 입고 또는 판매 요청 수량
     */
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

    /**
     * 양 끝의 표준 공백을 제거한 문자열 전체가 지정된 정규식과 일치하는지 검사
     * @param raw 검사할 null이 아닌 원문 입력 문자열
     * @param pattern 문자열 전체에 적용할 정규식
     * @param code 정규식과 일치하지 않을 때 전달할 사용자 입력 오류 코드
     * @return 정규식 검사를 통과하고 양 끝의 표준 공백을 제거한 문자열
     */
    private String requirePattern(String raw, String pattern, ErrorCode code) {
        String value = normalizeSpaces(raw);
        if (!value.matches(pattern)) {
            throw new UserInputException(code);
        }
        return value;
    }
}
