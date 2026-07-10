package id.adiputera.demo.cms.admin.handler;

import id.adiputera.demo.cms.annotation.CmsFieldType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry mapping CmsFieldType to its specific CmsFieldHandler implementation.
 *
 * @author Yusuf F. Adiputera
 */
@Service
public class CmsFieldHandlerRegistry {

    private final Map<CmsFieldType, CmsFieldHandler> registry = new ConcurrentHashMap<>();

    /**
     * Constructs the registry by scanning all available CmsFieldHandler beans.
     *
     * @param handlers The list of spring-managed field handler beans.
     */
    public CmsFieldHandlerRegistry(List<CmsFieldHandler> handlers) {
        for (CmsFieldHandler handler : handlers) {
            registry.put(handler.getSupportedType(), handler);
        }
    }

    /**
     * Resolves a field handler for a given CmsFieldType.
     *
     * @param type The field type.
     * @return The matching handler implementation.
     * @throws IllegalArgumentException If no handler is registered for the type.
     */
    public CmsFieldHandler getHandler(CmsFieldType type) {
        CmsFieldHandler handler = registry.get(type);
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for type: " + type);
        }
        return handler;
    }
}
