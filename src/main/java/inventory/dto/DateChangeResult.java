package inventory.dto;
import java.util.List;
import java.time.LocalDate;
public record DateChangeResult(LocalDate newDate, List<DiscardedLine> discarded) {
 public DateChangeResult { discarded=List.copyOf(discarded); }
}
