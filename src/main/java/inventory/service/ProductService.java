package inventory.service;

import inventory.database.InventoryDatabase;
import inventory.entity.LogicalItem;
import inventory.literal.Limits;
import inventory.validation.DomainRules;

//신규 상품 등록
public class ProductService {
    private final InventoryDatabase db;

    /**
     * 상품 등록에 사용할 공유 데이터베이스를 보관
     * @param db 등록된 상품 목록과 논리 파일 저장 기능을 제공하는 공유 데이터베이스
     */
    public ProductService(InventoryDatabase db) {
        this.db = db;
    }

    /**
     * 논리코드를 추가로 발급할 수 있는지 확인
     * @return 등록 상품이 99999개 미만이면 true, 논리코드가 소진되었으면 false
     */
    public boolean canRegister() {
        return db.size() < Limits.MAX_PRODUCTS;
    }

    /**
     * 등록된 상품이 있는지 확인합니다.
     * @return 등록된 상품이 하나 이상이면 true, 하나도 없으면 false
     */
    public boolean hasProducts() {
        return db.size() > 0;
    }

    /**
     * 다음 논리코드를 자동 발급하고 상품을 등록한 뒤 논리 파일에 저장
     * @param name 양 끝의 표준 공백 제거와 문법 검사를 마친 상품명
     * @param size 범위 검사를 마친 1~1000 용량 단위의 낱개 크기
     * @param price 범위와 단위 검사를 마친 10~10000000원 사이의 10원 단위 가격
     * @return 발급된 논리코드와 입력한 속성을 가진 신규 상품 객체
     */
    public LogicalItem register(String name, int size, int price) {
        LogicalItem item = new LogicalItem(DomainRules.formatLogicalCode(db.size() + 1), name, size, price);
        db.addItem(item);
        db.saveLogical();
        return item;
    }
}
