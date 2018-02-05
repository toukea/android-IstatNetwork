package istat.android.network.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import istat.android.network.http.utils.HttpUtils;
import istat.android.network.utils.ToolKits;

/**
 * Created by istat on 18/09/17.
 */

public interface HttpQueryResponse {

    int getCode();

    @SuppressWarnings("unchecked")
    <T> T getBody();


    JSONObject getBodyAsJson() throws JSONException;

    <T> T getBodyAs(Class<T> cLass);

    String getBodyAsString();

    String getHeader(String name);

    long getHeaderAsLong(String name);

    long getHeaderAsLong(String name, long defaultValue);

    int getHeaderAsInt(String name);

    int getHeaderAsInt(String name, int defaultValue);

    Map<String, List<String>> getHeaders();

    boolean isSuccess();

    boolean hasError();

    Throwable getError();

    boolean isAccepted();

    HttpURLConnection getConnection();

    List<String> getHeaders(String name);

    static boolean containHeader(HttpQueryResponse response, String name) {
        return false;
    }

    static boolean isSuccessCode(int code) {
        return code > 0 && code >= 200 && code <= 299;
    }

    static boolean isErrorCode(int code) {
        return !isSuccessCode(code);
    }

    static boolean isClientErrorCode(int code) {
        return code > 0 && code >= 400 && code <= 499;
    }

    static boolean isServerErrorCode(int code) {
        return code > 0 && code >= 500 && code <= 599;
    }
}
