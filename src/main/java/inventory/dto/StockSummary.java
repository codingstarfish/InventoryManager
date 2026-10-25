package inventory.dto;

import inventory.entity.LogicalItem;
import inventory.literal.Limits;
import java.util.OptionalInt;

/**
 * 조회 시점의 상품별 수량. received에는 판매된 낱개도 포함
 * @param item 수량을 조회한 등록 상품 정보
 * @param received 판매된 낱개를 포함한 누적 입고 수(0~999개)
 * @param sold 누적 판매 수(개). 0 이상이며 누적 입고 수 이하
 */
public record StockSummary(LogicalItem item, int received, int sold) {
    /**
     * 현재 재고 = 누적 입고 - 누적 판매 (0~999)
     * @return 현재 미판매 재고 수(개)
     */
    public int current() {
        return received - sold;
    }

    /**
     * 발급 가능 수 = 999 - 누적 입고
     * @return 이 상품에서 추가로 발급할 수 있는 낱개 코드 수(개)
     */
    public int issuable() {
        return Limits.MAX_SUFFIX - received;
    }

    /**
     * 다음 접미번호. 누적 입고가 999개면 OptionalInt.empty()
     * @return 다음에 발급할 접미번호(1~999), 모두 발급했으면 빈 OptionalInt
     */
    public OptionalInt nextSuffix() {
        return received == Limits.MAX_SUFFIX ? OptionalInt.empty() : OptionalInt.of(received + 1);
    }
}
