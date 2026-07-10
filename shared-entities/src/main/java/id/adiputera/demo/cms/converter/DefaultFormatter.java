package id.adiputera.demo.cms.converter;

/**
 * Default implementation of CmsFormatter that acts as a pass-through.
 *
 * @author Yusuf F. Adiputera
 */
public class DefaultFormatter implements CmsFormatter {

    /**
     * Formats the value as-is.
     *
     * @see CmsFormatter#format(Object)
     * @param value The raw field value.
     * @return The raw field value without formatting.
     */
    @Override
    public Object format(Object value) {
        return value;
    }
}
