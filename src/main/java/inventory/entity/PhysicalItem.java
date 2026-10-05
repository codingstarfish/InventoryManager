package inventory.entity;

public record PhysicalItem(
        String logicalCode,
        int suffix,
        boolean sold
) {
}
