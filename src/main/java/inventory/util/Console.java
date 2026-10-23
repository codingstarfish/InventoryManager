package inventory.util;

import java.util.function.Function;
import java.util.Map;
import java.util.Optional;
import inventory.dto.ItemDetail;
import inventory.dto.StockSummary;
import inventory.literal.ErrorCode;
import inventory.literal.CodePurpose;
import inventory.literal.Field;
import inventory.exception.SaveFailureException;
import inventory.exception.EndOfInputException;
import inventory.exception.BusinessRuleException;
import inventory.exception.UserInputException;
import inventory.validation.InputValidator;
import inventory.service.ProductService;
import inventory.service.StockService;
import inventory.service.QueryService;
import java.io.IOException;
import java.util.Objects;

/**
 * 메뉴와 재입력 흐름만 소유. 임시 입력은 각 메뉴의 지역 변수. 파일 쓰기·가변 재고 소유 없음.
 */
public class Console {
    /** 사용자 입력 채널 */
    private final Input input;
    /** 입력 전용 검증 */
    private final InputValidator validator;
    /** 공유 db의 등록·존재 확인 */
    private final ProductService productService;
    /** 공유 db의 입고·판매 */
    private final StockService stockService;
    /** 공유 db의 조회 */
    private final QueryService queryService;
    /** 화면 출력 담당 */
    private final ConsoleView view;

    /**
     * 의존 부품을 보관합니다. 생성 시 입력·출력·파일 작업 없음.
     * @param input 사용자 입력 채널
     * @param validator 입력 전용 검증
     * @param productService 공유 db의 등록·존재 확인
     * @param stockService 공유 db의 입고·판매
     * @param queryService 공유 db의 조회
     * @param view 화면 출력 담당
     */
    public Console(Input input, InputValidator validator, ProductService productService, StockService stockService, QueryService queryService, ConsoleView view) {
        this.input = Objects.requireNonNull(input, "input");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.productService = Objects.requireNonNull(productService, "productService");
        this.stockService = Objects.requireNonNull(stockService, "stockService");
        this.queryService = Objects.requireNonNull(queryService, "queryService");
        this.view = Objects.requireNonNull(view, "view");
    }

    /**
     * while 주 메뉴 루프. 메뉴 입력 오류는 안내 후 전체 주 메뉴 재표시(y/n 없음).
     * 1 등록, 2 입고, 3 판매, 4 조회, 5 normalExit 후 0 반환.
     * 최상위에서 EndOfInputException → eofExit → 0,
     * SaveFailureException → saveFailed → 1. 종료 안내는 한 번만 출력.
     * System.exit는 Main에서만. catch(Exception/RuntimeException)으로 개발 오류 숨기기 금지.
     * @return 정상 종료·EOF=0, 저장 실패=1
     * @throws IOException 입력 장치 장애를 Main에 전달
     */
    public int run() throws IOException {
        try {
            while (true) {
                view.mainMenu();
                int menu;
                try {
                    menu = validator.parseMainMenu(input.readLine());
                } catch (UserInputException e) {
                    view.error(e.code(), e.details());
                    continue;
                }
                switch (menu) {
                    case 1 -> registerMenu();
                    case 2 -> receiveMenu();
                    case 3 -> saleMenu();
                    case 4 -> queryMenu();
                    case 5 -> {
                        view.normalExit();
                        return 0;
                    }
                    default -> throw new IllegalStateException("검증되지 않은 메뉴 번호");
                }
            }
        } catch (EndOfInputException e) {
            view.eofExit();
            return 0;
        } catch (SaveFailureException e) {
            view.saveFailed(e);
            return 1;
        }
    }

    /**
     * canRegister 확인 → NAME → SIZE → PRICE → register → registered.
     * 번호 소진은 LIMIT_LOGICAL_CODE 안내 후 입력 없이 반환.
     * 필드 오류마다 error → askRetry. y는 현재 필드만 재입력, 이전 정상 값 유지.
     * n은 임시 값 폐기·cancelled 후 반환. 마지막 유효 가격 후 확인 없이 저장.
     * @throws IOException 입력 실패
     * @throws inventory.exception.EndOfInputException run으로 전달
     * @throws inventory.exception.SaveFailureException run으로 전달
     */
    private void registerMenu() throws IOException {
        if (!productService.canRegister()) {
            view.error(ErrorCode.LIMIT_LOGICAL_CODE, Map.of());
            return;
        }
        Optional<String> name = readValue(Field.NAME, validator::parseName);
        if (name.isEmpty()) {
            return;
        }
        Optional<Integer> size = readValue(Field.SIZE, validator::parseSize);
        if (size.isEmpty()) {
            return;
        }
        Optional<Integer> price = readValue(Field.PRICE, validator::parsePrice);
        if (price.isEmpty()) {
            return;
        }
        try {
            view.registered(productService.register(name.get(), size.get(), price.get()));
        } catch (BusinessRuleException e) {
            if (e.code() != ErrorCode.LIMIT_LOGICAL_CODE) {
                throw e;
            }
            view.error(e.code(), e.details());
        }
    }

    /**
     * hasProducts=false는 view.noProducts(INBOUND) 후 반환.
     * 코드 문법·범위 → selectInbound → selected → 수량 → receive → received.
     * LIMIT_PHYSICAL_CODE는 selected·수량·y/n 없이 안내 후 반환.
     * 코드 오류는 코드 재입력 확인, 수량/접미번호/용량 오류는 수량 재입력 확인.
     * 검증된 코드 유지, 오류·취소 시 파일 변경·번호 발급 없음.
     * @throws IOException 입력 실패
     * @throws inventory.exception.EndOfInputException run으로 전달
     * @throws inventory.exception.SaveFailureException run으로 전달
     */
    private void receiveMenu() throws IOException {
        if (!productService.hasProducts()) {
            view.noProducts(CodePurpose.INBOUND);
            return;
        }
        Optional<StockSummary> selected = readCode(CodePurpose.INBOUND, stockService::selectInbound);
        if (selected.isEmpty()) {
            return;
        }
        StockSummary summary = selected.get();
        view.selected(summary);
        readValue(Field.INBOUND_QUANTITY, raw -> stockService.receive(summary.item().code(),
                validator.parseQuantity(raw, Field.INBOUND_QUANTITY))).ifPresent(view::received);
    }

    /**
     * hasProducts=false는 view.noProducts(SALE) 후 반환.
     * 코드 문법·범위 → selectSale → selected → 수량 → sell → sold.
     * Q=0도 수량 입력을 수행. 재고 부족은 수량 재입력 확인.
     * 코드·수량 오류는 각각 현재 단계만 다시 입력; 이전 정상 값 유지.
     * @throws IOException 입력 실패
     * @throws inventory.exception.EndOfInputException run으로 전달
     * @throws inventory.exception.SaveFailureException run으로 전달
     */
    private void saleMenu() throws IOException {
        if (!productService.hasProducts()) {
            view.noProducts(CodePurpose.SALE);
            return;
        }
        Optional<StockSummary> selected = readCode(CodePurpose.SALE, stockService::selectSale);
        if (selected.isEmpty()) {
            return;
        }
        StockSummary summary = selected.get();
        view.selected(summary);
        readValue(Field.SALE_QUANTITY, raw -> stockService.sell(summary.item().code(),
                validator.parseQuantity(raw, Field.SALE_QUANTITY))).ifPresent(view::sold);
    }

    /**
     * 상품이 없어도 QUERY_MODE부터 입력. 1=findAll→allInventory,
     * 2=코드 검증→findOne→itemDetail. 오류는 현재 단계 재입력 확인.
     * 결과 출력 후 추가 입력 없이 반환. 파일 재읽기·저장 없음.
     * @throws IOException 입력 실패
     * @throws inventory.exception.EndOfInputException run으로 전달
     */
    private void queryMenu() throws IOException {
        Optional<Integer> mode = readValue(Field.QUERY_MODE, validator::parseQueryMode);
        if (mode.isEmpty()) {
            return;
        }
        if (mode.get() == 1) {
            view.allInventory(queryService.findAll());
        } else {
            Optional<ItemDetail> detail = readCode(CodePurpose.QUERY, queryService::findOne);
            detail.ifPresent(view::itemDetail);
        }
    }

    /**
     * RETRY 프롬프트 → parseRetry 반복. 잘못된 y/n은 RETRY_SYNTAX 안내 후 같은 확인만 반복.
     * 중첩 askRetry 호출·재귀 메뉴 호출 없음. 대상 필드·이전 정상 값 유지.
     * false를 받은 메뉴가 cancelled 출력·임시 값 폐기 후 반환합니다.
     * @return y=true, n=false
     * @throws IOException 입력 실패
     * @throws inventory.exception.EndOfInputException run으로 전달
     */
    private boolean askRetry() throws IOException {
        while (true) {
            view.prompt(Field.RETRY);
            try {
                return validator.parseRetry(input.readLine());
            } catch (UserInputException e) {
                view.error(e.code(), e.details());
            }
        }
    }

    /** 현재 필드만 반복하고 취소하면 빈 Optional로 해당 작업을 끝냅니다. */
    private <T> Optional<T> readValue(Field field, Function<String, T> parse) throws IOException {
        while (true) {
            view.prompt(field);
            try {
                return Optional.of(parse.apply(input.readLine()));
            } catch (UserInputException e) {
                view.error(e.code(), e.details());
            } catch (BusinessRuleException e) {
                boolean retryable = field == Field.INBOUND_QUANTITY
                        && (e.code() == ErrorCode.SUFFIX_SHORTAGE || e.code() == ErrorCode.CAPACITY_SHORTAGE)
                        || field == Field.SALE_QUANTITY && e.code() == ErrorCode.STOCK_SHORTAGE;
                if (!retryable) {
                    throw e;
                }
                view.error(e.code(), e.details());
            }
            if (!askRetry()) {
                view.cancelled();
                return Optional.empty();
            }
        }
    }

    /** 등록 존재 확인까지 코드 단계에서 수행하며 입고 코드 소진은 즉시 메뉴로 복귀합니다. */
    private <T> Optional<T> readCode(CodePurpose purpose, Function<String, T> select) throws IOException {
        while (true) {
            view.promptCode(purpose);
            try {
                String code = validator.parseLogicalCode(input.readLine());
                return Optional.of(select.apply(code));
            } catch (UserInputException e) {
                view.error(e.code(), e.details());
            } catch (BusinessRuleException e) {
                view.error(e.code(), e.details());
                if (purpose == CodePurpose.INBOUND && e.code() == ErrorCode.LIMIT_PHYSICAL_CODE) {
                    return Optional.empty();
                }
                if (e.code() != ErrorCode.CODE_NOT_FOUND) {
                    throw e;
                }
            }
            if (!askRetry()) {
                view.cancelled();
                return Optional.empty();
            }
        }
    }
}
