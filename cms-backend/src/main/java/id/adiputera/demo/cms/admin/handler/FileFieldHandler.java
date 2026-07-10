package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import org.springframework.stereotype.Component;

/**
 * Field handler implementation for FILE type.
 *
 * @author Yusuf F. Adiputera
 */
@Component
public class FileFieldHandler extends StringFieldHandler {

    /**
     * Gets the supported CmsFieldType.
     *
     * @see StringFieldHandler#getSupportedType()
     * @return The FILE type.
     */
    @Override
    public CmsFieldType getSupportedType() {
        return CmsFieldType.FILE;
    }
}
