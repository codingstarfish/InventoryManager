package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.dto.InventorySnapshot;
import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import inventory.exception.BusinessRuleException;
import inventory.literal.ErrorCode;
import inventory.literal.Limits;
import inventory.validation.DomainRules;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 신규 상품 등록. 상품 값은 LogicalItem에서 검사하고, 논리 파일만 저장합니다. */
public class ProductService {
    private final InventoryDatabase db;

    public ProductService(InventoryDatabase db) {
        this.db = db;
    }

    public boolean canRegister() {
        return db.size() < Limits.MAX_PRODUCTS;
    }

    public boolean hasProducts() {
        return db.size() > 0;
    }

    public LogicalItem register(String name, int size, int price) {
        if (!canRegister()) {
            throw new BusinessRuleException(ErrorCode.LIMIT_LOGICAL_CODE, Map.of());
        }
        LogicalItem item = new LogicalItem(DomainRules.formatLogicalCode(db.size() + 1), name, size, price);
        InventorySnapshot before = db.snapshot();
        Map<String, LogicalItem> logical = new HashMap<>(before.logicalItems());
        Map<String, List<PhysicalItem>> physical = new HashMap<>(before.physicalByCode());
        logical.put(item.code(), item);
        physical.put(item.code(), List.of());
        db.saveLogical(new InventorySnapshot(logical, physical));
        return item;
    }
}
