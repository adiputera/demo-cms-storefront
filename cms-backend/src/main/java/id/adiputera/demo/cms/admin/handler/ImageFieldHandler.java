package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import org.springframework.stereotype.Component;

/**
 * Field handler implementation for IMAGE type.
 *
 * @author Yusuf F. Adiputera
 */
@Component
public class ImageFieldHandler extends StringFieldHandler {

    /**
     * Gets the supported CmsFieldType.
     *
     * @see StringFieldHandler#getSupportedType()
     * @return The IMAGE type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.IMAGE;
    }
}
