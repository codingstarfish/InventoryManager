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
     * @param writer 변경된 상품·낱개 데이터를 파일에 저장하는 객체
     */
    public InventoryDatabase(EntityWriter writer) {
        this.writer = writer;
    }

    /**
     * 상품 등록과 함께 빈 낱개 목록도 만듦
     *
     * @param item 등록할 논리 상품 정보
     */
    public void addItem(LogicalItem item) {
        logicalItems.put(item.code(), item);
        physicalByCode.put(item.code(), new ArrayList<>());
    }

    /**
     * @return 등록된 논리 상품의 수
     */
    public int size() {
        return logicalItems.size();
    }

    /**
     * 등록된 상품을 찾고, 없으면 CODE_NOT_FOUND
     *
     * @param code 조회할 상품의 논리코드
     * @return 해당 논리코드로 등록된 상품 정보
     * @throws BusinessRuleException 해당 논리코드로 등록된 상품이 없는 경우(CODE_NOT_FOUND)
     */
    public LogicalItem requireItem(String code) {
        LogicalItem item = logicalItems.get(code);
        if (item == null) {
            throw new BusinessRuleException(ErrorCode.CODE_NOT_FOUND, Map.of());
        }
        return item;
    }

    /**
     * 전체 상품을 논리코드순으로 조회
     *
     * @return 논리코드 오름차순으로 정렬된 전체 상품 목록
     */
    public List<LogicalItem> findAllOrdered() {
        return logicalItems.values().stream()
                .sorted(Comparator.comparing(LogicalItem::code)).toList();
    }

    /**
     * 판매된 낱개도 포함한 원본 목록. 수정하면 DB에도 반영됨
     * 미등록 코드는 null 반환
     *
     * @param code 낱개 목록을 조회할 상품의 논리코드
     * @return DB에 보관된 해당 상품의 낱개 목록, 미등록 코드면 null
     */
    public List<PhysicalItem> findPhysicalItems(String code) {
        return physicalByCode.get(code);
    }

    /**
     * 전체 행 수는 누적 입고, sold가 true인 행 수는 누적 판매
     *
     * @param code 수량을 집계할 상품의 논리코드
     * @return 상품 정보와 누적 입고·누적 판매 수량을 담은 요약
     * @throws BusinessRuleException 해당 논리코드로 등록된 상품이 없는 경우(CODE_NOT_FOUND)
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
     * 미판매 낱개의 크기만 long으로 합산해 창고 사용량 계산
     *
     * @return 미판매 낱개 크기의 합을 사용량으로 담은 창고 요약
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
     * 등록할 때 논리 파일만 저장
     *
     * @throws inventory.exception.SaveFailureException 논리 파일을 열거나 쓰거나 닫는 데 실패한 경우
     */
    public void saveLogical() {
        writer.writeLogical(logicalItems.values());
    }

    /**
     * 입고/판매 후 전체 낱개를 물리 파일에 저장
     *
     * @throws inventory.exception.SaveFailureException 물리 파일을 열거나 쓰거나 닫는 데 실패한 경우
     */
    public void savePhysical() {
        List<PhysicalItem> rows = physicalByCode.values().stream()
                .flatMap(List::stream).toList();
        writer.writePhysical(rows);
    }
}
