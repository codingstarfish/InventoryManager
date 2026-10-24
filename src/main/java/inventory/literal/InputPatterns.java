package inventory.literal;


public final class InputPatterns {
    public static final String MAIN_MENU = "[1-5]";

    public static final String QUERY_MODE = "[12]";

    public static final String RETRY = "[yn]";

    public static final String NAME = "[가-힣A-Za-z0-9 ]{1,30}";

    public static final String LOGICAL_CODE = "P[0-9]{5}";

    public static final String INPUT_DECIMAL = "[0-9]{1,10}";

    public static final String FILE_SIZE = "[1-9][0-9]{0,3}";

    public static final String FILE_PRICE = "[1-9][0-9]{0,7}";

    public static final String FILE_SUFFIX = "[0-9]{3}";

    public static final String FILE_SOLD = "[01]";

    private InputPatterns() {
    }
}
