package inventory.entity;

/**
 * code: P00001~P99999.
 * name: 양끝 공백 없는 허용 문자 1~30자.
 * size: 1~1000.
 * price: 10~10000000원, 10의 배수.
 * DomainRules로 검증. 코드·상품명·크기·가격 순서이며 파일 필드 순서와 다릅니다.
 */
public record LogicalItem(
        String code,
        String name,
        int size,
        int price
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다.
     * DomainRules로 검증. 코드·상품명·크기·가격 순서이며 파일 필드 순서와 다릅니다.
     * @param code P00001~P99999
     * @param name 양끝 공백 없는 허용 문자 1~30자
     * @param size 1~1000
     * @param price 10~10000000원, 10의 배수
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드가 null
     */
    public LogicalItem {
        // TODO: 위 생성자 검증을 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
