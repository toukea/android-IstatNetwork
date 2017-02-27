package istat.android.network.http.tools;

import android.util.Log;

import org.apache.http.client.methods.HttpOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;

import istat.android.network.http.HttpAsyncQuery;
import istat.android.network.http.HttpQueryOptions;
import istat.android.network.utils.StreamOperationTools;

/**
 * Created by istat on 13/02/17.
 */

public abstract class FileDownloadHandler extends HttpAsyncQuery.HttpDownloadHandler<Integer> {
    String destinationPath;

    public FileDownloadHandler(String destination) {
        this.destinationPath = destination;
    }

    public FileDownloadHandler(File destination) {
        this(destination.getAbsolutePath());
    }

    @Override
    public File onBuildResponseBody(HttpURLConnection connexion, InputStream stream) throws Exception {
        File file = new File(destinationPath);
        FileOutputStream os = new FileOutputStream(file);
        HttpQueryOptions options = this.getQuery().getOptions();
        StreamOperationTools.copyStream(this.getAsyncQuery().executionController, stream, os, options.getBufferSize());
        return file;
    }
}
