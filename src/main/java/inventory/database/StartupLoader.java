package inventory.database;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.nio.file.Path;
import java.util.List;
import java.util.Comparator;
import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import inventory.exception.StartupDataException;
import inventory.validation.FileIntegrityValidator;

/**
 * 시작 단계
 */
public class StartupLoader {
    /** 시작 생성,접근 및 경로 */
    private final FileAccess access;
    /** 문법 단계 변환 */
    private final EntityParser parser;
    /** 의미·관계 검사 */
    private final FileIntegrityValidator validator;

    /**
     * 파일 접근 도구를 보관하고 문법,의미 검증기를 생성
     * @param access 작업 디렉터리 아래의 데이터 경로와 생성,접근 검사를 제공하는 도구
     */
    public StartupLoader(FileAccess access) {
        this.access = access;
        this.parser = new EntityParser();
        this.validator = new FileIntegrityValidator();
    }

    /**
     * 없는 파일 생성과 접근 확인 후 두 파일의 문법을 모두 검사하고 의미·관계 검사를 수행,
     * 검사 통과한 데이터만 적재하며 낱개 목록은 접미번호순으로 정렬
     * @param db 검사가 완료된 상품과 낱개 행을 적재할 빈 공유 데이터베이스
     * @throws StartupDataException 파일 생성,접근 또는 문법,의미 검사에서 오류를 확인하면 던짐
     */
    public void load(InventoryDatabase db) {
        access.ensureDataFiles();
        access.checkReadableWritable();
        List<String> logicalLines = readStrictLines(access.logicalPath());
        List<String> physicalLines = readStrictLines(access.physicalPath());
        List<RawLogicalRow> logicalRows = new ArrayList<>();
        List<RawPhysicalRow> physicalRows = new ArrayList<>();
        for (String line : logicalLines) {
            logicalRows.add(parser.parseLogicalSyntax(line));
        }
        for (String line : physicalLines) {
            physicalRows.add(parser.parsePhysicalSyntax(line));
        }
        validator.validate(logicalRows, physicalRows);
        for (RawLogicalRow row : logicalRows) {
            db.addItem(new LogicalItem(row.code(), row.name(), row.size(), row.price()));
        }
        for (RawPhysicalRow row : physicalRows) {
            db.findPhysicalItems(row.logicalCode())
                    .add(new PhysicalItem(row.logicalCode(), row.suffix(), row.sold()));
        }
        for (RawLogicalRow row : logicalRows) {
            db.findPhysicalItems(row.code()).sort(Comparator.comparingInt(PhysicalItem::suffix));
        }
    }

    /**
     * 파일을 엄격한 UTF-8로 읽고 파일 시작의 BOM을 거절
     * LF·CR·CRLF 혼용을 허용하며 원문 공백과 대소문자를 유지
     * @param path 읽을 논리 파일 또는 물리 파일의 절대 경로.
     * @return 개행 문자를 제외한 원문 행 목록, 0바이트 파일은 빈 목록이고 마지막 정상 개행 하나는 추가 빈 행으로 세지 않음
     * @throws StartupDataException 파일 읽기,접근에 실패하거나 UTF-8 또는 BOM 규칙을 위반하면 던짐
     */
    private List<String> readStrictLines(Path path) {
        String name = path.getFileName().toString();
        List<String> lines = new ArrayList<>();
        var decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(path), decoder))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (lines.isEmpty() && line.startsWith("\uFEFF")) {
                    throw new StartupDataException(List.of(name), "BOM 없는 UTF-8 파일이어야 합니다.");
                }
                lines.add(line);
            }
        } catch (CharacterCodingException e) {
            throw new StartupDataException(List.of(name), "올바른 UTF-8 파일이어야 합니다.", e);
        } catch (IOException | SecurityException e) {
            throw new StartupDataException(List.of(name), "데이터 파일을 읽을 수 없습니다.", e);
        }
        return lines;
    }
}
