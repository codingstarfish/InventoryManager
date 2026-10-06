package inventory.database;

import java.nio.file.Path;
import inventory.exception.StartupDataException;
import java.util.Objects;

/**
 * 시작 파일 생성·접근 확인·경로 제공. 현재 작업 디렉터리 아래 data 기준.
 */
public class FileAccess {
    /** 실제 작업 디렉터리; 테스트에서는 별도 작업 경로 */
    private final Path workingDir;

    /**
     * 의존 부품을 보관합니다. 생성 시 파일 접근·입력·저장 없음.
     * @param workingDir 실제 작업 디렉터리; 테스트에서는 별도 작업 경로
     */
    public FileAccess(Path workingDir) {
        this.workingDir = Objects.requireNonNull(workingDir, "workingDir");
    }

    /**
     * workingDir의 data 디렉터리와 없는 두 데이터 파일만 생성. 파일은 0바이트.
     * 기존 파일 truncate 금지. data가 일반 파일이거나 대상 파일이 디렉터리이면 시작 오류.
     * 시작 시에만 호출하며 오류 전에 생성한 빈 파일도 유지.
     * @throws StartupDataException FILE_CREATE 또는 FILE_NOT_REGULAR; 실패 경로·cause 보존
     */
    public void ensureDataFiles() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 양쪽 경로가 일반 파일이고 읽기·쓰기 가능한지 확인.
     * 기존 파일 내용을 바꾸는 시험 쓰기 금지; 실제 저장 성공 보장은 아님.
     * @throws StartupDataException FILE_NOT_REGULAR, FILE_READ 또는 FILE_WRITE_ACCESS
     */
    public void checkReadableWritable() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR).resolve(DataPaths.LOGICAL_FILE) 기준.
     * @return 작업 디렉터리의 data 아래 논리 파일의 절대 경로
     */
    public Path logicalPath() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR).resolve(DataPaths.PHYSICAL_FILE) 기준.
     * @return 작업 디렉터리의 data 아래 물리 파일의 절대 경로
     */
    public Path physicalPath() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
