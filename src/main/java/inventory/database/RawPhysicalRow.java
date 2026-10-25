package inventory.database;

/**
 * 파일 문법 검사를 통과하고 의미 검사 전인 물리 행을 보관합니다. 등록 참조와 FIFO는 아직 검증하지 안한상태
 * @param logicalCode 대문자 P와 숫자 다섯 자리로 구성된 코드로, 미등록 코드나 P00000도 포함될 수 있음
 * @param suffix 파일의 세 자리 숫자를 해석한 0~999 사이의 값으로, 0은 의미 검사에서 거절
 * @param sold 파일의 판매여부가 0이면 false이며 1이면 true인 상태
 */
public record RawPhysicalRow(
        String logicalCode,
        int suffix,
        boolean sold
) {}
