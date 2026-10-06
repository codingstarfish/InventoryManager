package inventory.database;

import java.util.List;

/**
 * logicalRows: 논리 파일의 문법 통과 행.
 * physicalRows: 물리 파일의 문법 통과 행.
 * 양쪽 파일 문법 검사가 끝난 뒤 생성. 행 번호·원래 순서를 보존하고 목록 불변 복사. 의미 검사는 하지 않습니다.
 */
public record ParsedFiles(
        List<RawLogicalRow> logicalRows,
        List<RawPhysicalRow> physicalRows
) {
    /**
     * 생성 시 null·값 범위·관계 조건을 검사합니다. 컬렉션은 불변 복사합니다.
     * 양쪽 파일 문법 검사가 끝난 뒤 생성. 행 번호·원래 순서를 보존하고 목록 불변 복사. 의미 검사는 하지 않습니다.
     * @param logicalRows 논리 파일의 문법 통과 행
     * @param physicalRows 물리 파일의 문법 통과 행
     * @throws IllegalArgumentException 값이나 관계 조건 위반
     * @throws NullPointerException 필드 또는 컬렉션 원소가 null
     */
    public ParsedFiles {
        logicalRows = List.copyOf(logicalRows);
        physicalRows = List.copyOf(physicalRows);
    }
}
