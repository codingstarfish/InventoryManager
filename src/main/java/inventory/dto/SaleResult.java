package inventory.dto;
import java.util.List;
public record SaleResult(List<SaleLine> lines, long totalAmount, int currentAfter) {
 public SaleResult { lines=List.copyOf(lines); }
}
