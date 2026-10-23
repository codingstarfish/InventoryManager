package inventory.database;

import inventory.literal.InputPatterns;
import inventory.literal.DataPaths;
import java.util.List;
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
     * @return 문법만 통과한 RawLogicalRow
     * @throws StartupDataException 빈 행, 필드 수 또는 각 필드의 문법 오류
     */
    public RawLogicalRow parseLogicalSyntax(String line) {
        String file = DataPaths.LOGICAL_FILE;
        if (line.isEmpty()) {
            throw syntaxError(file, "빈 행은 허용하지 않습니다.");
        }
        String[] fields = line.split(",", -1);
        if (fields.length != 4) {
            throw syntaxError(file, "논리 파일의 각 행은 4개 필드여야 합니다.");
        }
        if (!fields[0].matches(InputPatterns.NAME) || fields[0].startsWith(" ") || fields[0].endsWith(" ")) {
            throw syntaxError(file,
                    "상품명은 완성형 한글, 영문, 숫자, 표준 공백으로 구성된 1~30자이며 양 끝에 공백이 없어야 합니다.");
        }
        checkPattern(fields[1], InputPatterns.LOGICAL_CODE, file,
                "논리코드는 대문자 P와 숫자 5자리여야 합니다.");
        checkPattern(fields[2], InputPatterns.FILE_SIZE, file,
                "크기는 첫 문자가 1~9인 숫자 1~4자리여야 합니다.");
        checkPattern(fields[3], InputPatterns.FILE_PRICE, file,
                "가격은 첫 문자가 1~9인 숫자 1~8자리여야 합니다.");
        return new RawLogicalRow(fields[0], fields[1], Integer.parseInt(fields[2]), Integer.parseInt(fields[3]));
    }

    /**
     * 빈 행 → split(",", -1) 필드 수 3 → P[0-9]{5}, [0-9]{3}, [01] 검사.
     * 판매 0=false, 1=true. 접미번호 000은 의미 단계 전까지 허용.
     * 등록 존재·FIFO 검사를 여기서 하지 않습니다.
     * @param line 공백 제거하지 않은 원문 행
     * @return 문법만 통과한 RawPhysicalRow
     * @throws StartupDataException 빈 행, 필드 수 또는 각 필드의 문법 오류
     */
    public RawPhysicalRow parsePhysicalSyntax(String line) {
        String file = DataPaths.PHYSICAL_FILE;
        if (line.isEmpty()) {
            throw syntaxError(file, "빈 행은 허용하지 않습니다.");
        }
        String[] fields = line.split(",", -1);
        if (fields.length != 3) {
            throw syntaxError(file, "물리 파일의 각 행은 3개 필드여야 합니다.");
        }
        checkPattern(fields[0], InputPatterns.LOGICAL_CODE, file,
                "논리코드는 대문자 P와 숫자 5자리여야 합니다.");
        checkPattern(fields[1], InputPatterns.FILE_SUFFIX, file,
                "접미번호는 숫자 세 자리여야 합니다.");
        checkPattern(fields[2], InputPatterns.FILE_SOLD, file,
                "판매여부는 0 또는 1 한 자리여야 합니다.");
        return new RawPhysicalRow(fields[0], Integer.parseInt(fields[1]), fields[2].equals("1"));
    }

    private void checkPattern(String value, String pattern, String file, String reason) {
        if (!value.matches(pattern)) {
            throw syntaxError(file, reason);
        }
    }

    private StartupDataException syntaxError(String file, String reason) {
        return new StartupDataException(List.of(file), reason);
    }
}
