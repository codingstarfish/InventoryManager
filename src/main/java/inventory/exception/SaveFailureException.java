package inventory.exception;

import java.util.Objects;
import inventory.literal.DataPaths;

/**
 * EntityWriter에서 발생, Console.run에서 안내 후 종료 1. 실패 시 파일 보존은 보장하지 않음.
 */
public class SaveFailureException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 저장에 실패한 대상 파일명. */
    private final String fileName;

    /**
     * 저장·flush·close 또는 저장 접근 실패의 원인을 보존합니다.
     * @param fileName logical_item.txt 또는 physical_item.txt
     * @param cause null이 아닌 원인 예외
     */
    public SaveFailureException(String fileName, Throwable cause) {
        super(Objects.requireNonNull(fileName, "fileName") + " 저장 실패", Objects.requireNonNull(cause, "cause"));
        this.fileName = fileName;
        if (!fileName.equals(DataPaths.LOGICAL_FILE) && !fileName.equals(DataPaths.PHYSICAL_FILE)) {
            throw new IllegalArgumentException("데이터 파일명이 아닙니다: " + fileName);
        }
    }

    /**
     * 실패한 저장 대상 파일을 반환합니다.
     * @return 논리 또는 물리 파일명
     */
    public String fileName() {
        return fileName;
    }
}
