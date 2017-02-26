package istat.android.network.http.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import istat.android.network.http.HttpQuery;
import istat.android.network.http.MultipartHttpQuery;

/**
 * Created by istat on 17/10/16.
 */

public interface UpLoadHandler {
    void onUploadStream(OutputStream request, InputStream stream, HttpQuery httpQuery)
            throws IOException;
}
