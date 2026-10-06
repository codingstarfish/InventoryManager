package inventory.literal;

/**
 * 작업 디렉터리 아래 data 디렉터리와 고정 데이터 파일명.
 * 기획서 원판 2.2·4.8.1·5절 및 구현 명세 5.3절 기준.
 */
public final class DataPaths {
    /** 데이터 디렉터리명. JAR 경로가 아닌 현재 작업 디렉터리 기준. */
    public static final String DATA_DIR = "data";

    /** data 하위의 논리 파일명. */
    public static final String LOGICAL_FILE = "logical_item.txt";

    /** data 하위의 물리 파일명. */
    public static final String PHYSICAL_FILE = "physical_item.txt";

    private DataPaths() {
    }
}
