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

    public ConsoleView(PrintWriter out) {
        this.out = out;
    }

    
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

    // 필드에 따른 출력
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

    // 코드 입력 관련 출력
    public void promptCode(CodePurpose purpose) {
        String text = switch (purpose) {
            case INBOUND -> "입고할 논리적 상품코드";
            case SALE -> "판매할 기존 상품의 논리코드";
            case QUERY -> "조회할 기존 상품의 논리코드";
        };
        out.print("ConvenienceStore: " + text + " > ");
        out.flush();
    }

    // 에러별 출력
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

    
    public void noProducts(CodePurpose purpose) {
        switch (purpose) {
            case INBOUND -> line("등록된 상품이 없어 입고할 수 없습니다. 먼저 상품을 등록해야 합니다.");
            case SALE -> line("등록된 상품이 없어 판매할 수 없습니다.");
            case QUERY -> throw new IllegalArgumentException("조회는 상품이 없어도 방식부터 선택합니다.");
        }
    }

    /**
     * 상품명: {상품명}, 현재 재고: {current}개
     */
    public void selected(StockSummary summary) {
        line("상품명: " + summary.item().name() + ", 현재 재고: " + summary.current() + "개");
    }

    /**
     * 신규 상품이 등록되었습니다.
     * 논리코드: {코드}, 상품명: {상품명}, 크기: {크기}, 가격: {가격}원, 현재 재고: 0개
     */
    public void registered(LogicalItem item) {
        line("신규 상품이 등록되었습니다.");
        line("논리코드: " + item.code() + ", 상품명: " + item.name()
                + ", 크기: " + item.size() + ", 가격: " + item.price() + "원, 현재 재고: 0개");
    }

    /**
     * 입고가 완료되었습니다. (입고 수량: {quantity}개, 현재 재고: {currentAfter}개)
     */
    public void received(StockChangeResult result) {
        line("입고가 완료되었습니다. (입고 수량: " + result.quantity()
                + "개, 현재 재고: " + result.currentAfter() + "개)");
    }

    /**
     * 판매가 완료되었습니다. (판매 수량: {quantity}개, 현재 재고: {currentAfter}개)
     */
    public void sold(StockChangeResult result) {
        line("판매가 완료되었습니다. (판매 수량: " + result.quantity()
                + "개, 현재 재고: " + result.currentAfter() + "개)");
    }

    /**
     * 각 상품을 논리코드순으로 formatSummary 결과 한 줄씩 출력.
     * 상품 없으면 등록된 상품이 없습니다. 출력.
     * 창고 사용 용량: {used}, 남은 용량: {remaining} 출력.
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
     * 상품 formatSummary 출력
     * 접미번호 순으로 물리코드: {전체 물리코드}, 판매여부: {0 또는 1}
     * 낱개 없으면 입고된 물리적 상품이 없습니다. 출력
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
     * 논리코드: {코드}, 상품명: {상품명}, 크기: {크기}, 가격: {가격}원,
     * 누적 입고: {H}개, 누적 판매: {T}개, 현재 재고: {Q}개,
     * 발급 가능 수: {A}개, 다음 접미번호: {세 자리 번호 또는 없음}
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

    
    public void startupChecking() {
        line("[INFO] 데이터 파일 검사를 진행합니다.");
    }

    public void startupPassed() {
        line("[INFO] 무결성 검사 완료. 프로그램을 시작합니다.");
    }

    /**
     * [오류] {파일명}: {구체적인 이유} 출력.
     * 관계 오류도 해당 파일명을 표시하며, 필요한 경우 두 파일명을 함께 표시.
     * 특정 필드 오류는 필드명과 허용 규칙, 관계 오류는 위반한 규칙을 표시.
     */
    public void startupFailed(StartupDataException e) {
        line("[오류] " + String.join(" / ", e.fileNames()) + ": " + e.reason());
        line("데이터 파일 오류로 프로그램을 종료합니다.");
    }

    public void saveFailed(SaveFailureException e) {
        line("[오류] " + e.fileName() + " 저장에 실패했습니다. 작업을 완료하지 못했습니다.");
        line("프로그램을 종료합니다.");
    }

    public void cancelled() {
        line("작업을 취소했습니다.");
    }

    public void normalExit() {
        line("프로그램을 종료합니다.");
    }

    public void eofExit() {
        line("입력이 종료되어 프로그램을 종료합니다.");
    }

    private void line(String text) {
        out.println(text);
        out.flush();
    }
}
