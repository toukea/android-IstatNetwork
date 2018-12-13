package istat.android.network.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;

//TODO inner variable is really needed?
public class HttpQueryError extends RuntimeException implements HttpQueryResponse {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int code;
    Object body;
    HttpQueryResponse response;

    HttpQueryError(HttpQueryResponse resp) {
        super(resp.getMessage());
        this.code = resp.getCode();
        this.body = resp.getBody();
        this.response = resp;
    }

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

    public String getHeader(String name) {
        return response.getHeader(name);
    }

    public List<String> getHeaders(String name) {
        return response.getHeaders().get(name);
    }

    public long getHeaderAsLong(String name) {
        return getHeaderAsLong(name, 0);
    }

    public long getHeaderAsLong(String name, long defaultValue) {
        return response.getHeaderAsLong(name);
    }

    public int getHeaderAsInt(String name) {
        return getHeaderAsInt(name, 0);
    }

    public int getHeaderAsInt(String name, int defaultValue) {
        return response.getHeaderAsInt(name, defaultValue);
    }

    public Header getHeaders() {
        return response.getHeaders();
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean hasError() {
        return true;
    }

    public Throwable getError() {
        if (body != null && body instanceof Throwable) {
            return (Throwable) body;
        }
        if (getCause() != null) {
            return getCause();
        } else {
            return this;
        }
    }

    public boolean isAccepted() {
        return code > 0;
    }

    @Override
    public HttpURLConnection getConnection() {
        return response.getConnection();
    }
}