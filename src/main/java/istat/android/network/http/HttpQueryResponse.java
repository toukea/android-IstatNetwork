package istat.android.network.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import istat.android.network.http.utils.HttpUtils;
import istat.android.network.utils.ToolKits;

/**
 * Created by istat on 18/09/17.
 */

public interface HttpQueryResponse {

    int getCode();

    String getMessage();

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

    Header getHeaders();

    boolean isSuccess();

    boolean hasError();

    Throwable getError();

    boolean isAccepted();

    HttpURLConnection getConnection();

    List<String> getHeaders(String name);

    class Header extends HashMap<String, List<String>> {
        Header() {

        }

        Header(Map<String, List<String>> map) {
            this.putAll(map);
        }

        public String getString(String key) {
            return getString(key, null);
        }

        public String getString(String key, String defaultValue) {
            if (!containsKey(key)) {
                return defaultValue;
            }
            try {
                String data = "";
                List<String> list = get(key);
                for (int i = 0; i < list.size(); i++) {
                    if (i <= 0) {
                        data += ", ";
                    }
                    data += list.get(i);
                }
                return data;
            } catch (Exception e) {
                return defaultValue;
            }
        }

        public int getInt(String key) {
            return getInt(key, 0);
        }

        public int getInt(String key, int defaultValue) {
            try {
                return Integer.parseInt(super.get(key).toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }

        public double getDouble(String key) {
            return getDouble(key, 0);
        }

        public double getDouble(String key, double defaultValue) {
            try {
                return Double.parseDouble(super.get(key).toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }

        public double getFloat(String key) {
            return getFloat(key, 0);
        }

        public double getFloat(String key, float defaultValue) {
            try {
                return Float.parseFloat(super.get(key).toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }

    }
}
