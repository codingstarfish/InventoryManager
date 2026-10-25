package inventory.dto;

import inventory.entity.LogicalItem;
import inventory.literal.Limits;
import java.util.OptionalInt;

/**
 * 한 상품의 수량 요약을 보관하고 현재 재고와 발급 가능 수를 계산함
 * @param item 요약 대상인 등록 상품의 정보
 * @param received 판매된 낱개까지 포함한 누적 입고 수량
 * @param sold 판매 완료된 낱개의 누적 수량입니다
 */
public record StockSummary(LogicalItem item, int received, int sold) {
    /**
     * 누적 입고에서 누적 판매를 빼서 현재 재고를 계산
     * @return 판매되지 않은 낱개의 개수입니다, 0~999 사이
     */
    public int current() {
        return received - sold;
    }

    /**
     * 상품별 접미번호 한도에서 누적 입고 수량을 빼서 남은 발급 가능 수를 계산
     * @return 앞으로 추가 입고할 수 있는 낱개의 개수, 0~999 사이
     */
    public int issuable() {
        return Limits.MAX_SUFFIX - received;
    }

    /**
     * 누적 입고 수량으로 다음에 발급할 접미번호를 계산
     * @return 다음 번호 1~999를 담은 OptionalInt, 누적 입고가 999개이면 OptionalInt.empty()
     */
    public OptionalInt nextSuffix() {
        return received == Limits.MAX_SUFFIX ? OptionalInt.empty() : OptionalInt.of(received + 1);
    }
}
