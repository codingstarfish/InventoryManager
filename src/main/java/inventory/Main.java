package inventory;

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
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
