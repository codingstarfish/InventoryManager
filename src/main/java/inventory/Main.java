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

public class Main {
    
    /**
     * 재고 관리 프로그램을 시작한다.
     *
     * @param args 명령행 인자. 현재는 사용하지 않음
     * @throws IOException 콘솔 입력을 읽는 중 입출력 오류가 발생한 경우
     * @throws IllegalStateException 검증된 주 메뉴 번호가 1~5가 아닌 경우
     * @throws inventory.exception.BusinessRuleException 콘솔 입력 단계에서 처리하지 않는 업무 오류가 전파된 경우
     */
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
