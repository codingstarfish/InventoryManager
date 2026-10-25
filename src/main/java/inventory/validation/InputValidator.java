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
     * 양 끝의 일반 공백(U+0020)만 제거. 탭은 남김
     *
     * @param raw 정리할 사용자 입력 원문
     * @return 양 끝의 일반 공백을 제거한 문자열
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
     * 주 메뉴: 1~5
     *
     * @param raw 사용자가 입력한 주 메뉴 번호 원문
     * @return 검증된 주 메뉴 번호(1~5)
     * @throws UserInputException 공백 정리 후 1~5 중 한 자리가 아니면 {@code MENU_SYNTAX}
     */
    public int parseMainMenu(String raw) {
        String value = requirePattern(raw, InputPatterns.MAIN_MENU, ErrorCode.MENU_SYNTAX);
        return value.charAt(0) - '0';
    }

    
    /**
     * 조회 방식: 1은 전체, 2는 단일 상품
     *
     * @param raw 사용자가 입력한 조회 방식 번호 원문
     * @return 전체 조회는 1, 단일 상품 조회는 2
     * @throws UserInputException 공백 정리 후 1 또는 2가 아니면 {@code QUERY_MODE_SYNTAX}
     */
    public int parseQueryMode(String raw) {
        String value = requirePattern(raw, InputPatterns.QUERY_MODE, ErrorCode.QUERY_MODE_SYNTAX);
        return value.charAt(0) - '0';
    }

    /**
     * 소문자 y/n만 허용. y면 true
     *
     * @param raw 사용자가 입력한 재시도 여부 원문
     * @return y이면 true, n이면 false
     * @throws UserInputException 공백 정리 후 소문자 y 또는 n이 아니면 {@code RETRY_SYNTAX}
     */
    public boolean parseRetry(String raw) {
        String value = requirePattern(raw, InputPatterns.RETRY, ErrorCode.RETRY_SYNTAX);
        return value.equals("y");
    }

    /**
     * 상품명: 허용 문자로 1~30자. 내부 공백은 유지
     *
     * @param raw 사용자가 입력한 상품명 원문
     * @return 양 끝의 일반 공백을 제거하고 형식을 검증한 상품명
     * @throws UserInputException 공백 정리 후 완성형 한글·영문·숫자·공백으로 구성된
     *         1~30자 상품명이 아니면 {@code NAME_SYNTAX}
     */
    public String parseName(String raw) {
        return requirePattern(raw, InputPatterns.NAME, ErrorCode.NAME_SYNTAX);
    }

    /**
     * P + 숫자 다섯 자리인지 검사한 뒤 P00000 거절
     *
     * @param raw 사용자가 입력한 논리코드 원문
     * @return 양 끝의 일반 공백을 제거한 P00001~P99999 형식의 논리코드
     * @throws UserInputException 공백 정리 후 대문자 P와 숫자 다섯 자리 형식이 아니면
     *         {@code CODE_SYNTAX}, 번호가 00000이면 {@code CODE_RANGE}
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
     * 숫자 1~10자리와 선행 0 허용. 부호나 소수점은 불가
     * 최대 9999999999까지 읽어야 하므로 long 사용. 범위는 각 항목에서 검사
     *
     * @param raw 사용자가 입력한 정수 원문
     * @param field 형식 오류 코드를 선택할 항목(SIZE, PRICE, INBOUND_QUANTITY, SALE_QUANTITY)
     * @return 숫자 형식을 검증한 0~9999999999의 정수. 항목별 범위 검사는 별도 수행
     * @throws UserInputException 공백 정리 후 숫자 1~10자리가 아니면 항목에 따라
     *         {@code SIZE_SYNTAX}, {@code PRICE_SYNTAX}, {@code INBOUND_SYNTAX} 또는 {@code SALE_SYNTAX}
     * @throws IllegalArgumentException 항목이 SIZE, PRICE, INBOUND_QUANTITY, SALE_QUANTITY 중 하나가 아닌 경우
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
        return Long.parseLong(value);
    }

    /**
     * 숫자 형식 검사 후 1~1000 범위 확인
     *
     * @param raw 사용자가 입력한 상품 한 개의 크기 원문
     * @return 검증된 상품 크기(1~1000 용량 단위)
     * @throws UserInputException 공백 정리 후 숫자 1~10자리가 아니면 {@code SIZE_SYNTAX},
     *         값이 1~1000 범위를 벗어나면 {@code SIZE_RANGE}
     */
    public int parseSize(String raw) {
        long value = parseUnsignedDecimal(raw, Field.SIZE);
        if (value < Limits.MIN_SIZE || value > Limits.MAX_SIZE) {
            throw new UserInputException(ErrorCode.SIZE_RANGE);
        }
        return (int) value;
    }

    /**
     * 숫자 형식 → 10~10000000 범위 → 10의 배수 순서로 검사
     *
     * @param raw 사용자가 입력한 상품 가격 원문
     * @return 10원 단위로 검증된 상품 가격(10~10000000원)
     * @throws UserInputException 공백 정리 후 숫자 1~10자리가 아니면 {@code PRICE_SYNTAX},
     *         값이 10~10000000 범위를 벗어나면 {@code PRICE_RANGE}, 10의 배수가 아니면 {@code PRICE_UNIT}
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
     * 입고/판매 수량: 숫자 형식 검사 후 1~100 범위 확인
     *
     * @param raw 사용자가 입력한 입고 또는 판매 수량 원문
     * @param field 오류 코드를 선택할 수량 항목(INBOUND_QUANTITY 또는 SALE_QUANTITY)
     * @return 검증된 입고 또는 판매 수량(1~100개)
     * @throws UserInputException 공백 정리 후 숫자 1~10자리가 아니면 항목에 따라
     *         {@code INBOUND_SYNTAX} 또는 {@code SALE_SYNTAX}, 값이 1~100 범위를 벗어나면
     *         {@code INBOUND_RANGE} 또는 {@code SALE_RANGE}
     * @throws IllegalArgumentException 항목이 INBOUND_QUANTITY 또는 SALE_QUANTITY가 아닌 경우
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
     * 양 끝의 일반 공백을 제거한 뒤 문자열 전체가 정규식과 맞는지 확인
     *
     * @param raw 형식을 검사할 사용자 입력 원문
     * @param pattern 입력 문자열 전체에 적용할 정규식
     * @param code 형식이 맞지 않을 때 사용할 입력 오류 코드
     * @return 양 끝의 일반 공백을 제거하고 정규식 검증을 통과한 문자열
     * @throws UserInputException 공백 정리 후 입력 문자열 전체가 정규식과 맞지 않는 경우. 전달받은 오류 코드 사용
     */
    private String requirePattern(String raw, String pattern, ErrorCode code) {
        String value = normalizeSpaces(raw);
        if (!value.matches(pattern)) {
            throw new UserInputException(code);
        }
        return value;
    }
}
