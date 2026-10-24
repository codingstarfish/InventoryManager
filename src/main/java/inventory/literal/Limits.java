package inventory.literal;


public final class Limits {
    // 등록 가능한 상품 종류 수
    public static final int MAX_PRODUCTS = 99999;

    // 상품별 누적 입고 상한
    public static final int MAX_SUFFIX = 999;

    // 창고 용량 
    public static final long MAX_CAPACITY = 1000000L;

    // 최소 크기
    public static final int MIN_SIZE = 1;

    // 최대 크기 
    public static final int MAX_SIZE = 1000;

    // 최소 가격
    public static final int MIN_PRICE = 10;

    // 최대 가격
    public static final int MAX_PRICE = 10000000;

    // 가격 단위
    public static final int PRICE_UNIT = 10;

    // 최소 요청 수량
    public static final int MIN_QUANTITY = 1;

    // 최대 요청 수량
    public static final int MAX_QUANTITY = 100;

    private Limits() {
    }
}
