package inventory.literal;

/**
 * 입력·업무·시작 오류 식별자. 메시지 출력은 ConsoleView, 화면 이동은 Console 담당.
 * 별도 표시가 없는 details는 빈 Map. 시작 오류는 종료 1.
 * 기획서 원판 5.6·7.3절: 해당 파일명과 구체 이유를 표시. 관계 오류는 필요한 파일명들.
 */
public enum ErrorCode {
    /** 메뉴는 1~5 중 하나를 입력해야 합니다. 주 메뉴 재표시; 재입력 확인 없음. */
    MENU_SYNTAX,

    /** 상품명은 완성형 한글, 영문, 숫자, 공백으로 구성된 1~30자여야 합니다. 이름 재입력 확인. */
    NAME_SYNTAX,

    /** 상품 크기는 숫자 1~10자리로 입력해야 합니다. 크기 재입력 확인. */
    SIZE_SYNTAX,

    /** 상품 크기는 1~1000 용량 단위여야 합니다. 크기 재입력 확인. */
    SIZE_RANGE,

    /** 가격은 숫자 1~10자리로 입력해야 합니다. 가격 재입력 확인. */
    PRICE_SYNTAX,

    /** 가격은 10~10000000원이어야 합니다. 가격 재입력 확인. */
    PRICE_RANGE,

    /** 가격은 10원 단위로 입력해야 합니다. 범위 통과 후 검사. */
    PRICE_UNIT,

    /** 논리적 상품코드는 대문자 P와 숫자 5자리로 입력해야 합니다. 코드 재입력 확인. */
    CODE_SYNTAX,

    /** 논리적 상품코드의 번호는 00001~99999여야 합니다. 코드 재입력 확인. */
    CODE_RANGE,

    /** 존재하지 않는 논리적 상품코드입니다. 코드 재입력 확인. */
    CODE_NOT_FOUND,

    /** 사용 가능한 논리적 상품코드가 없어 신규 상품을 등록할 수 없습니다. 즉시 주 메뉴. */
    LIMIT_LOGICAL_CODE,

    /** 해당 상품의 물리적 상품코드가 소진되어 입고할 수 없습니다. 수량 입력 없이 주 메뉴. */
    LIMIT_PHYSICAL_CODE,

    /** 입고 수량은 숫자 1~10자리로 입력해야 합니다. 수량 재입력 확인. */
    INBOUND_SYNTAX,

    /** 입고 수량은 1~100개여야 합니다. 수량 재입력 확인. */
    INBOUND_RANGE,

    /** 물리적 상품코드가 부족합니다. (발급 가능 수: {issuable}개). details: issuable 필수; 수량 재입력 확인. */
    SUFFIX_SHORTAGE,

    /** 창고의 남은 용량이 부족합니다. (필요 용량: {required}, 남은 용량: {remaining}). details: required, remaining 필수; 수량 재입력 확인. */
    CAPACITY_SHORTAGE,

    /** 판매 수량은 숫자 1~10자리로 입력해야 합니다. 수량 재입력 확인. */
    SALE_SYNTAX,

    /** 판매 수량은 1~100개여야 합니다. 수량 재입력 확인. */
    SALE_RANGE,

    /** 재고가 부족합니다. (현재 재고: {current}개). details: current 필수; 수량 재입력 확인. */
    STOCK_SHORTAGE,

    /** 조회 방식은 1 또는 2를 입력해야 합니다. 방식 재입력 확인. */
    QUERY_MODE_SYNTAX,

    /** y 또는 n을 입력해야 합니다. 같은 확인 반복. */
    RETRY_SYNTAX,

    /** 데이터 디렉터리나 빈 파일 생성 실패. 실패 경로 또는 파일명. */
    FILE_CREATE,

    /** 데이터 파일 경로가 일반 파일이 아님. */
    FILE_NOT_REGULAR,

    /** 데이터 파일 읽기 실패. */
    FILE_READ,

    /** 데이터 파일 쓰기 접근 불가. */
    FILE_WRITE_ACCESS,

    /** 올바른 UTF-8 파일이어야 함. */
    FILE_UTF8,

    /** BOM 없는 UTF-8 파일이어야 함. */
    FILE_BOM,

    /** 빈 행은 허용하지 않습니다. */
    FILE_EMPTY_LINE,

    /** 논리 파일의 각 행은 4개 필드여야 합니다. */
    FILE_LOGICAL_FIELDS,

    /** 물리 파일의 각 행은 3개 필드여야 합니다. */
    FILE_PHYSICAL_FIELDS,

    /** 상품명은 완성형 한글·영문·숫자·표준 공백 1~30자, 앞뒤 공백 금지. */
    FILE_NAME_SYNTAX,

    /** 논리코드는 대문자 P와 숫자 다섯 자리여야 합니다. */
    FILE_CODE_SYNTAX,

    /** 크기는 첫 문자가 1~9인 숫자 1~4자리여야 합니다. */
    FILE_SIZE_SYNTAX,

    /** 가격은 첫 문자가 1~9인 숫자 1~8자리여야 합니다. */
    FILE_PRICE_SYNTAX,

    /** 접미번호는 숫자 세 자리여야 합니다. */
    FILE_SUFFIX_SYNTAX,

    /** 판매여부는 0 또는 1 한 문자. */
    FILE_SOLD_SYNTAX,

    /** 논리코드의 번호는 00001~99999여야 합니다. */
    FILE_CODE_RANGE,

    /** 상품 크기는 1~1000 용량 단위여야 합니다. */
    FILE_SIZE_RANGE,

    /** 가격은 10~10000000원이어야 합니다. */
    FILE_PRICE_RANGE,

    /** 가격은 10원 단위여야 합니다. */
    FILE_PRICE_UNIT,

    /** 접미번호는 001~999여야 합니다. */
    FILE_SUFFIX_RANGE,

    /** 논리코드가 중복되었습니다. logical_item.txt의 관계 오류. */
    FILE_DUPLICATE_CODE,

    /** 논리코드는 P00001부터 중간 번호 누락 없이 연속. */
    FILE_CODE_GAP,

    /** 물리 파일의 논리코드가 논리 파일에 등록되어 있지 않음. */
    FILE_UNKNOWN_REFERENCE,

    /** 같은 논리코드와 접미번호 조합 중복. */
    FILE_DUPLICATE_PHYSICAL,

    /** 상품별 접미번호는 001부터 중간 번호 누락 없이 연속. */
    FILE_SUFFIX_GAP,

    /** 작은 미판매 접미번호 뒤에 큰 판매 완료 접미번호가 존재. */
    FILE_FIFO,

    /** 전체 미판매 용량은 1000000 이하. 양쪽 파일명을 메타데이터에 보존. */
    FILE_CAPACITY;
}
