package istat.android.network.http.interfaces;

import java.io.InputStream;
import java.net.HttpURLConnection;

import istat.android.network.http.HttpAsyncQuery;

/**
 * Created by istat on 05/11/16.
 */

public interface DownloadHandler {
    enum WHEN {
        SUCCESS, ERROR
    }

    Object onBuildResponseBody(HttpURLConnection connexion,
                               InputStream stream) throws Exception;
}
