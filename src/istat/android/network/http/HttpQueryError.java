package istat.android.network.http;

public class HttpQueryError extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int code = 0;
    Object body;

    public int getCode() {
        return code;
    }

    public HttpQueryError(int code, String message) {
        super(message);
        this.code = code;
    }

    HttpQueryError(int code, String message, Object body) {
        super(message);
        this.code = code;
        this.body = body;
    }

    public HttpQueryError(Exception e) {
        super(e);
        if (e instanceof HttpQueryError) {
            this.code = ((HttpQueryError) e).getCode();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBody() {
        if (body == null) {
            return null;
        }
        try {
            return (T) body;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getBodyAsString() {
        if (body == null)
            return null;
        return body.toString();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public String toString() {
        return code + " : " + getMessage();
    }
}