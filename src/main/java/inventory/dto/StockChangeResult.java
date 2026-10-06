package inventory.dto;

import java.util.List;

/**
 * logicalCode: 변경 대상 논리코드.
 * quantity: 실제 입고 또는 판매 수량 1~100.
 * currentAfter: 변경 후 현재 재고 0~999.
 * affectedCodes: 변경된 전체 물리코드 목록.
 * affectedCodes는 대상 상품 코드이며 접미번호순·중복 없음, 길이=quantity, 불변 복사.
 * 정상 화면에는 affectedCodes를 추가 출력하지 않습니다.
 */
public record StockChangeResult(
        String logicalCode,
        int quantity,
        int currentAfter,
        List<String> affectedCodes
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다. 컬렉션은 불변 복사합니다.
     * affectedCodes는 대상 상품 코드이며 접미번호순·중복 없음, 길이=quantity, 불변 복사.
     * 정상 화면에는 affectedCodes를 추가 출력하지 않습니다.
     * @param logicalCode 변경 대상 논리코드
     * @param quantity 실제 입고 또는 판매 수량 1~100
     * @param currentAfter 변경 후 현재 재고 0~999
     * @param affectedCodes 변경된 전체 물리코드 목록
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드 또는 컬렉션 원소가 null
     */
    public StockChangeResult {
        // TODO: 위 생성자 검증과 필요한 불변 복사를 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
