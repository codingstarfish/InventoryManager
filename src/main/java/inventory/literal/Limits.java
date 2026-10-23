package inventory.literal;

/**
 * 공유 상수만 보유하는 클래스. 인스턴스 생성 없음.
 */
public final class Limits {
    /** 등록 가능한 상품 종류 수 */
    public static final int MAX_PRODUCTS = 99999;

    /** 상품별 누적 입고 상한 */
    public static final int MAX_SUFFIX = 999;

    /** 창고 용량 */
    public static final long MAX_CAPACITY = 1000000L;

    /** 최소 크기 */
    public static final int MIN_SIZE = 1;

    /** 최대 크기 */
    public static final int MAX_SIZE = 1000;

    /** 최소 가격 */
    public static final int MIN_PRICE = 10;

    /** 최대 가격 */
    public static final int MAX_PRICE = 10000000;

    /** 가격 단위 */
    public static final int PRICE_UNIT = 10;

    /** 최소 요청 수량 */
    public static final int MIN_QUANTITY = 1;

    /** 최대 요청 수량 */
    public static final int MAX_QUANTITY = 100;

    /** 상품명 코드 포인트 길이 */
    public static final int MAX_NAME_LENGTH = 30;

    /** 사용자 숫자 입력 최대 자릿수 */
    public static final int MAX_INPUT_DIGITS = 10;

    private Limits() {
    }
}
