package inventory.entity;

import inventory.literal.Limits;
import inventory.validation.DomainRules;
/**
 * logicalCode: 유효 논리코드.
 * suffix: 1~999.
 * sold: false=미판매(0), true=판매 완료(1).
 * 객체는 불변입니다. 판매된 낱개도 유지하며 접미번호를 재사용하지 않습니다.
 */
public record PhysicalItem(
        String logicalCode,
        int suffix,
        boolean sold
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다.
     * 객체는 불변입니다. 판매된 낱개도 유지하며 접미번호를 재사용하지 않습니다.
     * @param logicalCode 유효 논리코드
     * @param suffix 1~999
     * @param sold false=미판매(0), true=판매 완료(1)
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드가 null
     */
    public PhysicalItem {
        DomainRules.validateLogicalCode(logicalCode);
        if (suffix < 1 || suffix > Limits.MAX_SUFFIX) {
            throw new IllegalArgumentException("접미번호 범위 위반");
        }
    }
    /**
     * DomainRules.formatSuffix(suffix)와 논리코드를 조합합니다.
     * @return P00001-001 형태의 전체 물리코드
     */
    public String physicalCode() {
        return logicalCode + "-" + DomainRules.formatSuffix(suffix);
    }

    /**
     * 원본을 바꾸지 않고 sold=true인 새 낱개를 만듭니다.
     * @return 판매 완료 상태의 새 객체
     * @throws IllegalStateException 이미 sold=true인 경우
     */
    public PhysicalItem markSold() {
        if (sold) {
            throw new IllegalStateException("이미 판매한 낱개입니다.");
        }
        return new PhysicalItem(logicalCode, suffix, true);
    }
}
