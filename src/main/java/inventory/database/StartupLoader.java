package inventory.database;

import java.util.ArrayList;
import java.util.List;
import inventory.validation.FileIntegrityValidator;
import inventory.dto.InventorySnapshot;
import inventory.exception.StartupDataException;
import java.util.Objects;

/**
 * 시작 단계 조율. 생성자는 파일을 읽지 않습니다.
 */
public class StartupLoader {
    /** 시작 생성·접근 및 경로 */
    private final FileAccess access;
    /** 엄격한 원문 읽기 */
    private final EntityLoader loader;
    /** 문법 단계 변환 */
    private final EntityParser parser;
    /** 의미·관계 검사 */
    private final FileIntegrityValidator validator;

    /**
     * 의존 부품을 보관합니다. 생성 시 파일 접근·입력·저장 없음.
     * @param access 시작 생성·접근 및 경로
     * @param loader 엄격한 원문 읽기
     * @param parser 문법 단계 변환
     * @param validator 의미·관계 검사
     */
    public StartupLoader(FileAccess access, EntityLoader loader, EntityParser parser, FileIntegrityValidator validator) {
        this.access = Objects.requireNonNull(access, "access");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.parser = Objects.requireNonNull(parser, "parser");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    /**
     * ensureDataFiles → checkReadableWritable → 양쪽 readStrictLines 및 전체 문법 검사
     * → ParsedFiles → validateAndBuild → 정상 상태 반환.
     * 양쪽 파일 문법 검사가 끝나기 전 의미 오류를 보고하지 않습니다.
     * 전체 성공 뒤에만 Main에서 InventoryDatabase 생성.
     * @return 시작 무결성 검사를 통과한 불변 전체 상태
     * @throws StartupDataException 구체적인 생성·접근·문법·의미 오류 하나; 기존 파일 미변경
     */
    public InventorySnapshot load() {
        access.ensureDataFiles();
        access.checkReadableWritable();
        List<String> logicalLines = loader.readStrictLines(access.logicalPath());
        List<String> physicalLines = loader.readStrictLines(access.physicalPath());
        List<RawLogicalRow> logicalRows = new ArrayList<>();
        List<RawPhysicalRow> physicalRows = new ArrayList<>();
        for (int i = 0; i < logicalLines.size(); i++) {
            logicalRows.add(parser.parseLogicalSyntax(logicalLines.get(i), i + 1));
        }
        for (int i = 0; i < physicalLines.size(); i++) {
            physicalRows.add(parser.parsePhysicalSyntax(physicalLines.get(i), i + 1));
        }
        return validator.validateAndBuild(new ParsedFiles(logicalRows, physicalRows));
    }
}
