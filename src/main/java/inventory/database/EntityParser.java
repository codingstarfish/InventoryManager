package inventory.database;

import inventory.literal.InputPatterns;
import inventory.literal.DataPaths;
import java.util.List;
import inventory.exception.StartupDataException;

/**
 * 문법 단계 전용 순수 변환기. Entity를 생성해 의미 검사 우선순위 유지시킴
 */
public class EntityParser {

    public EntityParser() {
    }

    /**
     * 빈 행과 필드 수를 확인하고 상품명, 논리코드, 크기, 가격 순서로 문법을 검사하되 값의 범위나 관계는 검사 안함
     * @param line 개행을 제외한 논리 파일의 원문 한 행, 필드 순서는 상품명,논리코드,크기,가격
     * @return 상품명,논리코드,크기,가격을 담은 중간 행 객체로, P00000, 크기 1001, 가격 1 등 의미 검사가 필요한 값도 유지
     * @throws StartupDataException 파일 생성·접근 또는 문법·의미 검사에서 오류를 확인하면 던짐
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
     * 빈 행과 필드 수를 확인하고 논리코드, 세 자리 접미번호, 판매여부의 문법을 검사, 등록 참조와 FIFO는 검사하지 안함
     * @param line 개행을 제외한 물리 파일의 원문 한 행, 필드 순서는 논리코드,접미번호,판매여부
     * @return 논리코드,접미번호,판매 상태를 담은 중간 행 객체, 판매여부 0은 false, 1은 true로 변환하며 접미번호 000도 의미 검사 전까지 유지
     * @throws StartupDataException 파일 생성,접근 및 문법,의미검사에서 오류를 확인시 던짐
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

    /**
     * 필드 문자열 전체가 정규식과 일치하는지 검사하고 불일치 시 시작 검사 예외를 전달
     * return void
     * @param value 공백을 제거하지 않은 파일의 필드 문자열
     * @param pattern 필드의 허용 문자와 자릿수를 나타내는 정규식
     * @param file 오류 메시지에 표시할 데이터 파일명
     * @param reason 문법이 일치하지 않을 때 표시할 허용 규칙
     */
    private void checkPattern(String value, String pattern, String file, String reason) {
        if (!value.matches(pattern)) {
            throw syntaxError(file, reason);
        }
    }

    /**
     * 파일명과 문법 오류 이유를 담은 예외를 생성
     * @param file 문법 오류가 발생한 데이터 파일의 이름
     * @param reason 위반한 문법 규칙을 설명하는 안내 문구
     * @return 호출한 위치에서 던질 시작 검사 예외 객체
     */
    private StartupDataException syntaxError(String file, String reason) {
        return new StartupDataException(List.of(file), reason);
    }
}
