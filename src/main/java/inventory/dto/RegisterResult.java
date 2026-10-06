package inventory.dto;

import inventory.entity.LogicalItem;

/**
 * item: 저장을 완료한 신규 상품.
 * 저장·close·메모리 게시 후에만 반환. 초기 H=T=Q=0.
 */
public record RegisterResult(
        LogicalItem item
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다.
     * 저장·close·메모리 게시 후에만 반환. 초기 H=T=Q=0.
     * @param item 저장을 완료한 신규 상품
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드가 null
     */
    public RegisterResult {
        // TODO: 위 생성자 검증을 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
