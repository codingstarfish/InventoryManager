package inventory.entity;
import java.time.LocalDate;
import java.util.Optional;
public record PhysicalItem(String logicalCode, int suffix, ItemState state, LocalDate producedOn,
 Optional<LocalDate> soldOn, boolean refunded) {
 public String physicalCode() { return new PhysicalKey(logicalCode,suffix).toString(); }
 public boolean sold() { return state==ItemState.SOLD; }
 public PhysicalItem markSold(LocalDate date) { return new PhysicalItem(logicalCode,suffix,ItemState.SOLD,producedOn,Optional.of(date),refunded); }
 public PhysicalItem refund() { return new PhysicalItem(logicalCode,suffix,ItemState.IN_STOCK,producedOn,Optional.empty(),true); }
 public PhysicalItem discard() { return new PhysicalItem(logicalCode,suffix,ItemState.DISCARDED,producedOn,Optional.empty(),refunded); }
}
