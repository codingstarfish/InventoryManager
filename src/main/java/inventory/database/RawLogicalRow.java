package inventory.database;

/**
 * name: 문법 통과한 이름.
 * code: P와 숫자 다섯 자리.
 * size: 파일 문법 통과한 크기.
 * price: 파일 문법 통과한 가격.
 * Entity가 아닌 문법 단계 중간 행. P00000, size=1001, price=1도 의미 검사 전 허용.
 * 문법 검사는 EntityParser, 의미 검사는 FileIntegrityValidator가 담당합니다.
 */
public record RawLogicalRow(
        String name,
        String code,
        int size,
        int price
) {}
