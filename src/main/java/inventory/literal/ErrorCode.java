package inventory.literal;

// 입력, 업무 오류 식별자. 
public enum ErrorCode {
    //메뉴는 1~5 중 하나를 입력해야 합니다. 
    MENU_SYNTAX,

    //상품명은 완성형 한글, 영문, 숫자, 공백으로 구성된 1~30자여야 합니다.
    NAME_SYNTAX,

    //상품 크기는 숫자 1~10자리로 입력해야 합니다. 
    SIZE_SYNTAX,

    //상품 크기는 1~1000 용량 단위여야 합니다.
    SIZE_RANGE,

    //가격은 숫자 1~10자리로 입력해야 합니다. 
    PRICE_SYNTAX,

    //가격은 10~10000000원이어야 합니다. 
    PRICE_RANGE,

    //가격은 10원 단위로 입력해야 합니다. 
    PRICE_UNIT,

    //논리적 상품코드는 대문자 P와 숫자 5자리로 입력해야 합니다. 
    CODE_SYNTAX,

    //논리적 상품코드의 번호는 00001~99999여야 합니다. 
    CODE_RANGE,

    //존재하지 않는 논리적 상품코드입니다. 
    CODE_NOT_FOUND,

    //사용 가능한 논리적 상품코드가 없어 신규 상품을 등록할 수 없습니다. 
    LIMIT_LOGICAL_CODE,

    //해당 상품의 물리적 상품코드가 소진되어 입고할 수 없습니다. 
    LIMIT_PHYSICAL_CODE,

    //입고 수량은 숫자 1~10자리로 입력해야 합니다.
    INBOUND_SYNTAX,

    //입고 수량은 1~100개여야 합니다. 
    INBOUND_RANGE,

    //물리적 상품코드가 부족합니다. (발급 가능 수: {issuable}개).
    SUFFIX_SHORTAGE,

    //창고의 남은 용량이 부족합니다. (필요 용량: {required}, 남은 용량: {remaining}).
    CAPACITY_SHORTAGE,

    //판매 수량은 숫자 1~10자리로 입력해야 합니다. 
    SALE_SYNTAX,

    //판매 수량은 1~100개여야 합니다. 
    SALE_RANGE,

    //재고가 부족합니다. (현재 재고: {current}개)
    STOCK_SHORTAGE,

    //조회 방식은 1 또는 2를 입력해야 합니다
    QUERY_MODE_SYNTAX,

    //y 또는 n을 입력해야 합니다
    RETRY_SYNTAX
}
