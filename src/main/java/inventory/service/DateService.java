package inventory.service;
import inventory.database.InventoryDatabase;
import inventory.dto.*;
import inventory.entity.*;
import inventory.validation.DomainRules;
import inventory.literal.ErrorCode;
import inventory.exception.BusinessRuleException;
import java.time.LocalDate;
import java.util.*;
public class DateService {
 private final InventoryDatabase db;
 public DateService(InventoryDatabase db) { this.db=db; }
 public Optional<LocalDate> currentDate() { return db.currentDate(); }
 public void initialize(LocalDate day) {
  if(db.currentDate().isPresent()) throw new IllegalStateException("날짜가 이미 설정되어 있습니다.");
  db.saveDate(day);
 }
 public DateChangeResult advance(LocalDate day) {
  if(day.isBefore(db.currentDate().orElseThrow())) throw new BusinessRuleException(ErrorCode.DATE_BACKWARD,Map.of());
  var discarded=new ArrayList<DiscardedLine>();
  for(var item:db.findAllOrdered()) {
   var rows=db.findPhysicalItems(item.code());
   for(int i=0;i<rows.size();i++) {
    var row=rows.get(i); var expiry=DomainRules.expiry(item,row);
    if(row.state()==ItemState.IN_STOCK && expiry.filter(d->d.isBefore(day)).isPresent()) {
     discarded.add(new DiscardedLine(new PhysicalKey(row.logicalCode(),row.suffix()),expiry.orElseThrow()));
     rows.set(i,row.discard());
    }
   }
  }
  if(!discarded.isEmpty()) db.savePhysical();
  db.saveDate(day);
  return new DateChangeResult(day,discarded);
 }
}
