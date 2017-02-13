package istat.android.network.http.tools;

import java.io.InputStream;
import java.net.HttpURLConnection;

import istat.android.network.http.HttpAsyncQuery;
import istat.android.network.utils.ToolKits.Stream;

public abstract class ReadByteDownloadHandler extends
        HttpAsyncQuery.HttpDownloadHandler<Integer> {
    int buffer = Stream.DEFAULT_BUFFER_SIZE;
    String encoding = Stream.DEFAULT_ENCODING;

    public ReadByteDownloadHandler() {

    }

    public ReadByteDownloadHandler(String encoding, int bufferSize) {
        this.encoding = encoding;
        this.buffer = bufferSize;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public int getBuffer() {
        return buffer;
    }
//
//	/**
//	 * @param vars
//	 *            un tableaqu d'entier representant la progression.
//	 *            vars[0]=nombre de byte deja lu, vars[1]=nobre total de byte a
//	 *            lire, vars[2] le pourcentage deja lue
//	 */

    @Override
    public Object onBuildResponseBody(HttpURLConnection currentConnexion,
                                      InputStream inp, HttpAsyncQuery query) {
        String out = "";
        byte[] b = new byte[buffer];
        int read;
        int totalReade = 0;
        int streamSize = currentConnexion == null ? 0 : currentConnexion
                .getContentLength();
        try {
            streamSize = streamSize == 0 ? inp.available() : streamSize;
            while ((read = inp.read(b)) > -1) {

                if (query.isCancelled() || !query.isRunning()) {
                    return null;
                }
                out += (encoding != null ? new String(b, 0, read, encoding)
                        : new String(b, 0, read));
                totalReade += read;
                publishProgression(totalReade, streamSize,
                        streamSize > 0 ? (100 * totalReade / streamSize) : -1);
            }
            inp.close();
        } catch (Exception e) {
        }

        return out;
    }
    /*
    byte[] b = new byte[this.buffer];
        int write;
        long totalWrite = 0;
        long uploadSize = getFileToUpload().length();
        long writePercentage;
        long time = System.currentTimeMillis();
        long debit = -1;
        long writeLastSec = 0;
        long newTimeRight = -1;
        long oldTimeRight = -1;
        long timeRight = -1;
        long timeDiff;
        while ((write = stream.read(b)) > -1) {
            executePauseControl(asyc);
            if (httpQuery.isAborted() || !httpQuery.hasRunningRequest()) {
                Log.i("UploadManager", "onProceedStreamUpload::Aborted");
                stream.close();
                return;
            }
            request.write(b, 0, write);
            totalWrite += write;
            writeLastSec += write;
            writePercentage = uploadSize > 0 ? (100 * totalWrite / uploadSize) : 0;
            if (oldTimeRight > 0 && newTimeRight > 0) {
                double avg = (newTimeRight + oldTimeRight) / 2;
                if (avg < timeRight) {
                    timeRight = (int) Math.max(newTimeRight, oldTimeRight);
                } else if (timeRight <= 0) {
                    timeRight = newTimeRight;
                }
            }
            callback.publishProgression(totalWrite, uploadSize, writePercentage, debit, timeRight);
            timeDiff = System.currentTimeMillis() - time;
            if (timeDiff >= 1000) {
                debit = writeLastSec;
                oldTimeRight = newTimeRight;
                newTimeRight = ((uploadSize - totalWrite) * 1000) / debit;
                time = System.currentTimeMillis();
                writeLastSec = 0;
            }
        }
        stream.close();
        request.close();
     */

}
