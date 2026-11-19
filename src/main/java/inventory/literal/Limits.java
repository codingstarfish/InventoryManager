package inventory.literal;


public final class Limits {
    public static final int MAX_PRODUCTS = 99999;

    public static final int MAX_SUFFIX = 999;

    public static final long MAX_CAPACITY = 1000000L;

    public static final int MIN_SIZE = 1;

    public static final int MAX_SIZE = 1000;

    public static final int MIN_PRICE = 10;

    public static final int MAX_PRICE = 10000000;

    public static final int PRICE_UNIT = 10;

    public static final int MIN_QUANTITY = 1;

    public static final int MAX_QUANTITY = 100;

    public static final int MAX_SHELF_LIFE = 3650;
    public static final int REFUND_DAYS = 14;
    public static final int RESALE_PERCENT = 70;
    public static final java.time.LocalDate MIN_DATE = java.time.LocalDate.of(2000,1,1);
    public static final java.time.LocalDate MAX_DATE = java.time.LocalDate.of(2037,12,31);
    private Limits() {
    }
}
