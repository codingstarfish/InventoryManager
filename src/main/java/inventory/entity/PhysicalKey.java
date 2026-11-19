package inventory.entity;
import inventory.validation.DomainRules;
public record PhysicalKey(String logicalCode, int suffix) {
 @Override public String toString() { return logicalCode+"-"+DomainRules.formatSuffix(suffix); }
}
