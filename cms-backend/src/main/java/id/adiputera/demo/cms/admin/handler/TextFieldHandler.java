package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import org.springframework.stereotype.Component;

/**
 * Field handler implementation for TEXT type.
 *
 * @author Yusuf F. Adiputera
 */
@Component
public class TextFieldHandler extends StringFieldHandler {

    /**
     * Gets the supported CmsFieldType.
     *
     * @see StringFieldHandler#getSupportedType()
     * @return The TEXT type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.TEXT;
    }
}
