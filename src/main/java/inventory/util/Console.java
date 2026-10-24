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

/**
 * 메뉴와 재입력 흐름만 소유
 */
public class Console {
    private final Input input;
    private final InputValidator validator;
    private final ProductService productService;
    private final StockService stockService;
    private final QueryService queryService;
    private final ConsoleView view;

    
    /**
     * 입력, 검증, 업무 처리와 화면 출력에 사용할 구성 요소를 보관
     * @param input 사용자의 원문 입력과 EOF를 전달하는 입력 도구
     * @param validator 각 입력의 문법과 값 범위를 검사하는 검증기
     * @param productService 상품 등록과 등록 가능 여부를 처리하는 서비스
     * @param stockService 입고,판매 대상 선택과 재고 변경을 처리하는 서비스
     * @param queryService 전체 또는 단일 상품의 재고를 조회하는 서비스
     * @param view 프롬프트, 오류 안내와 처리 결과를 출력하는 화면 도구
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
     * 주 메뉴를 반복 표시하고 선택한 작업을 실행합니다. EOF와 저장 실패는 안내 후 반복을 끝내며 프로세스 종료는 호출한 Main에서 수행
     * @return 정상 종료나 EOF이면 0이며, 파일 저장 실패이면 1인 종료 코드
     * @throws IOException 입력 또는 파일 입출력이 실패하면 던짐
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

    /**
     * 등록 가능 여부를 확인하고 상품명, 크기, 가격 순서로 입력받아 저장, 재입력 시 앞 단계의 정상 값은 유지하며 취소 시 임시 값을 폐기
     * @throws IOException 입력 또는 파일 입출력이 실패하면 던짐
     * @throws inventory.exception.EndOfInputException 입력 중 EOF를 확인하면 상위 실행 흐름에 던짐
     * @throws inventory.exception.SaveFailureException 작업 결과의 파일 저장이 실패하면 상위 실행 흐름에 던짐
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
        view.registered(productService.register(name.get(), size.get(), price.get()));
    }

    /**
     * 입고 대상과 수량을 입력받아 입고합니다. 접미번호가 소진되었으면 수량 입력 없이 돌아가며 수량 오류 시 선택한 상품을 유지하여 재입력받음
     * @throws IOException 입력 또는 파일 입출력이 실패하면 전달
     * @throws inventory.exception.EndOfInputException 입력 중 EOF를 확인하면 상위 실행 흐름에 전달
     * @throws inventory.exception.SaveFailureException 작업 결과의 파일 저장이 실패하면 상위 실행 흐름에 전달
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
        readValue(Field.INBOUND_QUANTITY, raw -> stockService.receive(summary,
                validator.parseQuantity(raw, Field.INBOUND_QUANTITY))).ifPresent(view::received);
    }

    /**
     * 판매 대상과 수량을 입력받아 판매함. 현재 재고가 0개여도 수량을 입력받으며 수량 오류 시 선택한 상품을 유지
     * @throws IOException 입력 또는 파일 입출력이 실패하면 던짐
     * @throws inventory.exception.EndOfInputException 입력 중 EOF를 확인하면 상위 실행 흐름에 던짐
     * @throws inventory.exception.SaveFailureException 작업 결과의 파일 저장이 실패하면 상위 실행 흐름에 던짐
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
        readValue(Field.SALE_QUANTITY, raw -> stockService.sell(summary,
                validator.parseQuantity(raw, Field.SALE_QUANTITY))).ifPresent(view::sold);
    }

    /**
     * 상품 유무와 관계없이 조회 방식을 입력받아 전체 또는 단일 상품 결과를 출력, 결과 출력 후 추가 입력 없이 돌아감
     * @throws IOException 입력 또는 파일 입출력이 실패하면 던짐
     * @throws inventory.exception.EndOfInputException 입력 중 EOF를 확인하면 상위 실행 흐름에 던짐
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
     * 올바른 소문자 y 또는 n을 입력할 때까지 같은 확인 프롬프트를 반복
     * @return 현재 단계의 재입력을 선택하면 true, 작업 전체의 취소를 선택하면 false
     * @throws IOException 입력 또는 파일 입출력이 실패하면 던짐
     * @throws inventory.exception.EndOfInputException 입력 중 EOF를 확인하면 상위 실행 흐름에 던짐
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
     * 현재 필드의 값을 입력받고 입력 오류 또는 허용된 업무 오류 시 재입력 여부를 확인, n이면 취소 안내 후 빈 결과를 반환
     * @param <T> 입력 문자열을 검증·변환하거나 업무를 처리하여 얻는 결과 값의 자료형
     * @param field 표시할 프롬프트와 재입력 가능한 업무 오류의 종류를 구분하는 입력 항목
     * @param parse 원문 입력을 검증·변환하는 함수, 수량 입력에서는 입고 또는 판매 처리까지 수행할 수 있음
     * @return 입력 및 처리에 성공한 값을 담은 Optional, 사용자가 작업을 취소하면 Optional.empty()
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
     * 논리코드의 문법,범위,등록 여부를 확인하고 대상을 선택, 입고 접미번호 소진은 재입력 확인 없이 빈 결과로 반환
     * @param <T> 코드에 해당하는 상품을 선택하거나 조회하여 얻는 결과 값의 자료형
     * @param purpose 입고, 판매, 조회 중 코드 입력의 목적입니다. 프롬프트와 소진 시 흐름을 결정
     * @param select 검증된 논리코드로 상품을 선택하거나 조회하는 함수입니다. 등록 여부도 확인
     * @return 선택에 성공한 결과를 담은 Optional, 작업 취소 또는 입고 접미번호 소진이면 Optional.empty()
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
