package inventory.dto;
public record DiscardedLine(inventory.entity.PhysicalKey key, java.time.LocalDate expiryDate) {}
