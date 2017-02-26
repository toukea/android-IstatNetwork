package istat.android.network.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;


/**
 * Created by Istat Toukea on 22/09/2016.
 */
public class StreamOperationTools {

    public final static int DEFAULT_BUFFER_SIZE = 16384;
    public final static String DEFAULT_ENCODING = "UTF-8";


    public static String streamToLinearisedString(OperationController controller, java.io.InputStream inp,
                                                  String encoding) throws IOException {
        String out;
        StringBuilder total = new StringBuilder();
        String line;

        BufferedReader r = new BufferedReader(new InputStreamReader(
                inp, encoding));
        while ((line = r.readLine()) != null) {
            pauseControl(controller);
            total.append(line);
        }
        out = total.toString();
        inp.close();

        return out;

    }

    public static String streamToString(OperationController controller, java.io.InputStream inp) throws IOException {
        return streamToString(controller, inp, DEFAULT_ENCODING);
    }

    public static String streamToString(OperationController controller, java.io.InputStream inp, int buffer) throws IOException {
        return streamToString(controller, inp, buffer, null);
    }

    public static String streamToString(OperationController controller, java.io.InputStream inp,
                                        String encoding) throws IOException {
        return streamToString(controller, inp, DEFAULT_BUFFER_SIZE, encoding);

    }

    public static String streamToString(OperationController controller, java.io.InputStream inp,
                                        int buffer, String encoding) throws IOException {
        String out = "";
        byte[] b = new byte[buffer];
        int read;
        while ((read = inp.read(b)) > -1) {
            pauseControl(controller);
            if (controller.isStopped()) {
                Log.i("HttpQuery", "streamToString::aborted");
                break;
            }
            out = out
                    + (encoding != null ? new String(b, 0, read,
                    encoding) : new String(b, 0, read));
        }
        inp.close();
        return out;
    }

    public static void pauseControl(OperationController controller) {
        if (controller.isPaused()) {
            while (controller.isPaused()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //------------------------------------------------------------
    public static OperationController copyStream(InputStream is, OutputStream os) throws IOException {


        return copyStream(is, os, DEFAULT_BUFFER_SIZE, 0);
    }

    public static OperationController copyStream(byte[] prefix, byte[] suffix,
                                                 InputStream is, OutputStream os) throws IOException {
        OperationController controller = new OperationController();
        copyStream(controller, prefix, suffix, is, os, DEFAULT_BUFFER_SIZE, 0);
        return controller;
    }

    public static OperationController copyStream(InputStream is, OutputStream os,
                                                 int buffer_size) throws IOException {
        return copyStream(is, os, buffer_size, 0);
    }

    public static OperationController copyStream(InputStream is, OutputStream os,
                                                 int buffer_size, int startByte) throws IOException {
        OperationController controller = new OperationController();
        copyStream(controller, null, null, is, os, buffer_size, startByte);
        return controller;
    }

    //------------------------------------------------------
    public static OutputStream copyStream(OperationController controller, InputStream is, OutputStream os) throws IOException {
        return copyStream(controller, is, os, DEFAULT_BUFFER_SIZE, 0);
    }

    public static OutputStream copyStream(OperationController controller, byte[] prefix, byte[] suffix,
                                          InputStream is, OutputStream os) throws IOException {
        return copyStream(controller, prefix, suffix, is, os, DEFAULT_BUFFER_SIZE, 0);
    }

    public static OutputStream copyStream(OperationController controller, InputStream is, OutputStream os,
                                          int buffer_size) throws IOException {
        return copyStream(controller, is, os, buffer_size, 0);
    }

    public static OutputStream copyStream(OperationController controller, InputStream is, OutputStream os,
                                          int buffer_size, int startByte) throws IOException {
        return copyStream(controller, null, null, is, os, buffer_size, startByte);
    }

    public static OutputStream copyStream(OperationController controller, byte[] prefix, byte[] suffix,
                                          InputStream inp, OutputStream os, int buffer, int startByte) throws IOException {
        byte[] b = new byte[buffer];
        int read;
        inp.skip(startByte);
        if (prefix != null && prefix.length > 0)
            os.write(prefix);
        while ((read = inp.read(b)) > -1) {
            pauseControl(controller);
            if (controller.isStopped()) {
                Log.i("HttpQuery", "streamToString::aborted");
                break;
            }
            os.write(b, 0, read);
        }
        if (suffix != null && suffix.length > 0)
            os.write(suffix);
        os.close();
        return os;
    }

    public static RandomAccessFile copyStream(OperationController controller, byte[] prefix, byte[] suffix,
                                              InputStream inp, RandomAccessFile os, int buffer_size,
                                              int startByte) throws IOException {
        byte[] bytes = new byte[buffer_size];
        int read;
        inp.skip(startByte);
        os.seek(startByte);
        if (prefix != null && prefix.length > 0)
            os.write(prefix);
        while ((read = inp.read(bytes)) > -1) {
            pauseControl(controller);
            if (controller.isStopped()) {
                Log.i("HttpQuery", "streamToString::aborted");
                break;
            }
            os.write(bytes, 0, read);
        }
        if (suffix != null && suffix.length > 0)
            os.write(suffix);
        os.close();
        return os;
    }

    public static OutputStream[] dispatch(OperationController controller, int buffer_size, InputStream is, OutputStream... oss) throws IOException {
        byte[] bytes = new byte[buffer_size];
        for (; ; ) {
            // Read byte from input stream
            int count = is.read(bytes, 0, buffer_size);
            if (count == -1)
                break;
            for (OutputStream os : oss) {
                os.write(bytes, 0, count);
            }
        }
        return oss;
    }

    public static class OperationController {
        boolean paused = false;
        boolean stopped = false;

        public void pause() {
            paused = true;
        }

        public void resume() {
            paused = false;
        }

        public void stop() {
            stopped = true;
        }

        public boolean isPaused() {
            return paused;
        }

        public boolean isStopped() {
            return stopped;
        }
    }
}
