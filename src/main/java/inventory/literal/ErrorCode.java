package inventory.literal;

/**
 * 입력·업무 오류 식별자. 메시지 출력은 ConsoleView, 화면 이동은 Console 담당.
 * 별도 표시가 없는 details는 빈 Map.
 */
public enum ErrorCode {
    //메뉴는 1~5 중 하나를 입력해야 합니다. 주 메뉴 재표시; 재입력 확인 없음
    MENU_SYNTAX,

    //상품명은 완성형 한글, 영문, 숫자, 공백으로 구성된 1~30자여야 합니다. 이름 재입력 확인.
    NAME_SYNTAX,

    //상품 크기는 숫자 1~10자리로 입력해야 합니다. 크기 재입력 확인.
    SIZE_SYNTAX,

    //상품 크기는 1~1000 용량 단위여야 합니다. 크기 재입력 확인.
    SIZE_RANGE,

    //가격은 숫자 1~10자리로 입력해야 합니다. 가격 재입력 확인
    PRICE_SYNTAX,

    //가격은 10~10000000원이어야 합니다. 가격 재입력 확인.
    PRICE_RANGE,

    //가격은 10원 단위로 입력해야 합니다. 범위 통과 후 검사.
    PRICE_UNIT,

    //논리적 상품코드는 대문자 P와 숫자 5자리로 입력해야 합니다. 코드 재입력 확인.
    CODE_SYNTAX,

    //논리적 상품코드의 번호는 00001~99999여야 합니다. 코드 재입력 확인. 
    CODE_RANGE,

    //존재하지 않는 논리적 상품코드입니다. 코드 재입력 확인.
    CODE_NOT_FOUND,

    //사용 가능한 논리적 상품코드가 없어 신규 상품을 등록할 수 없습니다. 즉시 주 메뉴.
    LIMIT_LOGICAL_CODE,

    //해당 상품의 물리적 상품코드가 소진되어 입고할 수 없습니다. 수량 입력 없이 주 메뉴.
    LIMIT_PHYSICAL_CODE,

    //입고 수량은 숫자 1~10자리로 입력해야 합니다. 수량 재입력 확인. 
    INBOUND_SYNTAX,

    //입고 수량은 1~100개여야 합니다. 수량 재입력 확인. 
    INBOUND_RANGE,

    //물리적 상품코드가 부족합니다. (발급 가능 수: {issuable}개). details: issuable 필수; 수량 재입력 확인. 
    SUFFIX_SHORTAGE,

    //창고의 남은 용량이 부족합니다. (필요 용량: {required}, 남은 용량: {remaining}). details: required, remaining 필수; 수량 재입력 확인.
    CAPACITY_SHORTAGE,

    //판매 수량은 숫자 1~10자리로 입력해야 합니다. 수량 재입력 확인.
    SALE_SYNTAX,

    //판매 수량은 1~100개여야 합니다. 수량 재입력 확인. 
    SALE_RANGE,

    //재고가 부족합니다. (현재 재고: {current}개). details: current 필수; 수량 재입력 확인. 
    STOCK_SHORTAGE,

    //조회 방식은 1 또는 2를 입력해야 합니다. 방식 재입력 확인.
    QUERY_MODE_SYNTAX,

    //y 또는 n을 입력해야 합니다. 같은 확인 반복. 
    RETRY_SYNTAX
}
