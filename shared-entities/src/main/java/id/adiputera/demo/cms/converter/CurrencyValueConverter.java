package id.adiputera.demo.cms.converter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * Value converter for currency fields.
 * Formats a numeric value (e.g. BigDecimal) into a structured map with raw and formatted values.
 *
 * @author Yusuf F. Adiputera
 */
public class CurrencyValueConverter implements CmsValueConverter {

    /**
     * Converts a numeric value into a map containing the raw value and its USD formatted representation.
     *
     * @see CmsValueConverter#convert(Object)
     * @param value The raw numeric field value.
     * @return A map with "value" and "formatted" keys, or null if input is null.
     */
    @Override
    public Object convert(Object value) {
        if (value == null) {
            return null;
        }
        BigDecimal numericValue;
        if (value instanceof BigDecimal) {
            numericValue = (BigDecimal) value;
        } else if (value instanceof Number) {
            numericValue = BigDecimal.valueOf(((Number) value).doubleValue());
        } else {
            try {
                numericValue = new BigDecimal(value.toString());
            } catch (Exception e) {
                return value;
            }
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        String formatted = formatter.format(numericValue);
        return Map.of(
            "value", numericValue,
            "formatted", formatted
        );
    }
}
