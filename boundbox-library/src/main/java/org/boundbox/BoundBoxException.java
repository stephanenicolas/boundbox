package org.boundbox;

public class BoundBoxException extends RuntimeException {
    private static final long serialVersionUID = 4325402242193943289L;

    public BoundBoxException() {
        super();
    }

    public BoundBoxException(String message, Throwable cause) {
        super(message, cause);
    }

    public BoundBoxException(String message) {
        super(message);
    }

    public BoundBoxException(Throwable cause) {
        super(cause);
    }
}
