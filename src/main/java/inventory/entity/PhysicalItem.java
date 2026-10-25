package inventory.entity;

import inventory.validation.DomainRules;
/**
 * 낱개 상품 정보를 보관
 * @param logicalCode 낱개가 속한 등록 상품의 논리코드
 * @param suffix 해당 상품에서 발급한 1~999 사이의 접미번호
 * @param sold 미판매이면 false이며 판매 완료이면 true인 상태
 */
public record PhysicalItem(
        String logicalCode,
        int suffix,
        boolean sold
) {
    /**
     * 논리코드와 하이픈 및 세 자리 접미번호를 조합합니다.
     * @return P00001-001과 같은 총 10자의 전체 물리코드 문자열
     */
    public String physicalCode() {
        return logicalCode + "-" + DomainRules.formatSuffix(suffix);
    }

    /**
     * 현재 낱개와 동일한 코드·접미번호를 가지며 판매여부가 true인 객체를 생성
     * @return 판매 완료 상태의 새 낱개 객체
     */
    public PhysicalItem markSold() {
        return new PhysicalItem(logicalCode, suffix, true);
    }
}
