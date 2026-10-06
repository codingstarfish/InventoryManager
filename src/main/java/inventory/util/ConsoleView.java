package inventory.util;

import inventory.literal.CodePurpose;
import inventory.literal.ErrorCode;
import inventory.literal.Field;
import inventory.dto.AllInventoryResult;
import inventory.dto.ItemDetail;
import inventory.dto.RegisterResult;
import inventory.dto.StockChangeResult;
import inventory.dto.StockSummary;
import inventory.exception.StartupDataException;
import inventory.exception.SaveFailureException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;

/**
 * 출력 전용. 업무 판단·입력·파일 수정·System.exit 없음. 프롬프트는 출력 직후 flush.
 */
public class ConsoleView {
    /** Main에서 주입한 UTF-8 표준 출력 */
    private final PrintWriter out;

    /**
     * 의존 부품을 보관합니다. 생성 시 입력·출력·파일 작업 없음.
     * @param out Main에서 주입한 UTF-8 표준 출력
     */
    public ConsoleView(PrintWriter out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    /**
     * 기획서 원판 6절의 구분선·제목·5개 메뉴와 ConvenienceStore> 출력.
     * 프롬프트 뒤 한 칸 공백을 두고 flush. 이후 입력은 Console 담당.
     */
    public void mainMenu() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * NAME: ConvenienceStore: 신규 상품명 >
     * SIZE: ConvenienceStore: 상품 크기(용량 단위) >
     * PRICE: ConvenienceStore: 판매 가격(원) >
     * INBOUND_QUANTITY: ConvenienceStore: 입고 수량 >
     * SALE_QUANTITY: ConvenienceStore: 판매 수량 >
     * QUERY_MODE: ConvenienceStore: 조회 방식(1: 전체, 2: 단일) >
     * RETRY: ConvenienceStore: 다시 입력하시겠습니까? (y/n) >
     * 프롬프트 뒤 한 칸 공백, 즉시 flush. MENU는 mainMenu, LOGICAL_CODE는 promptCode 사용.
     * @param field 위 부 프롬프트 중 하나
     * @throws IllegalArgumentException MENU 또는 LOGICAL_CODE를 직접 전달
     */
    public void prompt(Field field) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * INBOUND: ConvenienceStore: 입고할 논리적 상품코드 >
     * SALE: ConvenienceStore: 판매할 기존 상품의 논리코드 >
     * QUERY: ConvenienceStore: 조회할 기존 상품의 논리코드 >
     * 프롬프트 뒤 한 칸 공백, 즉시 flush.
     * @param purpose 입고·판매·조회 목적
     */
    public void promptCode(CodePurpose purpose) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * ErrorCode 사전의 입력·업무 메시지를 출력. 화면 이동은 Console 담당.
     * 필수 details: SUFFIX_SHORTAGE=issuable, CAPACITY_SHORTAGE=required/remaining,
     * STOCK_SHORTAGE=current. 일반 정수는 선행 0·쉼표·부호 없이 출력.
     * 필수 값 누락을 0으로 대체하지 않습니다.
     * @param code 입력·업무 오류 코드
     * @param details 필수 long 상세값 또는 Map.of()
     * @throws IllegalArgumentException 필수 key 누락 또는 잘못된 오류 코드
     */
    public void error(ErrorCode code, Map<String, Long> details) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * INBOUND: 등록된 상품이 없어 입고할 수 없습니다. 먼저 상품을 등록해야 합니다.
     * SALE: 등록된 상품이 없어 판매할 수 없습니다.
     * 기획서의 상품 없음 안내를 View에 모으기 위한 보조 API.
     * @param purpose INBOUND 또는 SALE
     * @throws IllegalArgumentException QUERY 전달; 조회는 상품이 없어도 방식 선택
     */
    public void noProducts(CodePurpose purpose) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 상품명: {상품명}, 현재 재고: {current}개
     * @param summary 선택 성공한 상품 수량
     */
    public void selected(StockSummary summary) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 신규 상품이 등록되었습니다.
     * 논리코드: {코드}, 상품명: {상품명}, 크기: {크기}, 가격: {가격}원, 현재 재고: 0개
     * @param result 논리 파일 저장·close·게시 후 받은 결과
     */
    public void registered(RegisterResult result) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 입고가 완료되었습니다. (입고 수량: {quantity}개, 현재 재고: {currentAfter}개)
     * affectedCodes를 추가 출력하지 않습니다.
     * @param result 물리 파일 저장·close·게시 후 입고 결과
     */
    public void received(StockChangeResult result) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 판매가 완료되었습니다. (판매 수량: {quantity}개, 현재 재고: {currentAfter}개)
     * affectedCodes를 추가 출력하지 않습니다.
     * @param result 물리 파일 저장·close·게시 후 판매 결과
     */
    public void sold(StockChangeResult result) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 각 상품을 논리코드순으로 formatSummary 결과 한 줄씩 출력.
     * 상품 없으면 등록된 상품이 없습니다. 출력.
     * 항상 마지막에 창고 사용 용량: {used}, 남은 용량: {remaining} 출력.
     * @param result 전체 상품 수량 및 창고 정보
     */
    public void allInventory(AllInventoryResult result) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 상품 formatSummary 한 줄 뒤 판매 완료 포함 낱개를 접미번호순 출력:
     * 물리코드: {전체 물리코드}, 판매여부: {0 또는 1}
     * 낱개 없으면 입고된 물리적 상품이 없습니다. 출력. 창고 합계 추가 없음.
     * @param result 단일 상품 수량과 전체 낱개 목록
     */
    public void itemDetail(ItemDetail result) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 공통 상품 정보 한 줄을 만듭니다:
     * 논리코드: {코드}, 상품명: {상품명}, 크기: {크기}, 가격: {가격}원,
     * 누적 입고: {H}개, 누적 판매: {T}개, 현재 재고: {Q}개,
     * 발급 가능 수: {A}개, 다음 접미번호: {세 자리 번호 또는 없음}
     * 실제 반환 문자열에는 위 설명상의 줄바꿈 없음. 고정 자리 포맷은 Locale.ROOT.
     * @param summary 상품별 수량 정보
     * @return 기획서 원판 6.4.3절의 한 줄 상품 정보
     */
    private String formatSummary(StockSummary summary) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * [INFO] 데이터 파일 검사를 진행합니다. 출력. Main이 load 전에 호출.
     */
    public void startupChecking() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * [INFO] 무결성 검사 완료. 프로그램을 시작합니다. 출력. Main이 load 성공 후 호출.
     */
    public void startupPassed() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 기획서 원판 5.6절: [오류] {파일명}: {구체적인 이유} 출력.
     * 관계 오류도 해당 파일명을 표시하며, 필요한 경우 두 파일명을 함께 표시.
     * 특정 필드 오류는 필드명과 허용 규칙, 관계 오류는 위반한 규칙을 표시.
     * 이후 데이터 파일 오류로 프로그램을 종료합니다. 출력. Main이 종료 1 처리.
     * @param e 파일명·코드·구체 이유·선택 행 번호를 가진 시작 예외
     */
    public void startupFailed(StartupDataException e) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * [오류] {파일명} 저장에 실패했습니다. 작업을 완료하지 못했습니다.
     * 프로그램을 종료합니다.
     * Console.run에서 한 번만 호출; 완료 메시지·메뉴 복귀 없음.
     * @param e 저장 대상 파일명과 원인 예외
     */
    public void saveFailed(SaveFailureException e) {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 작업을 취소했습니다. 출력. 파일 접근 없음.
     */
    public void cancelled() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 프로그램을 종료합니다. 출력. 종료 확인·파일 저장 없음.
     */
    public void normalExit() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }

    /**
     * 입력이 종료되어 프로그램을 종료합니다. 출력. 정상 종료 안내 중복 없음.
     */
    public void eofExit() {
        // TODO: 위 계약에 맞춰 구현합니다.
        throw new UnsupportedOperationException("미구현");
    }
}
