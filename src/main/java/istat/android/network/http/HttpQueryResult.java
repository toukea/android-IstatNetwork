package istat.android.network.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;

/**
 * Created by istat on 18/09/17.
 */
//TODO inner variable is really needed?
//TODO un result ne peut pas avoir une erreur, pas de sens.
public class HttpQueryResult implements HttpQueryResponse {
    int code;
    Object body;
    HttpQueryResponse response;

    HttpQueryResult(HttpQueryResponse resp) {
        this.code = resp.getCode();
        this.body = resp.getBody();
        this.response = resp;

    }

    @Override
    public String getMessage() {
        return response.getMessage();
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

    public String getHeader(String name) {
        return response.getHeader(name);
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
        return true;
    }

    @Override
    public boolean hasError() {
        return false;
    }

    public Throwable getError() {
        return null;
    }

    @Override
    public boolean isAccepted() {
        return true;
    }

    @Override
    public HttpURLConnection getConnection() {
        return response.getConnection();
    }

    public List<String> getHeaders(String name) {
        return response.getHeaders().get(name);
    }
}
