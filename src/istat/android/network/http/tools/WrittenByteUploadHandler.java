package istat.android.network.http.tools;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import istat.android.network.http.HttpAsyncQuery;
import istat.android.network.http.HttpAsyncQuery.HttpUploadHandler;
import istat.android.network.http.HttpQuery;
import istat.android.network.http.MultipartHttpQuery;
import istat.android.network.utils.ToolKits.Stream;

public abstract class WrittenByteUploadHandler extends HttpUploadHandler<Long> {
    int buffer = Stream.DEFAULT_BUFFER_SIZE;

    public WrittenByteUploadHandler() {

    }

    public WrittenByteUploadHandler(int bufferSize) {
        this.buffer = bufferSize;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public int getBuffer() {
        return buffer;
    }

    @Override
    public void onProceedStreamUpload(long uploadSize, OutputStream request, InputStream stream, HttpAsyncQuery asyc)
            throws IOException {
        HttpQuery httpQuery = asyc.getHttpQuery();
        byte[] b = new byte[buffer];
        int write;
        long totalWrite = 0;
        while ((write = stream.read(b)) > -1) {
            if (httpQuery.isAborted() || !httpQuery.hasRunningRequest()) {
                Log.i("Uploader", "onUploadStream::Aborted");
                stream.close();
                return;
            }
            request.write(b, 0, write);
            request.flush();
            totalWrite += write;
            long writePercentage = uploadSize > 0 ? (100 * totalWrite / uploadSize) : 0;
            publishProgression(totalWrite, uploadSize, writePercentage);
        }
        stream.close();
        request.close();

    }

}
