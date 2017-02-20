package istat.android.network.http.tools;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;

import istat.android.network.http.HttpAsyncQuery;

/**
 * Created by istat on 13/02/17.
 */

public abstract class FileDownloadHandler extends HttpAsyncQuery.HttpDownloadHandler<Integer> {

    @Override
    public File onBuildResponseBody(HttpURLConnection connexion, InputStream stream, HttpAsyncQuery query) throws Exception {
        return null;
    }
}
