package inventory.database;

import inventory.literal.InputPatterns;
import inventory.literal.ErrorCode;
import inventory.literal.DataPaths;
import java.util.OptionalInt;
import java.util.List;
import java.util.Objects;
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
        Objects.requireNonNull(line, "line");
        if (lineNo < 1) {
            throw new IllegalArgumentException("행 번호는 1 이상이어야 합니다.");
        }
        String file = DataPaths.LOGICAL_FILE;
        if (line.isEmpty()) {
            throw syntaxError(file, ErrorCode.FILE_EMPTY_LINE, "빈 행은 허용하지 않습니다.", lineNo);
        }
        String[] fields = line.split(",", -1);
        if (fields.length != 4) {
            throw syntaxError(file, ErrorCode.FILE_LOGICAL_FIELDS, "논리 파일의 각 행은 4개 필드여야 합니다.", lineNo);
        }
        if (!fields[0].matches(InputPatterns.NAME) || fields[0].startsWith(" ") || fields[0].endsWith(" ")) {
            throw syntaxError(file, ErrorCode.FILE_NAME_SYNTAX,
                    "상품명은 완성형 한글·영문·숫자·표준 공백으로 구성된 1~30자이며 양 끝에 공백이 없어야 합니다.", lineNo);
        }
        checkPattern(fields[1], InputPatterns.LOGICAL_CODE, file, ErrorCode.FILE_CODE_SYNTAX,
                "논리코드는 대문자 P와 숫자 5자리여야 합니다.", lineNo);
        checkPattern(fields[2], InputPatterns.FILE_SIZE, file, ErrorCode.FILE_SIZE_SYNTAX,
                "크기는 첫 문자가 1~9인 숫자 1~4자리여야 합니다.", lineNo);
        checkPattern(fields[3], InputPatterns.FILE_PRICE, file, ErrorCode.FILE_PRICE_SYNTAX,
                "가격은 첫 문자가 1~9인 숫자 1~8자리여야 합니다.", lineNo);
        return new RawLogicalRow(lineNo, fields[0], fields[1], Integer.parseInt(fields[2]), Integer.parseInt(fields[3]));
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
        Objects.requireNonNull(line, "line");
        if (lineNo < 1) {
            throw new IllegalArgumentException("행 번호는 1 이상이어야 합니다.");
        }
        String file = DataPaths.PHYSICAL_FILE;
        if (line.isEmpty()) {
            throw syntaxError(file, ErrorCode.FILE_EMPTY_LINE, "빈 행은 허용하지 않습니다.", lineNo);
        }
        String[] fields = line.split(",", -1);
        if (fields.length != 3) {
            throw syntaxError(file, ErrorCode.FILE_PHYSICAL_FIELDS, "물리 파일의 각 행은 3개 필드여야 합니다.", lineNo);
        }
        checkPattern(fields[0], InputPatterns.LOGICAL_CODE, file, ErrorCode.FILE_CODE_SYNTAX,
                "논리코드는 대문자 P와 숫자 5자리여야 합니다.", lineNo);
        checkPattern(fields[1], InputPatterns.FILE_SUFFIX, file, ErrorCode.FILE_SUFFIX_SYNTAX,
                "접미번호는 숫자 세 자리여야 합니다.", lineNo);
        checkPattern(fields[2], InputPatterns.FILE_SOLD, file, ErrorCode.FILE_SOLD_SYNTAX,
                "판매여부는 0 또는 1 한 자리여야 합니다.", lineNo);
        return new RawPhysicalRow(lineNo, fields[0], Integer.parseInt(fields[1]), fields[2].equals("1"));
    }

    private void checkPattern(String value, String pattern, String file, ErrorCode code, String reason, int lineNo) {
        if (!value.matches(pattern)) {
            throw syntaxError(file, code, reason, lineNo);
        }
    }

    private StartupDataException syntaxError(String file, ErrorCode code, String reason, int lineNo) {
        return new StartupDataException(List.of(file), code, reason, OptionalInt.of(lineNo));
    }
}
