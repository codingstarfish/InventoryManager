package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.entity.LogicalItem;
import inventory.literal.Limits;
import inventory.validation.DomainRules;

//신규 상품 등록
public class ProductService {
    private final InventoryDatabase db;

    public ProductService(InventoryDatabase db) {
        this.db = db;
    }

    // 상품 종류는 최대 99999개까지 등록
    public boolean canRegister() {
        return db.size() < Limits.MAX_PRODUCTS;
    }

    public boolean hasProducts() {
        return db.size() > 0;
    }

    // 입력값은 검증을 마친 상태. 다음 논리코드를 붙여 등록하고 논리 파일만 저장
    public LogicalItem register(String name, int size, int price) {
        LogicalItem item = new LogicalItem(DomainRules.formatLogicalCode(db.size() + 1), name, size, price);
        db.addItem(item);
        db.saveLogical();
        return item;
    }
}
