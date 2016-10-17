package istat.android.network.http.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import istat.android.network.http.MultipartHttpQuery;

/**
 * Created by istat on 17/10/16.
 */

public interface UpLoadHandler {
    public void onProceedStreamUpload(MultipartHttpQuery httpQuery,
                                      OutputStream request, InputStream stream)
            throws IOException;
}
