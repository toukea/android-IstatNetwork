package istat.android.network.http;

public class HttpQueryError extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int code = 0;

    public int getCode() {
        return code;
    }

    public HttpQueryError(int code, String message) {
        super(message);
        this.code = code;
    }

    public HttpQueryError(Exception e) {
        super(e);
        if (e instanceof HttpQueryError) {
            this.code = ((HttpQueryError) e).getCode();
        }
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public String toString() {
        return code+" : "+getMessage();
    }
}