package inventory.database;

// 문법만 통과한 물리 파일 한 행. 접미번호 000도 여기서는 허용
// 등록 여부와 FIFO는 나중에 의미 검사에서 확인
public record RawPhysicalRow(
        String logicalCode,
        int suffix,
        boolean sold
) {}
