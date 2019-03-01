package istat.android.network.http.tools;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import istat.android.network.http.HttpAsyncQuery;
import istat.android.network.http.HttpQuery;
import istat.android.network.utils.ToolKits.Stream;

public abstract class WrittenByteUploadHandler extends HttpAsyncQuery.UploadHandler {
    int buffer = Stream.DEFAULT_BUFFER_SIZE;

    long publishTimeInterval = -1;

    public WrittenByteUploadHandler() {
        this(Stream.DEFAULT_BUFFER_SIZE, -1);
    }

    public WrittenByteUploadHandler(int bufferSize) {
        this(bufferSize, -1);
    }

    public WrittenByteUploadHandler(int bufferSize, long publishTimeInterval) {
        this.buffer = bufferSize;
        this.publishTimeInterval = publishTimeInterval;
    }

    public void setPublishTimeInterval(int publishTimeInterval) {
        this.publishTimeInterval = publishTimeInterval;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public int getBuffer() {
        return buffer;
    }

    @Override
    public void onProceedStreamUpload(long uploadSize, InputStream stream, OutputStream request, HttpAsyncQuery asyc)
            throws IOException {
        HttpQuery httpQuery = asyc.getHttpQuery();
        long lastPublishTime = 0;
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
            if (publishTimeInterval > 0 && System.currentTimeMillis() - lastPublishTime > publishTimeInterval) {
                publishProgression(totalWrite, uploadSize, writePercentage);
                lastPublishTime = System.currentTimeMillis();
            }
        }
        stream.close();
        request.close();
    }

}
