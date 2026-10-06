package inventory.database;

import inventory.validation.DomainRules;
import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.util.Comparator;
import java.nio.file.Path;
import java.util.List;
import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import inventory.exception.SaveFailureException;
import java.util.Objects;

/**
 * 파일 기록의 단일 소유자. 생성자는 파일 미변경. 실패 시 파일 보존·복구 보장 없음.
 */
public class EntityWriter {
    /** 시작 시 확정한 논리 파일 경로 */
    private final Path logicalPath;
    /** 시작 시 확정한 물리 파일 경로 */
    private final Path physicalPath;

    /**
     * 의존 부품을 보관합니다. 생성 시 파일 접근·입력·저장 없음.
     * @param logicalPath 시작 시 확정한 논리 파일 경로
     * @param physicalPath 시작 시 확정한 물리 파일 경로
     */
    public EntityWriter(Path logicalPath, Path physicalPath) {
        this.logicalPath = Objects.requireNonNull(logicalPath, "logicalPath");
        this.physicalPath = Objects.requireNonNull(physicalPath, "physicalPath");
    }

    /**
     * 전체 논리 레코드 code순 정렬 → name,code,size,price로 재기록.
     * UTF-8 BOM 없음, 기획서 원판 5.7절에 따라 System.lineSeparator() 사용.
     * WRITE, TRUNCATE_EXISTING만 사용; CREATE/append 없음.
     * 각 행 뒤 실행 환경의 개행 한 개, 빈 목록은 0바이트. close까지 성공해야 반환.
     * Entity.toString() 사용 금지. 물리 파일은 변경하지 않습니다.
     * @param items 검증된 전체 논리 레코드, 순서는 자유
     * @throws SaveFailureException 기록·flush·close IOException 또는 접근 SecurityException; 파일명과 cause 보존
     */
    public void writeLogical(List<LogicalItem> items) {
        List<LogicalItem> ordered = List.copyOf(items).stream()
                .sorted(Comparator.comparing(LogicalItem::code)).toList();
        List<String> lines = ordered.stream().map(item -> item.name() + "," + item.code()
                + "," + item.size() + "," + item.price()).toList();
        writeLines(logicalPath, lines);
    }

    /**
     * 판매 완료 포함 모든 낱개를 logicalCode 다음 suffix순으로 정렬.
     * logicalCode,세 자리 suffix,0/1 형태, UTF-8 BOM 없음, System.lineSeparator() 사용.
     * Locale.ROOT 고정 자리 포맷, true/false 문자열 금지.
     * WRITE, TRUNCATE_EXISTING만 사용; CREATE/append 없음. 각 행 뒤 실행 환경의 개행 한 개.
     * close까지 성공해야 반환. 논리 파일은 변경하지 않습니다.
     * @param items 검증된 전체 낱개, 입력 순서는 자유
     * @throws SaveFailureException 기록·flush·close IOException 또는 접근 SecurityException; 파일명과 cause 보존
     */
    public void writePhysical(List<PhysicalItem> items) {
        List<PhysicalItem> ordered = List.copyOf(items).stream()
                .sorted(Comparator.comparing(PhysicalItem::logicalCode).thenComparingInt(PhysicalItem::suffix)).toList();
        List<String> lines = ordered.stream().map(item -> item.logicalCode() + ","
                + DomainRules.formatSuffix(item.suffix()) + "," + (item.sold() ? "1" : "0")).toList();
        writeLines(physicalPath, lines);
    }

    private void writeLines(Path path, List<String> lines) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        } catch (IOException | SecurityException e) {
            throw new SaveFailureException(path.getFileName().toString(), e);
        }
    }
}
