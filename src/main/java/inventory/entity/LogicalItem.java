package inventory.entity;

/**
 * code: P00001~P99999.
 * name: 양끝 공백 없는 허용 문자 1~30자.
 * size: 1~1000.
 * price: 10~10000000원, 10의 배수.
 * 입력 또는 시작 파일 검사를 통과한 상품입니다. 생성자에서 다시 검사하지 않습니다.
 * 코드·상품명·크기·가격 순서이며 파일 필드 순서와 다릅니다.
 */
public record LogicalItem(
        String code,
        String name,
        int size,
        int price
) {
}
