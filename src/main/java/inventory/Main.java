package inventory;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import inventory.validation.FileIntegrityValidator;
import inventory.validation.InputValidator;
import inventory.util.ConsoleView;
import inventory.util.Console;
import inventory.util.Input;
import inventory.service.QueryService;
import inventory.service.StockService;
import inventory.service.ProductService;
import inventory.repository.PhysicalItemRepository;
import inventory.repository.LogicalItemRepository;
import inventory.exception.StartupDataException;
import inventory.dto.InventorySnapshot;
import inventory.database.CommitCoordinator;
import inventory.database.InventoryDatabase;
import inventory.database.EntityWriter;
import inventory.database.EntityParser;
import inventory.database.EntityLoader;
import inventory.database.StartupLoader;
import inventory.database.FileAccess;
import java.io.IOException;

/**
 * 실행 진입점. 부품 생성·주입과 실제 프로세스 종료 코드 전달만 담당.
 */
public class Main {
    /**
     * 조립 순서:
     * 1. UTF-8 System.in BufferedReader와 UTF-8 System.out PrintWriter로 Input·ConsoleView 구성.
     * 2. Path.of("").toAbsolutePath()로 FileAccess, EntityLoader, EntityParser,
     *    FileIntegrityValidator, StartupLoader 생성.
     * 3. view.startupChecking() → startup.load() → view.startupPassed().
     * 4. 성공한 상태로 InventoryDatabase 하나, EntityWriter 하나, CommitCoordinator 하나 생성.
     * 5. 같은 db로 두 Repository 및 ProductService·StockService·QueryService 주입.
     * 6. Console(input, validator, productService, stockService, queryService, view).run()
     *    반환 코드를 System.exit에 전달.
     * 시작 경계의 StartupDataException은 view.startupFailed(e) 뒤 System.exit(1).
     * System.exit는 이 클래스에서만. IOException은 지원 범위 밖 입력 장치 오류로 전달.
     * 기획서 원판 2.2·4.8.1·5절: 두 파일은 작업 디렉터리의 data 하위. 최종 저장 없음.
     * @param args 명령행 인자; 현재 기획에는 별도 옵션 없음
     * @throws IOException 표준 입력 장치 장애
     */
    public static void main(String[] args) throws IOException {
        Input input = new Input(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)));
        ConsoleView view = new ConsoleView(new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true));
        FileAccess access = new FileAccess(Path.of("").toAbsolutePath());
        StartupLoader startup = new StartupLoader(access, new EntityLoader(), new EntityParser(), new FileIntegrityValidator());
        int exitCode;
        view.startupChecking();
        try {
            InventorySnapshot initial = startup.load();
            view.startupPassed();
            InventoryDatabase db = new InventoryDatabase(initial);
            EntityWriter writer = new EntityWriter(access.logicalPath(), access.physicalPath());
            CommitCoordinator commit = new CommitCoordinator(db, writer);
            LogicalItemRepository logicalRepo = new LogicalItemRepository(db);
            PhysicalItemRepository physicalRepo = new PhysicalItemRepository(db);
            ProductService products = new ProductService(db, logicalRepo, commit);
            StockService stock = new StockService(db, logicalRepo, physicalRepo, commit);
            QueryService query = new QueryService(logicalRepo, physicalRepo);
            exitCode = new Console(input, new InputValidator(), products, stock, query, view).run();
        } catch (StartupDataException e) {
            view.startupFailed(e);
            exitCode = 1;
        }
        System.exit(exitCode);
    }
}
