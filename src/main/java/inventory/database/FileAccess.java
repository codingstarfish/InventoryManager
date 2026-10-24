package inventory.database;

import inventory.literal.DataPaths;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import inventory.exception.StartupDataException;

/**
 * 시작 파일 생성,접근 확인,경로 제공함,  현재 작업 디렉터리는 /data 기준.
 */
public class FileAccess {
    /** 실제 작업 디렉터리 */
    private final Path workingDir;

    /**
     * @param workingDir 프로그램을 실행한 작업 디렉터리
     */
    public FileAccess(Path workingDir) {
        this.workingDir = workingDir;
    }

    /**
     *존재하지 않는 두 데이터 파일만 생성, 기존 파일은 유지
     * @throws StartupDataException 파일 생성·접근 또는 문법·의미 검사에서 오류를 확인하면 던짐
     */
    public void ensureDataFiles() {
        Path data = workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR);
        try {
            Files.createDirectories(data);
        } catch (IOException | SecurityException e) {
            throw new StartupDataException(List.of(DataPaths.DATA_DIR),
                    "데이터 디렉터리나 빈 데이터 파일을 생성할 수 없습니다.", e);
        }
        for (Path path : List.of(logicalPath(), physicalPath())) {
            try {
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
            } catch (IOException | SecurityException e) {
                throw new StartupDataException(List.of(path.getFileName().toString()),
                        "데이터 디렉터리나 빈 데이터 파일을 생성할 수 없습니다.", e);
            }
        }
    }

    /**
     * 두 데이터 경로가 일반 파일이며 읽기와 쓰기가 가능한지 확인
     * @throws StartupDataException 파일 생성·접근 또는 문법·의미 검사에서 오류를 확인하면 던짐
     */
    public void checkReadableWritable() {
        for (Path path : List.of(logicalPath(), physicalPath())) {
            String fileName = path.getFileName().toString();
            String reason = "데이터 파일을 읽을 수 없습니다.";
            try {
                if (!Files.isRegularFile(path)) {
                    throw new StartupDataException(List.of(fileName), "데이터 파일 경로가 일반 파일이 아닙니다.");
                }
                if (!Files.isReadable(path)) {
                    throw new StartupDataException(List.of(fileName), reason);
                }
                reason = "데이터 파일에 쓸 수 없습니다.";
                if (!Files.isWritable(path)) {
                    throw new StartupDataException(List.of(fileName), reason);
                }
            } catch (SecurityException e) {
                throw new StartupDataException(List.of(fileName), reason, e);
            }
        }
    }

    /**
     * 작업 디렉터리를 기준으로 논리 파일의 위치를 계산
     * @return 작업 디렉터리/data/logical_item.txt에 해당하는 절대 경로
     */
    public Path logicalPath() {
        return workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR).resolve(DataPaths.LOGICAL_FILE);
    }

    /**
     * 작업 디렉터리를 기준으로 물리 파일의 위치를 계산합니다.
     * @return 작업 디렉터리/data/physical_item.txt에 해당하는 절대 경로
     */
    public Path physicalPath() {
        return workingDir.toAbsolutePath().resolve(DataPaths.DATA_DIR).resolve(DataPaths.PHYSICAL_FILE);
    }
}
