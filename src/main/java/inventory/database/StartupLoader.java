package inventory.database;

import inventory.literal.ErrorCode;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.OptionalInt;
import java.util.ArrayList;
import java.nio.file.Path;
import java.util.List;
import inventory.exception.StartupDataException;
import inventory.validation.FileIntegrityValidator;
import inventory.dto.InventorySnapshot;

/**
 * 시작 단계 조율. 생성자는 파일을 읽지 않습니다.
 */
public class StartupLoader {
    /** 시작 생성·접근 및 경로 */
    private final FileAccess access;
    /** 문법 단계 변환 */
    private final EntityParser parser;
    /** 의미·관계 검사 */
    private final FileIntegrityValidator validator;

    public StartupLoader(FileAccess access) {
        this.access = access;
        this.parser = new EntityParser();
        this.validator = new FileIntegrityValidator();
    }

    /**
     * ensureDataFiles → checkReadableWritable → 양쪽 readStrictLines 및 전체 문법 검사
     * → validateAndBuild → 정상 상태 반환.
     * 양쪽 파일 문법 검사가 끝나기 전 의미 오류를 보고하지 않습니다.
     * 전체 성공 뒤에만 Main에서 InventoryDatabase 생성.
     * @return 시작 무결성 검사를 통과한 불변 전체 상태
     * @throws StartupDataException 구체적인 생성·접근·문법·의미 오류 하나; 기존 파일 미변경
     */
    public InventorySnapshot load() {
        access.ensureDataFiles();
        access.checkReadableWritable();
        List<String> logicalLines = readStrictLines(access.logicalPath());
        List<String> physicalLines = readStrictLines(access.physicalPath());
        List<RawLogicalRow> logicalRows = new ArrayList<>();
        List<RawPhysicalRow> physicalRows = new ArrayList<>();
        for (int i = 0; i < logicalLines.size(); i++) {
            logicalRows.add(parser.parseLogicalSyntax(logicalLines.get(i), i + 1));
        }
        for (int i = 0; i < physicalLines.size(); i++) {
            physicalRows.add(parser.parsePhysicalSyntax(physicalLines.get(i), i + 1));
        }
        return validator.validateAndBuild(logicalRows, physicalRows);
    }

    /**
     * UTF-8 REPORT 디코더로 읽고 파일 시작의 U+FEFF만 BOM으로 거절합니다.
     * 중간의 U+FEFF는 원문에 유지하여 EntityParser에서 해당 필드의 문법 오류로 처리합니다.
     * 기획서 원판 5.1절: LF·CR·CRLF 혼용 허용, CRLF는 개행 하나.
     * BufferedReader.readLine으로 행을 구분할 수 있습니다.
     * 0바이트=[]; 개행만=[""]; A+개행=[A]; A+개행 두 개=[A, ""].
     * 원문 공백·대소문자 유지. 자원 close 실패까지 시작 오류로 전달.
     * @param path 읽을 데이터 파일 절대 경로
     * @return 원문 행 목록; 마지막 정상 개행 하나는 추가 빈 행이 아님
     * @throws StartupDataException FILE_READ, FILE_UTF8 또는 FILE_BOM
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
                    throw new StartupDataException(List.of(name), ErrorCode.FILE_BOM,
                            "BOM 없는 UTF-8 파일이어야 합니다.", OptionalInt.of(lines.size() + 1));
                }
                lines.add(line);
            }
        } catch (CharacterCodingException e) {
            throw new StartupDataException(List.of(name), ErrorCode.FILE_UTF8,
                    "올바른 UTF-8 파일이어야 합니다.", OptionalInt.empty(), e);
        } catch (IOException | SecurityException e) {
            throw new StartupDataException(List.of(name), ErrorCode.FILE_READ,
                    "데이터 파일을 읽을 수 없습니다.", OptionalInt.empty(), e);
        }
        return lines;
    }
}
