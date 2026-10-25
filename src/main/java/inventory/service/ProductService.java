package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.entity.LogicalItem;
import inventory.literal.Limits;
import inventory.validation.DomainRules;

//신규 상품 등록
public class ProductService {
    private final InventoryDatabase db;

    /**
     * @param db 상품을 등록하고 저장할 데이터베이스
     */
    public ProductService(InventoryDatabase db) {
        this.db = db;
    }

    /**
     * 상품 종류는 최대 99999개까지 등록
     *
     * @return 상품 종류를 추가로 등록할 수 있으면 true
     */
    public boolean canRegister() {
        return db.size() < Limits.MAX_PRODUCTS;
    }

    /**
     * @return 등록된 상품이 하나 이상이면 true
     */
    public boolean hasProducts() {
        return db.size() > 0;
    }

    /**
     * 입력값은 검증을 마친 상태. 다음 논리코드를 붙여 등록하고 논리 파일만 저장
     *
     * @param name 형식 검증을 마친 상품명
     * @param size 범위 검증을 마친 상품 한 개의 크기(용량 단위)
     * @param price 범위와 10원 단위 검증을 마친 상품 가격(원)
     * @return 새 논리코드가 부여되어 등록된 상품
     * @throws inventory.exception.SaveFailureException 등록한 상품을 논리 파일에 저장하지 못한 경우
     */
    public LogicalItem register(String name, int size, int price) {
        LogicalItem item = new LogicalItem(DomainRules.formatLogicalCode(db.size() + 1), name, size, price);
        db.addItem(item);
        db.saveLogical();
        return item;
    }
}
