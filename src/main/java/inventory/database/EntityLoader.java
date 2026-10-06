package inventory.database;

import java.nio.file.Path;
import java.util.List;
import inventory.exception.StartupDataException;

/**
 * 엄격한 파일 읽기. 의미 검사·자동 수정·상태 게시·쓰기 없음.
 */
public class EntityLoader {
    /** 파일을 읽지 않고 로더만 생성합니다. */
    public EntityLoader() {
    }

    /**
     * UTF-8 REPORT 디코더로 읽고 BOM 거절. 기획서 원판 5.1절: LF·CR·CRLF 혼용 허용, CRLF는 개행 하나.
     * BufferedReader.readLine으로 행을 구분할 수 있습니다.
     * 0바이트=[]; 개행만=[""]; A+개행=[A]; A+개행 두 개=[A, ""].
     * 원문 공백·대소문자 유지. 자원 close 실패까지 시작 오류로 전달.
     * @param path 읽을 데이터 파일 절대 경로
     * @return 원문 행의 불변 목록; 마지막 정상 개행 하나는 추가 빈 행이 아님
     * @throws StartupDataException FILE_READ, FILE_UTF8 또는 FILE_BOM
     */
    public List<String> readStrictLines(Path path) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
