package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.dto.AllInventoryResult;
import inventory.dto.ItemDetail;

/** 파일을 다시 읽지 않고 현재 메모리 상태를 조회합니다. */
public class QueryService {
    private final InventoryDatabase db;

    public QueryService(InventoryDatabase db) {
        this.db = db;
    }

    public AllInventoryResult findAll() {
        return new AllInventoryResult(db.findAllOrdered().stream()
                .map(item -> db.summarize(item.code())).toList(), db.warehouse());
    }

    public ItemDetail findOne(String code) {
        return new ItemDetail(db.summarize(code), db.findPhysicalItems(code));
    }
}
