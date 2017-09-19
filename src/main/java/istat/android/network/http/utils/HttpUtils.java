package istat.android.network.http.utils;

/**
 * Created by istat on 18/09/17.
 */

public class HttpUtils {
    public static boolean isSuccessCode(int code) {
        return code > 0 && code >= 200 && code <= 299;
    }

    public static boolean isErrorCode(int code) {
        return !isSuccessCode(code);
    }

    public static boolean isClientErrorCode(int code) {
        return code > 0 && code >= 400 && code <= 499;
    }

    public static boolean isServerErrorCode(int code) {
        return code > 0 && code >= 500 && code <= 599;
    }
}
