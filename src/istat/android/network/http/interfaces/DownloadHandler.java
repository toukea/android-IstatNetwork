package istat.android.network.http.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import istat.android.network.http.HttpAsyncQuery;

/**
 * Created by istat on 05/11/16.
 */

public interface DownloadHandler {
    Object onBuildResponseBody(HttpURLConnection connexion,
                               InputStream stream, HttpAsyncQuery query) throws Exception;
}
