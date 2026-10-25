package inventory.database;

import inventory.literal.InputPatterns;
import inventory.literal.DataPaths;
import java.util.List;
import inventory.exception.StartupDataException;

// 파일 한 줄의 형식만 검사. 값과 파일 간 관계는 FileIntegrityValidator에서 확인
public class EntityParser {

    public EntityParser() {
    }

    // 상품명, 논리코드, 크기, 가격 순서로 문법 검사
    // P00000, 크기 1001, 가격 1 같은 값은 의미 검사에서 걸러짐
    // 문법이 틀리면 StartupDataException
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

    // 논리코드, 접미번호, 판매여부 순서로 문법 검사
    // 판매여부는 0/1을 false/true로 변환. 접미번호 000은 의미 검사에서 거절
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

    // 공백 제거 없이 필드 전체를 검사. 정규식과 안 맞으면 파일 오류
    private void checkPattern(String value, String pattern, String file, String reason) {
        if (!value.matches(pattern)) {
            throw syntaxError(file, reason);
        }
    }

    private StartupDataException syntaxError(String file, String reason) {
        return new StartupDataException(List.of(file), reason);
    }
}
