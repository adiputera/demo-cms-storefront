package id.adiputera.demo.cms.converter;

/**
 * Default implementation of CmsValueConverter that acts as a pass-through.
 *
 * @author Yusuf F. Adiputera
 */
public class DefaultValueConverter implements CmsValueConverter {

    /**
     * Converts the raw field value. This default implementation simply returns the value as-is.
     *
     * @see CmsValueConverter#convert(Object)
     * @param value The raw field value.
     * @return The raw field value without modifications.
     */
    @Override
    public Object convert(Object value) {
        return value;
    }
}
