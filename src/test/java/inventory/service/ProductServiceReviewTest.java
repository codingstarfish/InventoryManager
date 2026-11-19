package inventory.service;

import inventory.database.EntityWriter;
import inventory.database.InventoryDatabase;
import inventory.entity.LogicalItem;
import inventory.exception.SaveFailureException;
import inventory.util.Console;
import inventory.util.ConsoleView;
import inventory.util.Input;
import inventory.validation.InputValidator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Collection;

/** Standalone assertions, run by scripts/review_product_spec.py; no JUnit dependency. */
public final class ProductServiceReviewTest {
    private ProductServiceReviewTest() { }

    public static void main(String[] args) throws IOException {
        // Deterministic failure at the writer boundary, independent of OS permissions.
        EntityWriter failingWriter = new EntityWriter(Path.of("unused-logical"), Path.of("unused-physical")) {
            @Override
            public void writeLogical(Collection<LogicalItem> items) {
                throw new SaveFailureException("logical_item.txt", new IOException("injected write failure"));
            }
        };
        InventoryDatabase db = new InventoryDatabase(failingWriter);
        db.loadDate(java.util.Optional.of(java.time.LocalDate.of(2026,11,13)));
        StringWriter output = new StringWriter();
        Console console = new Console(new Input(new BufferedReader(new StringReader("1\nMilk\n1\n10\n0\n5\n"))),
                new InputValidator(), new ProductService(db), new StockService(db), new QueryService(db), new DateService(db),
                new ConsoleView(new PrintWriter(output, true)));
        check(console.run() == 1, "save failure must return exit code 1");
        String text = output.toString();
        check(text.contains("[오류] logical_item.txt 저장에 실패했습니다. 작업을 완료하지 못했습니다."), text);
        check(!text.contains("신규 상품이 등록되었습니다."), "success printed after failure");
        check(!text.contains("다시 입력하시겠습니까?"), "retry offered after failure");
        check(text.indexOf("ConvenienceStore>") == text.lastIndexOf("ConvenienceStore>"), "menu repeated after failure");

        // Observations of direct calls are not end-user specification failures:
        // register documents validated inputs; Console owns validation and the limit gate.
        EntityWriter noOpWriter = new EntityWriter(Path.of("unused-logical"), Path.of("unused-physical")) {
            @Override
            public void writeLogical(Collection<LogicalItem> items) { }
        };
        InventoryDatabase directDb = new InventoryDatabase(noOpWriter);
        ProductService service = new ProductService(directDb);
        LogicalItem invalid = service.register("!", 0, 15, 0);
        for (int i = 2; i <= 99999; i++) {
            directDb.addItem(new LogicalItem("P" + String.format(java.util.Locale.ROOT, "%05d", i), "Milk", 1, 10, 0));
        }
        check(!service.canRegister(), "canRegister must reject at 99999");
        LogicalItem overflow = service.register("Milk", 1, 10, 0);
        System.out.println("PASS save-failure handling; observations: direct register accepts invalid input/P100000; "
                + "invalid size=" + invalid.size() + ", price=" + invalid.price() + ", overflow code=" + overflow.code()
                + "; failed save leaves in-memory product count=" + db.size());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
