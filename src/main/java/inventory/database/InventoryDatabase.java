package inventory.database;

import inventory.dto.StockSummary;
import inventory.dto.WarehouseSummary;
import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import inventory.exception.BusinessRuleException;
import inventory.literal.ErrorCode;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**  Map과 List로 현재 데이터를 관리하고, 변경한 종류의 파일만 저장 */
public class InventoryDatabase {
    private final Map<String, LogicalItem> logicalItems = new HashMap<>();
    private final Map<String, List<PhysicalItem>> physicalByCode = new HashMap<>();
    private final EntityWriter writer;

    /**
     * 상품,낱개 목록을 초기화하고 파일 저장기를 보관
     * @param writer 시작 시 확정한 두 데이터 파일에 내용을 기록하는 저장기
     */
    public InventoryDatabase(EntityWriter writer) {
        this.writer = writer;
    }

    /**
     * 검증된 논리 상품과 해당 상품의 빈 낱개 목록을 메모리에 추가
     * @param item 시작 검사 또는 신규 등록 검증을 마친 상품 객체
     */
    public void addItem(LogicalItem item) {
        logicalItems.put(item.code(), item);
        physicalByCode.put(item.code(), new ArrayList<>());
    }

    /**
     * 메모리에 등록된 상품 종류 수를 조회
     * @return 현재 등록된 논리 상품의 개수, 정상 상태에서는 0~99999임
     */
    public int size() {
        return logicalItems.size();
    }

    /**
     * 논리코드에 해당하는 등록 상품을 조회하고, 없으면 업무 오류를 전달
     * @param code 등록 여부를 확인할 논리코드 문자열
     * @return 논리코드에 해당하는 등록 상품 객체
     */
    public LogicalItem requireItem(String code) {
        LogicalItem item = logicalItems.get(code);
        if (item == null) {
            throw new BusinessRuleException(ErrorCode.CODE_NOT_FOUND, Map.of());
        }
        return item;
    }

    /**
     * 등록된 모든 상품을 논리코드 오름차순으로 조회
     * @return 정렬된 상품 목록, 상품이 없으면 빈 목록
     */
    public List<LogicalItem> findAllOrdered() {
        return logicalItems.values().stream()
                .sorted(Comparator.comparing(LogicalItem::code)).toList();
    }

    /**
     * 논리코드에 연결된 메모리의 낱개 목록을 조회
     * @param code 낱개 목록을 찾을 상품의 논리코드
     * @return 판매 완료된 행까지 포함한 수정 가능한 원본 목록, 등록되지 않은 코드이면 null
     */
    public List<PhysicalItem> findPhysicalItems(String code) {
        return physicalByCode.get(code);
    }

    /**
     * 등록 상품의 낱개 행을 기준으로 누적 입고와 누적 판매 수량을 집계
     * @param code 집계할 등록 상품의 논리코드
     * @return 상품 정보와 누적 입고,판매 수량 및 파생 재고를 제공하는 요약 객체
     */
    public StockSummary summarize(String code) {
        LogicalItem item = requireItem(code);
        List<PhysicalItem> rows = findPhysicalItems(code);
        int sold = 0;
        for (PhysicalItem row : rows) {
            if (row.sold()) {
                sold++;
            }
        }
        return new StockSummary(item, rows.size(), sold);
    }

    /**
     * 모든 상품의 미판매 낱개 크기를 long 값으로 합산
     * @return 창고 사용 용량과 최대 용량에서 계산한 남은 용량을 제공하는 객체
     */
    public WarehouseSummary warehouse() {
        long used = 0;
        for (LogicalItem item : logicalItems.values()) {
            for (PhysicalItem row : findPhysicalItems(item.code())) {
                if (!row.sold()) {
                    used += item.size();
                }
            }
        }
        return new WarehouseSummary(used);
    }

    /**
     * 전체 논리 상품을 논리 파일에 저장
     */
    public void saveLogical() {
        writer.writeLogical(logicalItems.values());
    }

    /**
     * 판매된 낱개를 포함한 전체 낱개 목록을 물리 파일에 저장합니다.
     */
    public void savePhysical() {
        List<PhysicalItem> rows = physicalByCode.values().stream()
                .flatMap(List::stream).toList();
        writer.writePhysical(rows);
    }
}
