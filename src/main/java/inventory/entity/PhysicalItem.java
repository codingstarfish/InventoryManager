package inventory.entity;

import inventory.validation.DomainRules;

/**
 * 낱개 정보 보관
 * @param logicalCode 낱개가 속한 등록 상품의 논리코드(P00001~P99999)
 * @param suffix 해당 상품에서 발급한 접미번호(1~999)
 * @param sold 판매 완료이면 true, 미판매이면 false
 */
public record PhysicalItem(
        String logicalCode,
        int suffix,
        boolean sold
) {
    
    /**
     * {논리코드}-{접미번호} 조합
     * @return P00001-001처럼 접미번호를 세 자리로 맞춘 전체 물리코드
     */
    public String physicalCode() {
        return logicalCode + "-" + DomainRules.formatSuffix(suffix);
    }

    /**
     * 판매여부 true인 객체 생성해서 리턴
     * @return 현재 논리코드와 접미번호를 유지한 판매 완료 상태의 새 낱개 객체
     */
    public PhysicalItem markSold() {
        return new PhysicalItem(logicalCode, suffix, true);
    }
}
