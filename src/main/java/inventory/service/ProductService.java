package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.entity.LogicalItem;
import inventory.literal.Limits;
import inventory.validation.DomainRules;

/** 신규 상품 등록. Console에서 한도와 입력을 확인한 뒤 논리 파일만 저장합니다. */
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
        LogicalItem item = new LogicalItem(DomainRules.formatLogicalCode(db.size() + 1), name, size, price);
        db.addItem(item);
        db.saveLogical();
        return item;
    }
}
