package inventory.database;

import inventory.literal.ErrorCode;
import inventory.literal.DataPaths;
import java.util.OptionalInt;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
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
        Path data = workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR);
        try {
            Files.createDirectories(data);
        } catch (IOException | SecurityException e) {
            throw new StartupDataException(List.of(DataPaths.DATA_DIR), ErrorCode.FILE_CREATE,
                    "데이터 디렉터리나 빈 데이터 파일을 생성할 수 없습니다.", OptionalInt.empty(), e);
        }
        for (Path path : List.of(logicalPath(), physicalPath())) {
            try {
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
                if (!Files.isRegularFile(path)) {
                    throw new StartupDataException(List.of(path.getFileName().toString()),
                            ErrorCode.FILE_NOT_REGULAR, "데이터 파일 경로가 일반 파일이 아닙니다.", OptionalInt.empty());
                }
            } catch (IOException | SecurityException e) {
                throw new StartupDataException(List.of(path.getFileName().toString()), ErrorCode.FILE_CREATE,
                        "데이터 디렉터리나 빈 데이터 파일을 생성할 수 없습니다.", OptionalInt.empty(), e);
            }
        }
    }

    /**
     * 양쪽 경로가 일반 파일이고 읽기·쓰기 가능한지 확인.
     * 기존 파일 내용을 바꾸는 시험 쓰기 금지; 실제 저장 성공 보장은 아님.
     * @throws StartupDataException FILE_NOT_REGULAR, FILE_READ 또는 FILE_WRITE_ACCESS
     */
    public void checkReadableWritable() {
        for (Path path : List.of(logicalPath(), physicalPath())) {
            String fileName = path.getFileName().toString();
            ErrorCode stage = ErrorCode.FILE_READ;
            try {
                if (!Files.isRegularFile(path)) {
                    throw new StartupDataException(List.of(fileName), ErrorCode.FILE_NOT_REGULAR,
                            "데이터 파일 경로가 일반 파일이 아닙니다.", OptionalInt.empty());
                }
                if (!Files.isReadable(path)) {
                    throw new StartupDataException(List.of(fileName), ErrorCode.FILE_READ,
                            "데이터 파일을 읽을 수 없습니다.", OptionalInt.empty());
                }
                stage = ErrorCode.FILE_WRITE_ACCESS;
                if (!Files.isWritable(path)) {
                    throw new StartupDataException(List.of(fileName), stage,
                            "데이터 파일에 쓸 수 없습니다.", OptionalInt.empty());
                }
            } catch (SecurityException e) {
                throw new StartupDataException(List.of(fileName), stage,
                        stage == ErrorCode.FILE_READ ? "데이터 파일을 읽을 수 없습니다."
                                : "데이터 파일에 쓸 수 없습니다.", OptionalInt.empty(), e);
            }
        }
    }

    /**
     * workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR).resolve(DataPaths.LOGICAL_FILE) 기준.
     * @return 작업 디렉터리의 data 아래 논리 파일의 절대 경로
     */
    public Path logicalPath() {
        return workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR).resolve(DataPaths.LOGICAL_FILE);
    }

    /**
     * workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR).resolve(DataPaths.PHYSICAL_FILE) 기준.
     * @return 작업 디렉터리의 data 아래 물리 파일의 절대 경로
     */
    public Path physicalPath() {
        return workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR).resolve(DataPaths.PHYSICAL_FILE);
    }
}
