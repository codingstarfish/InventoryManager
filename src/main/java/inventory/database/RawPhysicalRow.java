package inventory.database;

/**
 * 문법만 통과한 물리 파일 한 행. 접미번호 000도 여기서는 허용
 * 등록 여부와 FIFO는 나중에 의미 검사에서 확인
 *
 * @param logicalCode 낱개가 속한 상품의 논리코드, 등록 여부는 의미 검사에서 확인
 * @param suffix 세 자리 접미번호를 정수로 변환한 값, 0도 포함할 수 있음
 * @param sold 판매 완료면 true, 미판매면 false
 */
public record RawPhysicalRow(
        String logicalCode,
        int suffix,
        boolean sold
) {}
