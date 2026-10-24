package inventory.validation;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import inventory.literal.DataPaths;
import inventory.literal.Limits;
import inventory.database.RawPhysicalRow;
import inventory.database.RawLogicalRow;
import inventory.exception.StartupDataException;

/**
 * 시작 의미·관계 검사. 파일 접근·저장·상태 게시 없이 구체 오류 이유를 전달
 */
public class FileIntegrityValidator {

    public FileIntegrityValidator() {
    }

    /**
     * 두 파일의 문법 검사가 끝난 행에 대해 범위, 중복, 연속성, 등록 참조, FIFO와 창고 용량을 검사
     * @param logicalRows 논리 파일의 문법 검사를 통과한 전체 행 목록, 빈 목록도 허용하며 의미 오류 값은 포함될 수 있음
     * @param physicalRows 물리 파일의 문법 검사를 통과한 전체 행 목록, 판매된 행도 포함하며 빈 목록도 허용
     * @throws StartupDataException 파일 생성,접근 또는 문법,의미 검사에서 오류를 확인하면 전달
     */
    public void validate(List<RawLogicalRow> logicalRows, List<RawPhysicalRow> physicalRows) {
        Map<String, RawLogicalRow> logical = new TreeMap<>();
        for (RawLogicalRow row : logicalRows) {
            String file = DataPaths.LOGICAL_FILE;
            validateCode(row.code(), file);
            if (row.size() > Limits.MAX_SIZE) {
                throw dataError(file, "상품 크기는 1~1000 용량 단위여야 합니다.");
            }
            if (row.price() < Limits.MIN_PRICE || row.price() > Limits.MAX_PRICE) {
                throw dataError(file, "가격은 10~10000000원이어야 합니다.");
            }
            if (row.price() % Limits.PRICE_UNIT != 0) {
                throw dataError(file, "가격은 10원 단위여야 합니다.");
            }
            if (logical.containsKey(row.code())) {
                throw dataError(file, "논리코드가 중복되었습니다.");
            }
            logical.put(row.code(), row);
        }
        int number = 1;
        for (String code : logical.keySet()) {
            if (Integer.parseInt(code.substring(1)) != number++) {
                throw dataError(DataPaths.LOGICAL_FILE,
                        "논리코드가 P00001부터 중간 번호의 누락 없이 연속해야 합니다.");
            }
        }
        Map<String, List<RawPhysicalRow>> physical = new HashMap<>();
        Set<String> physicalCodes = new HashSet<>();
        for (RawPhysicalRow row : physicalRows) {
            String file = DataPaths.PHYSICAL_FILE;
            validateCode(row.logicalCode(), file);
            if (row.suffix() == 0) {
                throw dataError(file, "접미번호는 001~999여야 합니다.");
            }
            if (!logical.containsKey(row.logicalCode())) {
                throw dataError(file, "물리 파일의 논리코드가 논리 파일에 등록되어 있지 않습니다.");
            }
            String key = row.logicalCode() + "-" + row.suffix();
            if (!physicalCodes.add(key)) {
                throw dataError(file, "같은 논리코드와 접미번호의 조합이 중복되었습니다.");
            }
            physical.computeIfAbsent(row.logicalCode(), code -> new ArrayList<>()).add(row);
        }
        long used = 0;
        for (var entry : physical.entrySet()) {
            List<RawPhysicalRow> rows = entry.getValue();
            rows.sort(Comparator.comparingInt(RawPhysicalRow::suffix));
            boolean seenUnsold = false;
            for (int i = 0; i < rows.size(); i++) {
                RawPhysicalRow row = rows.get(i);
                if (row.suffix() != i + 1) {
                    throw dataError(DataPaths.PHYSICAL_FILE,
                            "같은 논리코드의 접미번호는 001부터 중간 번호의 누락 없이 연속해야 합니다.");
                }
                if (row.sold()) {
                    if (seenUnsold) {
                        throw dataError(DataPaths.PHYSICAL_FILE,
                                "더 작은 접미번호의 미판매 상품을 남겨 두고 더 큰 접미번호의 상품이 판매 상태입니다.");
                    }
                } else {
                    seenUnsold = true;
                    used += (long) logical.get(entry.getKey()).size();
                }
            }
        }
        if (used > Limits.MAX_CAPACITY) {
            throw new StartupDataException(List.of(DataPaths.LOGICAL_FILE, DataPaths.PHYSICAL_FILE),
                    "창고 사용 용량이 최대 용량 1000000을 초과합니다.");
        }
    }

    /**
     * 문법 검사를 통과한 논리코드의 번호가 00000인지 검사
     * @param code 문법상 대문자 P와 숫자 다섯 자리로 구성된 논리코드
     * @param file 번호 범위 오류를 표시할 논리 파일 또는 물리 파일의 이름
     */
    private void validateCode(String code, String file) {
        if (code.equals("P00000")) {
            throw dataError(file, "논리코드의 번호는 00001~99999여야 합니다.");
        }
    }

    /**
     * 오류 파일명과 구체적인 이유를 담은 시작 검사 예외를 생성
     * @param file 오류가 발생한 데이터 파일의 이름
     * @param reason 위반한 범위나 관계 규칙을 설명하는 오류 안내 문구
     * @return 호출한 위치에서 던질 StartupDataException 객체
     */
    private StartupDataException dataError(String file, String reason) {
        return new StartupDataException(List.of(file), reason);
    }
}
