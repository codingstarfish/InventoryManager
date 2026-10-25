package inventory.entity;

// 등록된 상품 정보. 값은 입력/시작 검사에서 확인
// code: P00001~P99999, name: 1~30자
// size: 1~1000, price: 10~10000000원이며 10의 배수
public record LogicalItem(
        String code,
        String name,
        int size,
        int price
) {
}
