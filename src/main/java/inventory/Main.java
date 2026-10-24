package inventory;

import inventory.database.EntityWriter;
import inventory.database.FileAccess;
import inventory.database.InventoryDatabase;
import inventory.database.StartupLoader;
import inventory.exception.StartupDataException;
import inventory.service.ProductService;
import inventory.service.QueryService;
import inventory.service.StockService;
import inventory.util.Console;
import inventory.util.ConsoleView;
import inventory.util.Input;
import inventory.validation.InputValidator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** 시작 검사 → 공유 데이터와 서비스 구성 → 콘솔 실행. 프로세스 종료는 여기서만 합니다. */
public class Main {
    public static void main(String[] args) throws IOException {
        Input input = new Input(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)));
        ConsoleView view = new ConsoleView(new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true));
        FileAccess access = new FileAccess(Path.of("").toAbsolutePath());
        int exitCode;
        view.startupChecking();
        try {
            EntityWriter writer = new EntityWriter(access.logicalPath(), access.physicalPath());
            InventoryDatabase db = new InventoryDatabase(writer);
            new StartupLoader(access).load(db);
            view.startupPassed();
            exitCode = new Console(input,
                new InputValidator(),
                new ProductService(db),
                new StockService(db), 
                new QueryService(db), 
                view
            ).run();
        } catch (StartupDataException e) {
            view.startupFailed(e);
            exitCode = 1;
        }
        System.exit(exitCode);
    }
}
