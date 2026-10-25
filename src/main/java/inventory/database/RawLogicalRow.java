package inventory.database;

/**
 * 파일 문법 검사를 통과하고 의미 검사 전인 논리 행을 보관하며, 범위 오류 값도 그대로 유지
 * @param name 파일 문법을 통과한 상품명
 * @param code 대문자 P와 숫자 다섯 자리로 구성된 코드, P00000도 포함될 수 있음
 * @param size 파일의 크기 문법을 통과한 정수, 1001 등의 의미 오류 값도 포함될 수 있음
 * @param price 파일의 가격 문법을 통과한 정수, 범위나 단위 오류 값도 포함될 수 있음
 */
public record RawLogicalRow(
        String name,
        String code,
        int size,
        int price
) {}
