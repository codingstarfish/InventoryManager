package inventory.dto;
public record SaleLine(inventory.entity.PhysicalKey key, int regularPrice, long salePrice, boolean discounted) {}
