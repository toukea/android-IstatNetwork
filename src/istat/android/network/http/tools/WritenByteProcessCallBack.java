package istat.android.network.http.tools;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import istat.android.network.http.HttpAsyncQuery;
import istat.android.network.http.HttpAsyncQuery.UploadProcessCallBack;
import istat.android.network.http.MultipartHttpQuery;
import istat.android.network.util.ToolKits.Stream;

public class WritenByteProcessCallBack extends UploadProcessCallBack<Integer> {
    int buffer = Stream.DEFAULT_BUFFER_SIZE;

    public WritenByteProcessCallBack() {

    }

    public WritenByteProcessCallBack(int bufferSize) {
        this.buffer = bufferSize;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public int getBuffer() {
        return buffer;
    }

    @Override
    public void onProceedStreamUpload(MultipartHttpQuery httpQuery,
                                      DataOutputStream request, InputStream stream, HttpAsyncQuery asyc)
            throws IOException {
        byte[] b = new byte[buffer];
        int writen = 0;
        int totalWriten = 0;
        int uploadSize = stream.available();
        while ((writen = stream.read(b)) > -1) {
            if (httpQuery.isAborted() || !httpQuery.hasRunningRequest()) {
                Log.i("Uploader", "onProceedStreamUpload::Aborted");
                stream.close();
                return;
            }
            request.write(b, 0, writen);
            request.flush();
            totalWriten += writen;
            int writenPercentage = uploadSize > 0 ? (100 * totalWriten / uploadSize) : 0;
            publishProgression(totalWriten, uploadSize, writenPercentage);
        }
        stream.close();
        request.close();

    }

    @Override
    public void onUpdateUploadProcess(HttpAsyncQuery query, Integer... vars) {

    }

}
