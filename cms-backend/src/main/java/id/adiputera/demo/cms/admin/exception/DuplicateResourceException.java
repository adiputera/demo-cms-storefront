package id.adiputera.demo.cms.admin.exception;

/**
 * Duplicate Resource Exception class.
 *
 * @author Yusuf F. Adiputera
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String resourceType, String field, String value) {
        super(String.format("%s with %s '%s' already exists", resourceType, field, value));
    }
}
