package inventory.util;

import inventory.validation.DomainRules;
import inventory.literal.CodePurpose;
import inventory.literal.ErrorCode;
import inventory.literal.Field;
import inventory.dto.AllInventoryResult;
import inventory.dto.ItemDetail;
import inventory.entity.LogicalItem;
import inventory.dto.StockChangeResult;
import inventory.dto.StockSummary;
import inventory.exception.StartupDataException;
import inventory.exception.SaveFailureException;
import java.io.PrintWriter;
import java.util.Map;

// 출력 전용
public class ConsoleView {
    
    private final PrintWriter out;

    /**
     * 화면 안내를 기록할 출력 채널 보관
     *
     * @param out 프롬프트, 오류 안내, 처리 결과를 기록할 출력 채널
     */
    public ConsoleView(PrintWriter out) {
        this.out = out;
    }

    
    // 주 메뉴와 프롬프트 출력
    public void mainMenu() {
        out.println("==================================================");
        out.println("        편의점 재고 관리 시스템 (v1.0)");
        out.println("==================================================");
        out.println("1. 신규 상품 등록");
        out.println("2. 재고 입고");
        out.println("3. 상품 판매");
        out.println("4. 재고 조회");
        out.println("5. 종료");
        out.println("--------------------------------------------------");
        out.print("ConvenienceStore> ");
        out.flush();
    }

    /**
     * 입력 항목에 맞는 프롬프트 출력
     *
     * @param field 상품명·크기·가격·수량·조회 방식·재입력 확인 중 입력받을 항목
     */
    public void prompt(Field field) {
        String text = switch (field) {
            case NAME -> "신규 상품명";
            case SIZE -> "상품 크기(용량 단위)";
            case PRICE -> "판매 가격(원)";
            case INBOUND_QUANTITY -> "입고 수량";
            case SALE_QUANTITY -> "판매 수량";
            case QUERY_MODE -> "조회 방식(1: 전체, 2: 단일)";
            case RETRY -> "다시 입력하시겠습니까? (y/n)";
        };
        out.print("ConvenienceStore: " + text + " > ");
        out.flush();
    }

    /**
     * 작업에 맞는 상품코드 입력 프롬프트 출력
     *
     * @param purpose 논리코드를 입력받는 목적. INBOUND, SALE, QUERY 중 하나
     */
    public void promptCode(CodePurpose purpose) {
        String text = switch (purpose) {
            case INBOUND -> "입고할 논리적 상품코드";
            case SALE -> "판매할 기존 상품의 논리코드";
            case QUERY -> "조회할 기존 상품의 논리코드";
        };
        out.print("ConvenienceStore: " + text + " > ");
        out.flush();
    }

    /**
     * 오류 코드에 맞는 안내 출력
     *
     * @param code 입력 형식·값 범위·업무 조건의 위반을 나타내는 오류 코드
     * @param details 오류 문구에 넣을 수치. 접미번호 부족은 issuable, 용량 부족은
     *                required/remaining, 재고 부족은 current를 사용하며 나머지는 빈 Map
     */
    public void error(ErrorCode code, Map<String, Long> details) {
        String message = switch (code) {
            case MENU_SYNTAX -> "메뉴는 1~5 중 하나를 입력해야 합니다.";
            case NAME_SYNTAX -> "상품명은 완성형 한글, 영문, 숫자, 공백으로 구성된 1~30자여야 합니다.";
            case SIZE_SYNTAX -> "상품 크기는 숫자 1~10자리로 입력해야 합니다.";
            case SIZE_RANGE -> "상품 크기는 1~1000 용량 단위여야 합니다.";
            case PRICE_SYNTAX -> "가격은 숫자 1~10자리로 입력해야 합니다.";
            case PRICE_RANGE -> "가격은 10~10000000원이어야 합니다.";
            case PRICE_UNIT -> "가격은 10원 단위로 입력해야 합니다.";
            case CODE_SYNTAX -> "논리적 상품코드는 대문자 P와 숫자 5자리로 입력해야 합니다.";
            case CODE_RANGE -> "논리적 상품코드의 번호는 00001~99999여야 합니다.";
            case CODE_NOT_FOUND -> "존재하지 않는 논리적 상품코드입니다.";
            case LIMIT_LOGICAL_CODE -> "사용 가능한 논리적 상품코드가 없어 신규 상품을 등록할 수 없습니다.";
            case LIMIT_PHYSICAL_CODE -> "해당 상품의 물리적 상품코드가 소진되어 입고할 수 없습니다.";
            case INBOUND_SYNTAX -> "입고 수량은 숫자 1~10자리로 입력해야 합니다.";
            case INBOUND_RANGE -> "입고 수량은 1~100개여야 합니다.";
            case SUFFIX_SHORTAGE -> "물리적 상품코드가 부족합니다. (발급 가능 수: " + details.get("issuable") + "개)";
            case CAPACITY_SHORTAGE -> "창고의 남은 용량이 부족합니다. (필요 용량: "
                    + details.get("required") + ", 남은 용량: " + details.get("remaining") + ")";
            case SALE_SYNTAX -> "판매 수량은 숫자 1~10자리로 입력해야 합니다.";
            case SALE_RANGE -> "판매 수량은 1~100개여야 합니다.";
            case STOCK_SHORTAGE -> "재고가 부족합니다. (현재 재고: " + details.get("current") + "개)";
            case QUERY_MODE_SYNTAX -> "조회 방식은 1 또는 2를 입력해야 합니다.";
            case RETRY_SYNTAX -> "y 또는 n을 입력해야 합니다.";
        };
        line(message);
    }

    
    /**
     * 상품이 없어 입고/판매 불가할 때 안내. QUERY로 호출하면 예외
     *
     * @param purpose 수행할 수 없는 작업. INBOUND 또는 SALE만 허용
     */
    public void noProducts(CodePurpose purpose) {
        switch (purpose) {
            case INBOUND -> line("등록된 상품이 없어 입고할 수 없습니다. 먼저 상품을 등록해야 합니다.");
            case SALE -> line("등록된 상품이 없어 판매할 수 없습니다.");
            case QUERY -> throw new IllegalArgumentException("조회는 상품이 없어도 방식부터 선택합니다.");
        }
    }

    /**
     * 선택한 상품명과 현재 재고 출력
     *
     * @param summary 입고·판매 대상으로 선택한 상품 정보와 최신 수량 요약
     */
    public void selected(StockSummary summary) {
        line("상품명: " + summary.item().name() + ", 현재 재고: " + summary.current() + "개");
    }

    /**
     * 등록 완료와 신규 상품 정보 출력
     *
     * @param item 논리 파일에 저장을 마친 신규 등록 상품
     */
    public void registered(LogicalItem item) {
        line("신규 상품이 등록되었습니다.");
        line("논리코드: " + item.code() + ", 상품명: " + item.name()
                + ", 크기: " + item.size() + ", 가격: " + item.price() + "원, 현재 재고: 0개");
    }

    /**
     * 입고 수량과 입고 후 재고 출력
     *
     * @param result 저장에 성공한 입고 수량과 입고 후 현재 재고
     */
    public void received(StockChangeResult result) {
        line("입고가 완료되었습니다. (입고 수량: " + result.quantity()
                + "개, 현재 재고: " + result.currentAfter() + "개)");
    }

    /**
     * 판매 수량과 판매 후 재고 출력
     *
     * @param result 저장에 성공한 판매 수량과 판매 후 현재 재고
     */
    public void sold(StockChangeResult result) {
        line("판매가 완료되었습니다. (판매 수량: " + result.quantity()
                + "개, 현재 재고: " + result.currentAfter() + "개)");
    }

    /**
     * 상품별 요약과 창고 사용량/남은 용량 출력. 상품이 없으면 빈 목록 안내
     *
     * @param result 논리코드순 상품 요약 목록과 창고 용량을 담은 전체 조회 결과
     */
    public void allInventory(AllInventoryResult result) {
        if (result.items().isEmpty()) {
            line("등록된 상품이 없습니다.");
        }
        for (StockSummary summary : result.items()) {
            line(formatSummary(summary));
        }
        line("창고 사용 용량: " + result.warehouse().used() + ", 남은 용량: " + result.warehouse().remaining());
    }

    /**
     * 상품 요약과 낱개별 판매여부 출력. 입고 기록이 없으면 따로 안내
     *
     * @param result 선택 상품의 요약과 판매된 낱개도 포함한 접미번호순 낱개 목록
     */
    public void itemDetail(ItemDetail result) {
        line(formatSummary(result.summary()));
        if (result.physicalItems().isEmpty()) {
            line("입고된 물리적 상품이 없습니다.");
        }
        for (var item : result.physicalItems()) {
            line("물리코드: " + item.physicalCode() + ", 판매여부: " + (item.sold() ? "1" : "0"));
        }
    }

    /**
     * 상품 한 종류의 조회 결과를 한 줄로 조합
     *
     * @param summary 표시할 상품 정보와 누적 입고·판매 수량 요약
     * @return 상품 정보와 수량을 조합한 한 줄 문자열. 다음 접미번호는 세 자리이며 소진되면 없음으로 표시
     */
    private String formatSummary(StockSummary summary) {
        var item = summary.item();
        String next = summary.nextSuffix().isPresent()
                ? DomainRules.formatSuffix(summary.nextSuffix().getAsInt()) : "없음";
        return "논리코드: " + item.code() + ", 상품명: " + item.name()
                + ", 크기: " + item.size() + ", 가격: " + item.price() + "원"
                + ", 누적 입고: " + summary.received() + "개, 누적 판매: " + summary.sold()
                + "개, 현재 재고: " + summary.current() + "개, 발급 가능 수: " + summary.issuable()
                + "개, 다음 접미번호: " + next;
    }

    
    // 시작 검사 전 출력
    public void startupChecking() {
        line("[INFO] 데이터 파일 검사를 진행합니다.");
    }

    // 시작 검사 후 출력
    public void startupPassed() {
        line("[INFO] 무결성 검사 완료. 프로그램을 시작합니다.");
    }

    /**
     * 시작 검사 시 오류 파일들과 이유 출력
     *
     * @param e 시작 파일 생성·접근·문법·의미 검사에서 발생한 예외. 파일명과 오류 이유를 사용
     */
    public void startupFailed(StartupDataException e) {
        line("[오류] " + String.join(" / ", e.fileNames()) + ": " + e.reason());
        line("데이터 파일 오류로 프로그램을 종료합니다.");
    }

    /**
     * 파일 저장 실패 안내 출력
     *
     * @param e 논리·물리 파일 저장 중 발생한 예외. 저장에 실패한 파일명을 사용
     */
    public void saveFailed(SaveFailureException e) {
        line("[오류] " + e.fileName() + " 저장에 실패했습니다. 작업을 완료하지 못했습니다.");
        line("프로그램을 종료합니다.");
    }

    // n 
    public void cancelled() {
        line("작업을 취소했습니다.");
    }

    // 종료 메뉴 선택 시 
    public void normalExit() {
        line("프로그램을 종료합니다.");
    }

    // EOF로 종료 시
    public void eofExit() {
        line("입력이 종료되어 프로그램을 종료합니다.");
    }

    /**
     * 한 줄을 출력하고 출력 버퍼를 비움
     *
     * @param text 개행을 붙여 출력할 안내 또는 결과 문자열
     */
    private void line(String text) {
        out.println(text);
        out.flush();
    }
}
