package istat.android.network.http;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpQueryError extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int code = 0;
    Object body;

    HttpQueryError(HttpAsyncQuery.HttpQueryResponse resp) {
        this(resp.getCode(), resp.getMessage(), resp.getBody());
    }

    HttpQueryError(int code, String message, Object body) {
        super(message);
        this.code = code;
        this.body = body;
    }
//    HttpQueryError(Exception e) {
//        super(e);
//        if (e instanceof HttpQueryError) {
//            HttpQueryError error = ((HttpQueryError) e);
//            this.code = error.getCode();
//            this.body = error.getBody();
//        }
//    }

    public int getCode() {
        return code;
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

    public JSONObject getBodyAsJson() throws JSONException {
        if (body == null)
            return null;
        return new JSONObject(body.toString());
    }

    public <T> T getBodyAs(Class<T> cLass) {
        if (body == null) {
            return null;
        }
        if (cLass.isAssignableFrom(body.getClass())) {
            return (T) body;
        }
        return null;
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
        return code + " : " + getMessage() + ", " + this.body;
    }
}