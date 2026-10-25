package inventory.literal;

public final class DataPaths {
    // 데이터 디렉터리명, JAR 경로가 아닌 현재 작업 디렉터리 기준
    public static final String DATA_DIR = "data";

    // data 하위의 논리 파일명
    public static final String LOGICAL_FILE = "logical_item.txt";

    // data 하위의 물리 파일명
    public static final String PHYSICAL_FILE = "physical_item.txt";

    private DataPaths() {
    }
}
