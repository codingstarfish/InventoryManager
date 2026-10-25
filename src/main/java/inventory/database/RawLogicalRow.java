package inventory.database;

/**
 * 문법만 통과한 논리 파일 한 행
 * P00000, 크기 1001 같은 값도 담고, 의미 검사는 나중에 처리
 *
 * @param name 파일에 기록된 상품명
 * @param code 파일에 기록된 논리코드, P00000도 포함할 수 있음
 * @param size 낱개 한 개가 차지하는 크기, 허용 범위는 의미 검사에서 확인
 * @param price 상품 한 개의 가격(원), 허용 범위와 10원 단위 여부는 의미 검사에서 확인
 */
public record RawLogicalRow(
        String name,
        String code,
        int size,
        int price
) {}
