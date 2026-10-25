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

// 시작할 때 파일을 검사하고, 통과한 데이터만 DB에 넣음
public class StartupLoader {
    /** 시작 생성,접근 및 경로 */
    private final FileAccess access;
    /** 문법 단계 변환 */
    private final EntityParser parser;
    /** 의미·관계 검사 */
    private final FileIntegrityValidator validator;

    /**
     * @param access 데이터 파일의 생성·접근 검사와 경로 조회를 담당하는 객체
     */
    public StartupLoader(FileAccess access) {
        this.access = access;
        this.parser = new EntityParser();
        this.validator = new FileIntegrityValidator();
    }

    /**
     * 파일 생성/접근 확인 → 두 파일 문법 검사 → 의미 검사 → DB에 적재
     * 검사 실패 시 StartupDataException. 적재 후 낱개는 접미번호순으로 정렬
     *
     * @param db 검사를 통과한 상품과 낱개 데이터를 추가할 DB
     * @throws StartupDataException 데이터 디렉터리·파일을 생성하거나 파일에 접근할 수 없거나,
     *         인코딩·BOM·행 형식 또는 값 범위·중복·번호 연속성·상품 참조·판매 순서·창고 용량 검사가 실패한 경우
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
     * UTF-8 오류와 파일 시작의 BOM을 거절. 공백과 대소문자는 그대로 읽음
     * LF/CR/CRLF 혼용 허용. 마지막 개행 하나는 빈 행으로 세지 않음
     * 읽기 실패나 인코딩/BOM 오류는 StartupDataException
     *
     * @param path 읽을 데이터 파일의 경로
     * @return 개행 문자를 제외하고 파일 순서대로 읽은 행 목록
     * @throws StartupDataException 파일을 읽거나 닫을 수 없거나, 올바른 UTF-8이 아니거나 첫 행에 BOM이 있는 경우
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
