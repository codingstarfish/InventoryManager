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


// 시작 검사 시에 의미 오류와 데이터 간 관계 오류를 검사한다.
public class FileIntegrityValidator {

    public FileIntegrityValidator() {
    }

    /**
     * 세 파일의 문법 검사가 끝난 뒤 값의 범위와 파일 간 관계 검사
     * 중복/누락/미등록 상품 참조/상태/날짜/용량 위반이면 StartupDataException
     *
     * @param logicalRows  문법 검사를 통과한 논리 파일의 상품 행 목록
     * @param physicalRows 문법 검사를 통과한 물리 파일의 낱개 상품 행 목록
     * @throws StartupDataException 상품 크기가 1000을 초과하거나 가격의 범위·10원 단위가 잘못된 경우,
     *                              논리코드가 P00000이거나 중복·누락된 경우, 접미번호가 000이거나 중복·누락된 경우,
     *                              물리 파일이 미등록 상품을 참조하거나 상태·날짜 일관성을 어긴 경우,
     *                              미판매 재고의 총용량이 1000000을 초과한 경우
     */
    public void validate(List<RawLogicalRow> logicalRows, List<RawPhysicalRow> physicalRows, String dateText) {
        var date = dateText == null ? null : parseStoredDate(dateText, DataPaths.DATE_FILE);
        Map<String, RawLogicalRow> logical = new TreeMap<>();
        for (RawLogicalRow row : logicalRows) {
            String file = DataPaths.LOGICAL_FILE;
            validateCode(row.code(), file);
            if (row.shelfLifeDays() > Limits.MAX_SHELF_LIFE) throw dataError(file, "유통기한 일수는 0 또는 1~3650이어야 합니다.");
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
                var item = logical.get(entry.getKey());
                var produced = parseStoredDate(row.producedOn(), DataPaths.PHYSICAL_FILE);
                var sold = row.soldOn().equals("-") ? null : parseStoredDate(row.soldOn(), DataPaths.PHYSICAL_FILE);
                if (date == null)
                    throw new StartupDataException(List.of(DataPaths.DATE_FILE, DataPaths.PHYSICAL_FILE), "날짜 파일이 비어 있는데 물리 파일에 기록이 있습니다.");
                if (produced.isAfter(date)) throw dataError(DataPaths.PHYSICAL_FILE, "생산일이 가상날짜보다 이후입니다.");
                if ((row.state() == inventory.entity.ItemState.SOLD) != (sold != null))
                    throw dataError(DataPaths.PHYSICAL_FILE, "상태와 판매일이 일치하지 않습니다.");
                if (sold != null && (sold.isBefore(produced) || sold.isAfter(date)))
                    throw dataError(DataPaths.PHYSICAL_FILE, "판매일은 생산일과 같거나 이후이고, 가상날짜와 같거나 이전이어야 합니다.");
                if (item.shelfLifeDays() == 0 && row.state() == inventory.entity.ItemState.DISCARDED)
                    throw dataError(DataPaths.PHYSICAL_FILE, "유통기한이 없는 상품은 폐기 상태일 수 없습니다.");
                if (item.shelfLifeDays() > 0) {
                    var expiry = produced.plusDays(item.shelfLifeDays());
                    if (sold != null && sold.isAfter(expiry))
                        throw dataError(DataPaths.PHYSICAL_FILE, "판매일은 만료일과 같거나 이전이어야 합니다.");
                    if (row.state() == inventory.entity.ItemState.IN_STOCK && expiry.isBefore(date))
                        throw dataError(DataPaths.PHYSICAL_FILE, "유통기한이 지났으나 폐기되지 않은 재고가 있습니다.");
                }
                if (row.state() == inventory.entity.ItemState.IN_STOCK) used += (long) item.size();
            }
        }
        if (used > Limits.MAX_CAPACITY) {
            throw new StartupDataException(List.of(DataPaths.LOGICAL_FILE, DataPaths.PHYSICAL_FILE),
                    "창고 사용 용량이 최대 용량 1000000을 초과합니다.");
        }
    }

    /**
     * 형식은 이미 확인한 상태. 번호 00000만 여기서 거절
     *
     * @param code P와 숫자 다섯 자리 형식 검증을 마친 논리코드
     * @param file 오류에 표시할 원본 데이터 파일명
     * @throws StartupDataException 논리코드가 P00000인 경우
     */
    public java.time.LocalDate parseStoredDate(String value, String file) {
        try {
            var day = java.time.LocalDate.parse(value);
            if (day.isBefore(Limits.MIN_DATE) || day.isAfter(Limits.MAX_DATE))
                throw new java.time.DateTimeException("range");
            return day;
        } catch (java.time.DateTimeException e) {
            throw dataError(file, "2000-01-01~2037-12-31 범위의 실제 날짜여야 합니다.");
        }
    }

    private void validateCode(String code, String file) {
        if (code.equals("P00000")) {
            throw dataError(file, "논리코드의 번호는 00001~99999여야 합니다.");
        }
    }

    /**
     * @param file   오류가 발생한 데이터 파일명
     * @param reason 데이터 검증에 실패한 이유
     * @return 파일명과 실패 이유를 담은 시작 데이터 오류
     */
    private StartupDataException dataError(String file, String reason) {
        return new StartupDataException(List.of(file), reason);
    }
}
