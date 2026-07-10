package id.adiputera.demo.cms.converter;

/**
 * Interface defining value formatting in the CMS metadata outbound pipeline.
 * Formats data when sending it to the frontend.
 *
 * @author Yusuf F. Adiputera
 */
public interface CmsFormatter {

    /**
     * Formats the raw entity field value for serialization.
     *
     * @param value The raw field value.
     * @return The formatted representation.
     */
    Object format(Object value);
}
