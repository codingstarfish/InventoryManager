package inventory.dto;

import inventory.entity.LogicalItem;
import inventory.literal.Limits;
import java.util.OptionalInt;

//한 LogicalItem 요약
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
