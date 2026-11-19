package inventory.database;

import inventory.literal.InputPatterns;
import inventory.literal.DataPaths;

import java.util.List;

import inventory.exception.StartupDataException;

// 파일 한 줄의 형식만 검사. 값과 파일 간 관계는 FileIntegrityValidator에서 확인
public class EntityParser {

    public EntityParser() {
    }

    /**
     * 상품명, 논리코드, 크기, 가격 순서로 문법 검사
     * P00000, 크기 1001, 가격 1 같은 값은 의미 검사에서 걸러짐
     * 문법이 틀리면 StartupDataException
     *
     * @param line 논리 파일에서 읽은 개행 문자를 제외한 한 행
     * @return 문법 검사를 통과한 상품명·논리코드·크기·가격(원) 데이터
     * @throws StartupDataException 빈 행이거나 필드 수·상품명·논리코드·크기·가격 형식이 잘못된 경우
     */
    public RawLogicalRow parseLogicalSyntax(String line) {
        String file = DataPaths.LOGICAL_FILE;
        if (line.isEmpty()) {
            throw syntaxError(file, "빈 행은 허용하지 않습니다.");
        }
        String[] fields = line.split(",", -1);
        if (fields.length != 5) {
            throw syntaxError(file, "논리 파일의 각 행은 5개 필드여야 합니다.");
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
        checkPattern(fields[4], "0|[1-9][0-9]{0,3}", file, "유통기한 일수는 0이거나 첫 문자가 1~9인 숫자 1~4자리여야 합니다.");
        return new RawLogicalRow(fields[0], fields[1], Integer.parseInt(fields[2]), Integer.parseInt(fields[3]), Integer.parseInt(fields[4]));
    }

    /**
     * 논리코드, 접미번호, 판매여부 순서로 문법 검사
     * 상태는 0/1/2로 변환. 날짜는 원문으로 보존하고 의미 검사에서 확인
     *
     * @param line 물리 파일에서 읽은 개행 문자를 제외한 한 행
     * @return 문법 검사를 통과한 논리코드·접미번호·판매여부 데이터
     * @throws StartupDataException 빈 행이거나 필드 수·논리코드·접미번호·판매여부 형식이 잘못된 경우
     */
    public RawPhysicalRow parsePhysicalSyntax(String line) {
        String file = DataPaths.PHYSICAL_FILE;
        if (line.isEmpty()) {
            throw syntaxError(file, "빈 행은 허용하지 않습니다.");
        }
        String[] fields = line.split(",", -1);
        if (fields.length != 6) {
            throw syntaxError(file, "물리 파일의 각 행은 6개 필드여야 합니다.");
        }
        checkPattern(fields[0], InputPatterns.LOGICAL_CODE, file,
                "논리코드는 대문자 P와 숫자 5자리여야 합니다.");
        checkPattern(fields[1], InputPatterns.FILE_SUFFIX, file,
                "접미번호는 숫자 세 자리여야 합니다.");
        checkPattern(fields[2], "[012]", file,
                "상태는 0, 1, 2 중 한 자리여야 합니다.");
        checkPattern(fields[3], "[0-9]{4}-[0-9]{2}-[0-9]{2}", file, "생산일은 YYYY-MM-DD 형식이어야 합니다.");
        checkPattern(fields[4], "-|[0-9]{4}-[0-9]{2}-[0-9]{2}", file, "판매일은 YYYY-MM-DD 형식이거나 하이픈(-) 한 개여야 합니다.");
        checkPattern(fields[5], "[01]", file, "환불이력은 0 또는 1 한 자리여야 합니다.");
        return new RawPhysicalRow(fields[0], Integer.parseInt(fields[1]), inventory.entity.ItemState.values()[Integer.parseInt(fields[2])], fields[3], fields[4], fields[5].equals("1"));
    }

    /**
     * 공백 제거 없이 필드 전체를 검사. 정규식과 안 맞으면 파일 오류
     *
     * @param value   검사할 필드의 원문 값
     * @param pattern 필드 전체가 일치해야 하는 정규식
     * @param file    오류가 발생한 데이터 파일명
     * @param reason  정규식 불일치 시 전달할 오류 설명
     * @throws StartupDataException 필드 전체가 지정한 정규식과 일치하지 않는 경우
     */
    public String parseDateSyntax(List<String> lines) {
        if (lines.size() > 1) throw syntaxError(DataPaths.DATE_FILE, "날짜 파일은 최대 한 행이어야 합니다.");
        if (lines.isEmpty()) return null;
        checkPattern(lines.get(0), "[0-9]{4}-[0-9]{2}-[0-9]{2}", DataPaths.DATE_FILE, "가상날짜는 YYYY-MM-DD 형식이어야 합니다.");
        return lines.get(0);
    }

    private void checkPattern(String value, String pattern, String file, String reason) {
        if (!value.matches(pattern)) {
            throw syntaxError(file, reason);
        }
    }

    /**
     * @param file   문법 오류가 발생한 데이터 파일명
     * @param reason 문법 오류의 원인 설명
     * @return 해당 파일명과 원인을 담은 시작 데이터 예외
     */
    private StartupDataException syntaxError(String file, String reason) {
        return new StartupDataException(List.of(file), reason);
    }
}
