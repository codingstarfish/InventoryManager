package inventory.database;

import inventory.entity.ItemState;

public record RawPhysicalRow(String logicalCode, int suffix, ItemState state, String producedOn, String soldOn,
                             boolean refunded) {
}
