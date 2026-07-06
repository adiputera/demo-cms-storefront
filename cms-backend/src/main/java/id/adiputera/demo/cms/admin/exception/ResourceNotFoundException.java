package id.adiputera.demo.cms.admin.exception;

/**
 * Resource Not Found Exception class.
 *
 * @author Yusuf F. Adiputera
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceType, identifier));
    }
    
    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s not found with ID: %d", resourceType, id));
    }
}
