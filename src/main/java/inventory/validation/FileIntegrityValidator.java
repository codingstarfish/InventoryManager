package inventory.validation;

import java.util.OptionalInt;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Objects;
import inventory.literal.ErrorCode;
import inventory.literal.DataPaths;
import inventory.literal.Limits;
import inventory.entity.PhysicalItem;
import inventory.entity.LogicalItem;
import inventory.database.RawPhysicalRow;
import inventory.database.RawLogicalRow;
import inventory.database.ParsedFiles;
import inventory.dto.InventorySnapshot;
import inventory.exception.StartupDataException;

/**
 * 시작 의미·관계 검사. 파일 접근·저장·상태 게시 없음. 구체 ErrorCode·reason 사용.
 */
public class FileIntegrityValidator {
    /** 상태 없는 시작 의미·관계 검증기를 생성합니다. */
    public FileIntegrityValidator() {
    }

    /**
     * 양쪽 문법 통과 후에만 실행. 개별 범위·가격 단위 → 논리 중복·P00001부터 연속
     * → 등록 참조 → code/suffix 조합 중복 → 상품별 001부터 연속 → 접미번호순 FIFO
     * → H=T+Q, A=999-H, 각 0~999 → sum((long)size*Q)&lt;=1000000.
     * 중복 검사 전에 Map.put으로 원본을 덮어쓰지 않습니다.
     * 파일의 원래 행 순서는 자유. 미판매를 만난 뒤 판매 완료가 나오면 FIFO 오류.
     * 유효 Entity와 상품별 빈 목록도 포함한 깊은 불변 Snapshot 구성.
     * @param files 두 파일의 모든 문법 검사를 통과한 중간 행 목록
     * @return 모든 의미·관계 규칙을 만족하는 초기 상태
     * @throws StartupDataException 각 FILE_*_RANGE/PRICE_UNIT, DUPLICATE_CODE/CODE_GAP, UNKNOWN_REFERENCE, DUPLICATE_PHYSICAL/SUFFIX_GAP, FIFO, CAPACITY
     */
    public InventorySnapshot validateAndBuild(ParsedFiles files) {
        Objects.requireNonNull(files, "files");
        Map<String, LogicalItem> logical = new TreeMap<>();
        for (RawLogicalRow row : files.logicalRows()) {
            String file = DataPaths.LOGICAL_FILE;
            validateCode(row.code(), file, row.lineNumber());
            if (row.size() < Limits.MIN_SIZE || row.size() > Limits.MAX_SIZE) {
                throw dataError(file, ErrorCode.FILE_SIZE_RANGE, "상품 크기는 1~1000 용량 단위여야 합니다.", row.lineNumber());
            }
            if (row.price() < Limits.MIN_PRICE || row.price() > Limits.MAX_PRICE) {
                throw dataError(file, ErrorCode.FILE_PRICE_RANGE, "가격은 10~10000000원이어야 합니다.", row.lineNumber());
            }
            if (row.price() % Limits.PRICE_UNIT != 0) {
                throw dataError(file, ErrorCode.FILE_PRICE_UNIT, "가격은 10원 단위여야 합니다.", row.lineNumber());
            }
            if (logical.containsKey(row.code())) {
                throw dataError(file, ErrorCode.FILE_DUPLICATE_CODE, "논리코드가 중복되었습니다.", row.lineNumber());
            }
            logical.put(row.code(), new LogicalItem(row.code(), row.name(), row.size(), row.price()));
        }
        int number = 1;
        for (String code : logical.keySet()) {
            if (number > Limits.MAX_PRODUCTS || !code.equals(DomainRules.formatLogicalCode(number++))) {
                throw dataError(DataPaths.LOGICAL_FILE, ErrorCode.FILE_CODE_GAP,
                        "논리코드가 P00001부터 중간 번호의 누락 없이 연속해야 합니다.", 0);
            }
        }
        Map<String, List<PhysicalItem>> physical = new HashMap<>();
        for (String code : logical.keySet()) {
            physical.put(code, new ArrayList<>());
        }
        Set<String> physicalCodes = new HashSet<>();
        for (RawPhysicalRow row : files.physicalRows()) {
            String file = DataPaths.PHYSICAL_FILE;
            validateCode(row.logicalCode(), file, row.lineNumber());
            if (row.suffix() < 1 || row.suffix() > Limits.MAX_SUFFIX) {
                throw dataError(file, ErrorCode.FILE_SUFFIX_RANGE, "접미번호는 001~999여야 합니다.", row.lineNumber());
            }
            if (!logical.containsKey(row.logicalCode())) {
                throw dataError(file, ErrorCode.FILE_UNKNOWN_REFERENCE,
                        "물리 파일의 논리코드가 논리 파일에 등록되어 있지 않습니다.", row.lineNumber());
            }
            String key = row.logicalCode() + "-" + row.suffix();
            if (!physicalCodes.add(key)) {
                throw dataError(file, ErrorCode.FILE_DUPLICATE_PHYSICAL,
                        "같은 논리코드와 접미번호의 조합이 중복되었습니다.", row.lineNumber());
            }
            physical.get(row.logicalCode()).add(new PhysicalItem(row.logicalCode(), row.suffix(), row.sold()));
        }
        long used = 0;
        for (var entry : physical.entrySet()) {
            List<PhysicalItem> rows = entry.getValue();
            rows.sort(Comparator.comparingInt(PhysicalItem::suffix));
            boolean seenUnsold = false;
            for (int i = 0; i < rows.size(); i++) {
                PhysicalItem row = rows.get(i);
                if (row.suffix() != i + 1) {
                    throw dataError(DataPaths.PHYSICAL_FILE, ErrorCode.FILE_SUFFIX_GAP,
                            "같은 논리코드의 접미번호는 001부터 중간 번호의 누락 없이 연속해야 합니다.", 0);
                }
                if (row.sold()) {
                    if (seenUnsold) {
                        throw dataError(DataPaths.PHYSICAL_FILE, ErrorCode.FILE_FIFO,
                                "더 작은 접미번호의 미판매 상품을 남겨 두고 더 큰 접미번호의 상품이 판매 상태입니다.", 0);
                    }
                } else {
                    seenUnsold = true;
                    used += (long) logical.get(entry.getKey()).size();
                }
            }
        }
        if (used > Limits.MAX_CAPACITY) {
            throw new StartupDataException(List.of(DataPaths.LOGICAL_FILE, DataPaths.PHYSICAL_FILE),
                    ErrorCode.FILE_CAPACITY, "창고 사용 용량이 최대 용량 1000000을 초과합니다.", OptionalInt.empty());
        }
        return new InventorySnapshot(logical, physical);
    }

    private void validateCode(String code, String file, int lineNo) {
        try {
            DomainRules.validateLogicalCode(code);
        } catch (IllegalArgumentException e) {
            throw dataError(file, ErrorCode.FILE_CODE_RANGE, "논리코드의 번호는 00001~99999여야 합니다.", lineNo);
        }
    }

    private StartupDataException dataError(String file, ErrorCode code, String reason, int lineNo) {
        return new StartupDataException(List.of(file), code, reason,
                lineNo == 0 ? OptionalInt.empty() : OptionalInt.of(lineNo));
    }
}
