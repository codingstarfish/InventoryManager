package inventory.entity;

public enum ItemState {
    IN_STOCK(0), SOLD(1), DISCARDED(2);
    private final int value;

    ItemState(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
