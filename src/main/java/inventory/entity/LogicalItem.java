package inventory.entity;

public record LogicalItem(
        String code,
        String name,
        int size,
        int price
) {

}