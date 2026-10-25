package inventory.entity;

import inventory.validation.DomainRules;

// 낱개 정보 보관
public record PhysicalItem(
        String logicalCode,
        int suffix,
        boolean sold
) {
    
    // {논리코드}-{접미번호} 조합
    public String physicalCode() {
        return logicalCode + "-" + DomainRules.formatSuffix(suffix);
    }

    // 판매여부 true인 객체 생성해서 리턴
    public PhysicalItem markSold() {
        return new PhysicalItem(logicalCode, suffix, true);
    }
}
