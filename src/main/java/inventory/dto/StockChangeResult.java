package inventory.dto;

import java.util.List;

/** 저장을 완료한 입고·판매 결과. 변경 코드는 접미번호순입니다. */
public record StockChangeResult(String logicalCode, int quantity, int currentAfter, List<String> affectedCodes) {
    public StockChangeResult {
        affectedCodes = List.copyOf(affectedCodes);
    }
}
