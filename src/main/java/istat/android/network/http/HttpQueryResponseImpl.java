package istat.android.network.http;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

import istat.android.network.http.interfaces.DownloadHandler;
import istat.android.network.http.utils.HttpUtils;

class HttpQueryResponseImpl implements HttpQueryResponse {
    Object body;
    Exception error;
    int code = -1;
    String message;
    HttpURLConnection connexion;
    Header headers = new Header();

    public HttpURLConnection getConnection() {
        return connexion;
    }

    static HttpQueryResponseImpl newErrorInstance(Exception e) {
        try {
            return new HttpQueryResponseImpl(e);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


    HttpQueryResponseImpl(Exception e) {
        this.error = e;
    }

    HttpQueryResponseImpl(HttpQuery http, InputStream stream) throws HttpQuery.AbortionException {
        connexion = http.getCurrentConnection();
        if (connexion != null) {
            this.code = http.getCurrentResponseCode();
            this.message = http.getCurrentResponseMessage();
            try {
                this.body = null;
                if (stream != null) {
                    DownloadHandler downloader;
                    if (HttpUtils.isSuccessCode(connexion.getResponseCode())) {
                        downloader = http.getDownloadHandler(DownloadHandler.WHEN.SUCCESS);
                    } else {
                        downloader = http.getDownloadHandler(DownloadHandler.WHEN.ERROR);
                    }
                    this.body = downloader.onBuildResponseBody(connexion, stream);
                }
            } catch (Exception ex) {
                //TODO trouver mieux pour gérer les abortion.
                ex.printStackTrace();
                if (ex instanceof IOException && http.isAborted()) {
                    throw new HttpQuery.AbortionException(http, ex);
                }
                this.error = ex;
            }
            this.headers = new Header(connexion.getHeaderFields());
        }
    }

    public Header getHeaders() {
        return headers;
    }

    public List<String> getHeaders(String name) {
        return getHeaders().get(name);
    }

    public String getHeader(String name) {
        if (connexion != null) {
            return connexion.getHeaderField(name);
        }
        return "";
    }

    public long getHeaderAsLong(String name) {
        return getHeaderAsLong(name, 0);
    }

    public long getHeaderAsLong(String name, long defaultValue) {
        if (connexion != null) {
            return connexion.getHeaderFieldDate(name, defaultValue);
        }
        return defaultValue;
    }

    public int getHeaderAsInt(String name) {
        return getHeaderAsInt(name, 0);
    }

    public int getHeaderAsInt(String name, int defaultValue) {
        if (connexion != null) {
            return connexion.getHeaderFieldInt(name, defaultValue);
        }
        return defaultValue;
    }

    public boolean hasError() {
        return error != null || !HttpUtils.isSuccessCode(code);
    }

    public boolean isSuccess() {
        return !hasError();
    }

    public boolean isAccepted() {
        return code > 0;
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

    public <T> T getBodyAs(Class<T> cLass) {
        if (body == null) {
            return null;
        }
        if (cLass.isAssignableFrom(body.getClass())) {
            return (T) body;
        }
        return null;
    }

    public <T> T optBody() {
        if (body == null) {
            return null;
        }
        try {
            return (T) body;
        } catch (Exception e) {
            return null;
        }
    }

    public String getBodyAsString() {
        if (body == null)
            return null;
        return body.toString();
    }

    public JSONObject getBodyAsJson() throws JSONException {
        if (body == null)
            return null;
        return new JSONObject(body.toString());
    }

    public Exception getError() {
        return error;
    }
}
