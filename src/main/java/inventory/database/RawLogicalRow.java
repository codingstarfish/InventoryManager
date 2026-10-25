package inventory.database;

// 문법만 통과한 논리 파일 한 행
// P00000, 크기 1001 같은 값도 담고, 의미 검사는 나중에 처리
public record RawLogicalRow(
        String name,
        String code,
        int size,
        int price
) {}
