package inventory.literal;

/**
 * 공유 상수만 보유하는 클래스. 인스턴스 생성 없음.
 */
public final class InputPatterns {
    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String MAIN_MENU = "[1-5]";

    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String QUERY_MODE = "[12]";

    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String RETRY = "[yn]";

    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String NAME = "[가-힣A-Za-z0-9 ]{1,30}";

    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String LOGICAL_CODE = "P[0-9]{5}";

    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String INPUT_DECIMAL = "[0-9]{1,10}";

    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String FILE_SIZE = "[1-9][0-9]{0,3}";

    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String FILE_PRICE = "[1-9][0-9]{0,7}";

    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String FILE_SUFFIX = "[0-9]{3}";

    /** matches()로 전체 문자열 검사; 범위·단위 검사는 별도 */
    public static final String FILE_SOLD = "[01]";

    private InputPatterns() {
    }
}
