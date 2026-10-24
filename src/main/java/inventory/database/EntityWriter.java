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

/**
 * 파일 기록의 단일 소유자임
 */
public class EntityWriter {
    /** 논리 파일 경로 */
    private final Path logicalPath;
    /** 물리 파일 경로 */
    private final Path physicalPath;

    /**
     * @param logicalPath 논리 상품 전체를 저장할 logical_item.txt의 경로
     * @param physicalPath 낱개 상품 전체를 저장할 physical_item.txt의 경로
     */
    public EntityWriter(Path logicalPath, Path physicalPath) {
        this.logicalPath = logicalPath;
        this.physicalPath = physicalPath;
    }

    /**
     * 전체 상품을 논리코드순으로 정렬하여 상품명,논리코드,크기,가격 형식으로 재기록한다. 물리 파일은 변경은 없음
     * @param items 검증된 전체 논리 상품 모음으로, 입력 순서는 자유이며 빈 모음도 허용
     * @throws SaveFailureException 파일 쓰기,버퍼 반영,닫기 또는 저장 접근이 실패하면 던짐
     */
    public void writeLogical(Collection<LogicalItem> items) {
        List<LogicalItem> ordered = items.stream()
                .sorted(Comparator.comparing(LogicalItem::code)).toList();
        List<String> lines = ordered.stream().map(item -> item.name() + "," + item.code()
                + "," + item.size() + "," + item.price()).toList();
        writeLines(logicalPath, lines);
    }

    /**
     * 전체 낱개를 논리코드와 접미번호순으로 정렬하여 논리코드,세자리접미번호,판매여부 형식으로 재기록, 논리 파일은 변경 X
     * @param items 판매 완료된 낱개까지 포함하는 검증된 전체 낱개 목록, 입력 순서는 자유, 빈 목록도 허용
     * @throws SaveFailureException 파일 쓰기,버퍼 반영,닫기, 저장 접근이 실패하면 던짐
     */
    public void writePhysical(List<PhysicalItem> items) {
        List<PhysicalItem> ordered = items.stream()
                .sorted(Comparator.comparing(PhysicalItem::logicalCode).thenComparingInt(PhysicalItem::suffix)).toList();
        List<String> lines = ordered.stream().map(item -> item.logicalCode() + ","
                + DomainRules.formatSuffix(item.suffix()) + "," + (item.sold() ? "1" : "0")).toList();
        writeLines(physicalPath, lines);
    }

    /**
     * 기존 대상 파일을 BOM 없는 UTF-8로 덮어씀
     * 각 행 뒤에는 실행 환경의 기본 개행을 하나 쓰며 빈 목록은 0바이트로 저장
     * @param path 시작 검사에서 존재와 접근 가능 여부를 확인한 저장 대상 파일 경로
     * @param lines 필드 조합과 정렬을 마친 전체 레코드 문자열 목록
     */
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
