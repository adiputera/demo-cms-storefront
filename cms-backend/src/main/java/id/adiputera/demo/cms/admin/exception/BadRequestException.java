package id.adiputera.demo.cms.admin.exception;

/**
 * Bad Request Exception class.
 *
 * @author Yusuf F. Adiputera
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
