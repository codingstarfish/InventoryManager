package inventory.service;
import inventory.database.InventoryDatabase;
import inventory.dto.*;
import inventory.entity.*;
import inventory.exception.BusinessRuleException;
import inventory.literal.*;
import inventory.validation.DomainRules;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class StockService {
 private final InventoryDatabase db;
 public StockService(InventoryDatabase db) { this.db=db; }
 private void reject(ErrorCode code) { throw new BusinessRuleException(code,Map.of()); }
 public StockSummary selectInbound(String code) {
  var s=db.summarize(code);
  if(s.received()==Limits.MAX_SUFFIX) reject(ErrorCode.LIMIT_PHYSICAL_CODE);
  return s;
 }
 public StockSummary selectSale(String code) { return db.summarize(code); }
 public void validateReceiptQuantity(StockSummary summary,int n) {
  var s=db.summarize(summary.item().code());
  if(n>s.issuable()) throw new BusinessRuleException(ErrorCode.SUFFIX_SHORTAGE,Map.of("issuable",Integer.toString(s.issuable())));
  long required=(long)s.item().size()*n, remaining=db.warehouse().remaining();
  if(required>remaining) throw new BusinessRuleException(ErrorCode.CAPACITY_SHORTAGE,Map.of("required",Long.toString(required),"remaining",Long.toString(remaining)));
 }
 public StockChangeResult receive(StockSummary summary,int n,LocalDate produced) {
  validateReceiptQuantity(summary,n);
  var s=db.summarize(summary.item().code());
  var day=db.currentDate().orElseThrow();
  if(produced.isAfter(day)) reject(ErrorCode.FUTURE_PRODUCTION);
  if(s.item().shelfLifeDays()>0 && produced.plusDays(s.item().shelfLifeDays()).isBefore(day)) reject(ErrorCode.EXPIRED);
  var rows=db.findPhysicalItems(s.item().code());
  for(int i=1;i<=n;i++) rows.add(new PhysicalItem(s.item().code(),s.received()+i,ItemState.IN_STOCK,produced,Optional.empty(),false));
  db.savePhysical();
  return new StockChangeResult(n,s.current()+n);
 }
 public List<PhysicalItem> saleCandidates(String code) {
  var item=db.requireItem(code);
  Comparator<PhysicalItem> order=Comparator.comparingInt(PhysicalItem::suffix);
  if(item.shelfLifeDays()>0) order=Comparator.comparing((PhysicalItem r)->DomainRules.expiry(item,r).orElseThrow()).thenComparingInt(PhysicalItem::suffix);
  return db.findPhysicalItems(code).stream().filter(r->r.state()==ItemState.IN_STOCK).sorted(order).toList();
 }
 public SaleResult sell(StockSummary s,int n) {
  var candidates=saleCandidates(s.item().code());
  if(n>candidates.size()) throw new BusinessRuleException(ErrorCode.STOCK_SHORTAGE,Map.of("current",Integer.toString(candidates.size())));
  return completeSale(s.item(),candidates.subList(0,n));
 }
 private PhysicalItem requireUnit(PhysicalKey key) {
  var rows=db.findPhysicalItems(key.logicalCode());
  if(rows==null || key.suffix()<1 || key.suffix()>rows.size()) reject(ErrorCode.PHYSICAL_NOT_FOUND);
  return rows.get(key.suffix()-1);
 }
 public SaleResult sellOne(StockSummary s,PhysicalKey key) {
  if(!s.item().code().equals(key.logicalCode())) reject(ErrorCode.WRONG_PRODUCT);
  var row=requireUnit(key);
  if(row.state()!=ItemState.IN_STOCK) reject(ErrorCode.NOT_IN_STOCK);
  return completeSale(s.item(),List.of(row));
 }
 private SaleResult completeSale(LogicalItem item,List<PhysicalItem> chosen) {
  var lines=new ArrayList<SaleLine>();
  long total=0;
  for(var row:chosen) {
   long price=DomainRules.salePrice(item,row); total+=price;
   lines.add(new SaleLine(new PhysicalKey(row.logicalCode(),row.suffix()),item.price(),price,row.refunded()));
   db.findPhysicalItems(item.code()).set(row.suffix()-1,row.markSold(db.currentDate().orElseThrow()));
  }
  db.savePhysical();
  return new SaleResult(lines,total,db.summarize(item.code()).current());
 }
 public RefundResult refund(PhysicalKey key) {
  var row=requireUnit(key);
  if(!row.sold()) reject(ErrorCode.NOT_SOLD);
  var day=db.currentDate().orElseThrow();
  long elapsed=ChronoUnit.DAYS.between(row.soldOn().orElseThrow(),day);
  if(elapsed<0||elapsed>Limits.REFUND_DAYS) reject(ErrorCode.REFUND_PERIOD);
  var item=db.requireItem(key.logicalCode());
  if(DomainRules.expiry(item,row).filter(d->d.isBefore(day)).isPresent()) reject(ErrorCode.EXPIRED);
  if(row.refunded()) reject(ErrorCode.ALREADY_REFUNDED);
  if(item.size()>db.warehouse().remaining()) throw new BusinessRuleException(ErrorCode.CAPACITY_SHORTAGE,Map.of("required",Integer.toString(item.size()),"remaining",Long.toString(db.warehouse().remaining())));
  var refunded=row.refund();
  db.findPhysicalItems(key.logicalCode()).set(key.suffix()-1,refunded);
  db.savePhysical();
  return new RefundResult(key,item.price(),db.summarize(key.logicalCode()).current(),DomainRules.salePrice(item,refunded));
 }
}
