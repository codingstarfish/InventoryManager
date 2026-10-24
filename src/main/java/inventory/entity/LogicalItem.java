package inventory.entity;

/**
 * 입력 또는 시작 검사를 통과한 상품 정보를 보관
 * @param code P00001~P99999 논리코드
 * @param name 1~30자의 상품명입니다
 * @param size 1~1000 용량 단위의 낱개 크기
 * @param price 10~10000000원 사이의 10원 단위 가격
 */
public record LogicalItem(
        String code,
        String name,
        int size,
        int price
) {
}
