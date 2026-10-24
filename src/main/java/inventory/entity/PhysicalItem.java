package inventory.entity;

import inventory.validation.DomainRules;
/**
 * logicalCode: 유효 논리코드.
 * suffix: 1~999.
 * sold: false=미판매(0), true=판매 완료(1).
 * 입력 또는 시작 파일 검사를 통과한 불변 객체입니다. 생성자에서 다시 검사하지 않습니다.
 * 판매된 낱개도 유지하며 접미번호를 재사용하지 않습니다.
 */
public record PhysicalItem(
        String logicalCode,
        int suffix,
        boolean sold
) {
    /**
     * DomainRules.formatSuffix(suffix)와 논리코드를 조합합니다.
     * @return P00001-001 형태의 전체 물리코드
     */
    public String physicalCode() {
        return logicalCode + "-" + DomainRules.formatSuffix(suffix);
    }

    /**
     * 서비스에서 선택한 미판매 낱개를 판매 완료 상태로 만듭니다.
     * @return 판매 완료 상태의 새 객체
     */
    public PhysicalItem markSold() {
        return new PhysicalItem(logicalCode, suffix, true);
    }
}
