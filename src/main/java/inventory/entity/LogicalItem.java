package inventory.entity;

/**
 * 등록된 상품 정보. 값은 입력/시작 검사에서 확인
 * @param code 상품을 식별하는 논리코드(P00001~P99999)
 * @param name 상품명(1~30자)
 * @param size 낱개 하나가 차지하는 크기(1~1000 용량 단위)
 * @param price 낱개 하나의 가격(10~10000000원이며 10의 배수)
 */
public record LogicalItem(
        String code,
        String name,
        int size,
        int price
) {
}
