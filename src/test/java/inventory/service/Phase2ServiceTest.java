package inventory.service;

import inventory.database.*;
import inventory.dto.*;
import inventory.entity.*;
import inventory.exception.*;
import inventory.literal.ErrorCode;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/** Deterministic storage failures and business boundaries; no external test library. */
public final class Phase2ServiceTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 13);
    private static final class Writer extends EntityWriter {
        final List<String> calls = new ArrayList<>();
        String fail;
        Writer() { super(Path.of("unused-logical"), Path.of("unused-physical")); }
        private void record(String kind) {
            calls.add(kind);
            if (kind.equals(fail)) throw new SaveFailureException(kind, new IOException("injected"));
        }
        @Override public void writePhysical(List<PhysicalItem> rows) { record("physical"); }
        @Override public void writeDate(LocalDate day) { record("date"); }
    }
    private static InventoryDatabase database(Writer writer, int shelf) {
        var db = new InventoryDatabase(writer);
        db.loadDate(Optional.of(DAY));
        db.addItem(new LogicalItem("P00001", "Milk", 1000, 30, shelf));
        return db;
    }
    private static PhysicalItem row(String code, int suffix, ItemState state) {
        return new PhysicalItem(code, suffix, state, DAY.minusDays(1),
                state == ItemState.SOLD ? Optional.of(DAY) : Optional.empty(), false);
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static void expect(ErrorCode code, Runnable operation) {
        try { operation.run(); throw new AssertionError("expected " + code); }
        catch (BusinessRuleException e) { check(e.code() == code, e.code().name()); }
    }
    public static void main(String[] args) {
        for (String failure : List.of("physical", "date")) {
            var writer = new Writer(); writer.fail = failure;
            var db = database(writer, 1);
            db.findPhysicalItems("P00001").add(row("P00001", 1, ItemState.IN_STOCK));
            try { new DateService(db).advance(DAY.plusDays(1)); throw new AssertionError("expected failure"); }
            catch (SaveFailureException expected) { /* asserted below */ }
            check(db.currentDate().orElseThrow().equals(DAY), "failed save advanced date");
            check(writer.calls.equals(failure.equals("physical") ? List.of("physical") : List.of("physical", "date")), "write order");
        }
        var writer = new Writer(); var db = database(writer, 1);
        var dates = new DateService(db);
        dates.advance(DAY);
        check(writer.calls.equals(List.of("date")), "no discard must save only date");
        expect(ErrorCode.DATE_BACKWARD, () -> dates.advance(DAY.minusDays(1)));
        writer = new Writer(); db = database(writer, 0);
        var rows = db.findPhysicalItems("P00001");
        for (int i = 1; i <= 998; i++) rows.add(row("P00001", i, ItemState.SOLD));
        var stock = new StockService(db); var summary = db.summarize("P00001");
        expect(ErrorCode.SUFFIX_SHORTAGE, () -> stock.validateReceiptQuantity(summary, 2));
        stock.receive(summary, 1, DAY);
        check(db.summarize("P00001").issuable() == 0 && rows.get(998).suffix() == 999, "suffix boundary");
        final var fullDb = database(new Writer(), 0);
        fullDb.addItem(new LogicalItem("P00002", "Bread", 1000, 10, 0));
        for (int i = 1; i <= 999; i++) fullDb.findPhysicalItems("P00001").add(row("P00001", i, ItemState.IN_STOCK));
        fullDb.findPhysicalItems("P00002").add(row("P00002", 1, ItemState.SOLD));
        var fullStock = new StockService(fullDb);
        fullStock.refund(new PhysicalKey("P00002", 1));
        check(fullDb.warehouse().remaining() == 0, "exact capacity refund");
        fullDb.findPhysicalItems("P00002").add(row("P00002", 2, ItemState.SOLD));
        expect(ErrorCode.CAPACITY_SHORTAGE, () -> fullStock.refund(new PhysicalKey("P00002", 2)));
        check(fullDb.findPhysicalItems("P00002").get(1).sold(), "failed refund changed row");
        System.out.println("PASS save ordering, unchanged date on failure, suffix and capacity boundaries");
    }
}
