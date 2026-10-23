package inventory.database;

/**
 * logicalCode: 문법 통과한 논리코드.
 * suffix: 세 자리 숫자를 해석한 0~999.
 * sold: 파일 0=false, 1=true.
 * Entity가 아닌 문법 단계 중간 행. P00000, suffix=0도 의미 검사 전 허용.
 * 문법 검사는 EntityParser, 의미 검사는 FileIntegrityValidator가 담당합니다.
 */
public record RawPhysicalRow(
        String logicalCode,
        int suffix,
        boolean sold
) {}
