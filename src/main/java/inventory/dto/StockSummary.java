package inventory.dto;

import inventory.entity.LogicalItem;
import inventory.literal.Limits;
import java.util.OptionalInt;

/** 상품별 누적 입고·판매 수량. 나머지 수량은 이 두 값으로 계산합니다. */
public record StockSummary(LogicalItem item, int received, int sold) {
    public int current() {
        return received - sold;
    }

    public int issuable() {
        return Limits.MAX_SUFFIX - received;
    }

    public OptionalInt nextSuffix() {
        return received == Limits.MAX_SUFFIX ? OptionalInt.empty() : OptionalInt.of(received + 1);
    }
}
