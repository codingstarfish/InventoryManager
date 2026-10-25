package inventory.database;

import inventory.validation.DomainRules;
import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Collection;
import java.nio.file.Path;
import java.util.List;
import inventory.entity.LogicalItem;
import inventory.entity.PhysicalItem;
import inventory.exception.SaveFailureException;

// 데이터 파일 저장 담당
public class EntityWriter {
    /** 논리 파일 경로 */
    private final Path logicalPath;
    /** 물리 파일 경로 */
    private final Path physicalPath;

    public EntityWriter(Path logicalPath, Path physicalPath) {
        this.logicalPath = logicalPath;
        this.physicalPath = physicalPath;
    }

    // 전체 상품을 논리코드순으로 정렬해 논리 파일만 저장
    // 파일 형식: 상품명,논리코드,크기,가격
    public void writeLogical(Collection<LogicalItem> items) {
        List<LogicalItem> ordered = items.stream()
                .sorted(Comparator.comparing(LogicalItem::code)).toList();
        List<String> lines = ordered.stream().map(item -> item.name() + "," + item.code()
                + "," + item.size() + "," + item.price()).toList();
        writeLines(logicalPath, lines);
    }

    // 판매된 낱개도 포함해 논리코드, 접미번호순으로 물리 파일만 저장
    // 파일 형식: 논리코드,세 자리 접미번호,판매여부(0/1)
    public void writePhysical(List<PhysicalItem> items) {
        List<PhysicalItem> ordered = items.stream()
                .sorted(Comparator.comparing(PhysicalItem::logicalCode).thenComparingInt(PhysicalItem::suffix)).toList();
        List<String> lines = ordered.stream().map(item -> item.logicalCode() + ","
                + DomainRules.formatSuffix(item.suffix()) + "," + (item.sold() ? "1" : "0")).toList();
        writeLines(physicalPath, lines);
    }

    // 기존 파일을 BOM 없는 UTF-8로 덮어씀. 없는 파일은 여기서 생성하지 않음
    // 각 행 뒤에 기본 개행을 붙이고, 빈 목록이면 0바이트로 저장
    // 열기/쓰기/버퍼 반영/닫기 실패는 SaveFailureException으로 넘김
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
