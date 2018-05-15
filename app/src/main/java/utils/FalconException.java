package utils;
/**
 * Created by mingcong on 2018/5/15.
 */
public class FalconException extends Exception {
    private Throwable cause;

    public FalconException(String message) {
        super(message);
    }

    public FalconException(Throwable t) {
        super(t.getMessage());
        this.cause = t;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
