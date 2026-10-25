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


public class Console {
    private final Input input;
    private final InputValidator validator;
    private final ProductService productService;
    private final StockService stockService;
    private final QueryService queryService;
    private final ConsoleView view;

    
    /**
     * 입력/검증/서비스/출력 객체 보관
     *
     * @param input 사용자 입력을 한 줄씩 읽는 입력 채널
     * @param validator 입력 문자열의 형식과 값 범위를 검사하는 검증기
     * @param productService 신규 상품 등록과 등록 가능 여부를 처리하는 서비스
     * @param stockService 재고 입고와 상품 판매를 처리하는 서비스
     * @param queryService 전체 재고와 단일 상품을 조회하는 서비스
     * @param view 프롬프트, 오류 안내, 처리 결과를 출력하는 객체
     */
    public Console(Input input, InputValidator validator, ProductService productService, StockService stockService, QueryService queryService, ConsoleView view) {
        this.input = input;
        this.validator = validator;
        this.productService = productService;
        this.stockService = stockService;
        this.queryService = queryService;
        this.view = view;
    }

    /**
     * 주 메뉴 반복. 실제 프로세스 종료는 Main에서 처리
     *
     * @return 정상 종료 또는 EOF이면 0, 파일 저장 실패이면 1인 종료 코드
     * @throws IOException 사용자 입력을 읽는 중 입출력 오류가 발생한 경우
     */
    public int run() throws IOException {
        try {
            while (true) {
                view.mainMenu();
                int menu;
                try {
                    menu = validator.parseMainMenu(input.readLine());
                } catch (UserInputException e) {
                    view.error(e.code(), Map.of());
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

    // 상품명 → 크기 → 가격 순서로 입력받아 등록
    // 재입력은 현재 항목만 반복하고, n이면 취소
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
        view.registered(productService.register(name.get(), size.get(), price.get()));
    }

    // 상품 선택 후 수량 입력. 접미번호가 소진된 상품은 수량 입력 없이 복귀
    // 수량을 다시 입력할 때 선택한 상품은 유지
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
        readValue(Field.INBOUND_QUANTITY, raw -> stockService.receive(summary,
                validator.parseQuantity(raw, Field.INBOUND_QUANTITY))).ifPresent(view::received);
    }

    // 상품 선택 후 수량 입력. 재고 0개여도 수량은 입력받음
    // 수량을 다시 입력할 때 선택한 상품은 유지
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
        readValue(Field.SALE_QUANTITY, raw -> stockService.sell(summary,
                validator.parseQuantity(raw, Field.SALE_QUANTITY))).ifPresent(view::sold);
    }

    // 상품이 없어도 조회 방식부터 선택. 결과 출력 후 주 메뉴로 복귀
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
     * y면 재입력, n이면 취소. 다른 입력이면 y/n부터 다시 받음
     *
     * @return 재입력을 선택한 y이면 true, 작업 취소를 선택한 n이면 false
     * @throws IOException 사용자 입력을 읽는 중 입출력 오류가 발생한 경우
     */
    private boolean askRetry() throws IOException {
        while (true) {
            view.prompt(Field.RETRY);
            try {
                return validator.parseRetry(input.readLine());
            } catch (UserInputException e) {
                view.error(e.code(), Map.of());
            }
        }
    }

    /**
     * 입력 오류나 해당 단계의 업무 오류면 재입력 확인. 예상하지 않은 업무 오류는 다시 던짐
     *
     * @param <T> 입력을 검증·변환하거나 업무를 처리해 얻는 결과의 자료형
     * @param field 표시할 프롬프트와 재입력 가능한 업무 오류를 구분하는 입력 항목
     * @param parse 원문 입력을 검증·변환하는 함수. 수량 입력에서는 입고·판매 처리도 수행
     * @return 처리에 성공한 값을 담은 Optional, 사용자가 취소하면 Optional.empty()
     * @throws IOException 사용자 입력을 읽는 중 입출력 오류가 발생한 경우
     */
    private <T> Optional<T> readValue(Field field, Function<String, T> parse) throws IOException {
        while (true) {
            view.prompt(field);
            try {
                return Optional.of(parse.apply(input.readLine()));
            } catch (UserInputException e) {
                view.error(e.code(), Map.of());
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

    /**
     * 코드 형식/범위/등록 여부 확인 후 상품 선택. 미등록 코드는 재입력 확인
     *
     * @param <T> 논리코드로 상품을 선택하거나 조회해 얻는 결과의 자료형
     * @param purpose 코드 입력 목적. 입고·판매·조회 프롬프트와 입고 접미번호 소진 시 흐름을 결정
     * @param select 검증된 논리코드로 등록 여부를 확인하고 상품을 선택·조회하는 함수
     * @return 선택한 결과를 담은 Optional, 취소하거나 입고 접미번호가 소진되면 Optional.empty()
     * @throws IOException 사용자 입력을 읽는 중 입출력 오류가 발생한 경우
     */
    private <T> Optional<T> readCode(CodePurpose purpose, Function<String, T> select) throws IOException {
        while (true) {
            view.promptCode(purpose);
            try {
                String code = validator.parseLogicalCode(input.readLine());
                return Optional.of(select.apply(code));
            } catch (UserInputException e) {
                view.error(e.code(), Map.of());
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
