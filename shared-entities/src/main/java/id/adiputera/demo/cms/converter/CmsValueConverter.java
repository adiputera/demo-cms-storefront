package id.adiputera.demo.cms.converter;

/**
 * Interface defining value conversion in the CMS metadata pipeline.
 *
 * @author Yusuf F. Adiputera
 */
public interface CmsValueConverter {

    /**
     * Converts the raw entity field value into a formatted or structured object.
     *
     * @param value The raw field value.
     * @return The converted value, which can be a String, Number, Boolean, or structured Object.
     */
    Object convert(Object value);
}
