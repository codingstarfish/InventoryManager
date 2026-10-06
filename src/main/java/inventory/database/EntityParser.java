package inventory.database;

import inventory.exception.StartupDataException;

/**
 * 문법 단계 전용 순수 변환기. Entity를 생성해 의미 검사 우선순위를 바꾸지 않습니다.
 */
public class EntityParser {
    /** 상태 없는 파일 문법 변환기를 생성합니다. */
    public EntityParser() {
    }

    /**
     * 빈 행 → split(",", -1) 필드 수 4 → 이름·코드·크기·가격 문법 순 검사.
     * name,code,size,price 순. 이름 양끝 U+0020 금지.
     * 파일 크기 [1-9][0-9]{0,3}, 가격 [1-9][0-9]{0,7}은 int 해석 가능.
     * P00000, 크기 1001, 가격 1은 의미 단계 전까지 허용. 헤더·주석 무시 금지.
     * @param line 공백 제거하지 않은 원문 행
     * @param lineNo 1부터 시작하는 원본 행 번호
     * @return 문법만 통과한 RawLogicalRow
     * @throws StartupDataException FILE_EMPTY_LINE, FILE_LOGICAL_FIELDS 또는 해당 FILE_*_SYNTAX
     */
    public RawLogicalRow parseLogicalSyntax(String line, int lineNo) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 빈 행 → split(",", -1) 필드 수 3 → P[0-9]{5}, [0-9]{3}, [01] 검사.
     * 판매 0=false, 1=true. 접미번호 000은 의미 단계 전까지 허용.
     * 등록 존재·FIFO 검사를 여기서 하지 않습니다.
     * @param line 공백 제거하지 않은 원문 행
     * @param lineNo 1부터 시작하는 원본 행 번호
     * @return 문법만 통과한 RawPhysicalRow
     * @throws StartupDataException FILE_EMPTY_LINE, FILE_PHYSICAL_FIELDS 또는 해당 FILE_*_SYNTAX
     */
    public RawPhysicalRow parsePhysicalSyntax(String line, int lineNo) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
