package inventory.dto;

import inventory.entity.LogicalItem;
import inventory.literal.Limits;
import java.util.OptionalInt;

// 조회 시점의 상품별 수량. received에는 판매된 낱개도 포함
public record StockSummary(LogicalItem item, int received, int sold) {
    // 현재 재고 = 누적 입고 - 누적 판매 (0~999)
    public int current() {
        return received - sold;
    }

    // 발급 가능 수 = 999 - 누적 입고
    public int issuable() {
        return Limits.MAX_SUFFIX - received;
    }

    // 다음 접미번호. 누적 입고가 999개면 OptionalInt.empty()
    public OptionalInt nextSuffix() {
        return received == Limits.MAX_SUFFIX ? OptionalInt.empty() : OptionalInt.of(received + 1);
    }
}
