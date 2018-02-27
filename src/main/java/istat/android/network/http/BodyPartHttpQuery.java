package istat.android.network.http;


import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import istat.android.network.http.interfaces.UpLoadHandler;
import istat.android.network.utils.ToolKits;

/**
 * Created by istat on 28/12/16.
 */

public class BodyPartHttpQuery extends HttpQuery<BodyPartHttpQuery> {
    Object part;

    public BodyPartHttpQuery(Object obj) {
        this.part = obj;
    }

    public BodyPartHttpQuery(File obj) {
        this((Object) obj);
    }

    public BodyPartHttpQuery(JSONObject jsonObject) {
        this((Object) jsonObject);
        this.setContentType("Application/json");
    }

    public BodyPartHttpQuery(Object obj, UpLoadHandler handler) {
        this.part = obj;
        if (handler != null) {
            this.uploadHandler = handler;
        }
    }

    public BodyPartHttpQuery() {

    }

    public BodyPartHttpQuery setBody(Object body) {
        this.part = body;
        return this;
    }

    /**
     * @param Name
     * @param Value
     * @return
     * @throws RuntimeException then called due to addParam not supported for BodyPart.
     */
    @Override
    public BodyPartHttpQuery addParam(String Name, String Value) throws RuntimeException {
        throw new RuntimeException("Not supported.");
    }

    @Override
    public InputStream doQuery(String url, String method, boolean holdError) throws IOException {
        if (!method.equals("POST") && method.equals("PUT")) {
            throw new RuntimeException("Method Not supported. can do " + method + " from BodyPart Http Query");
        }
        return super.doQuery(url, method, holdError);
    }

    @Override
    protected synchronized InputStream doQuery(String url, String method, boolean bodyData, boolean holdError) throws IOException {
        long length = 0;
        String data = "";
        if (parameterHandler != null) {
            data = parameterHandler.onStringifyQueryParams(method, parameters,
                    mOptions.encoding);
        }
        if (!ToolKits.Text.isEmpty(data)) {
            url += (url.contains("?") ? "" : "?") + data;
            length = data.length();
        }
        addToOutputHistoric(length);
        return super.doQuery(url, method, bodyData, holdError);
    }

    public static InputStream doPost(Object object, String url) throws IOException {
        BodyPartHttpQuery http = new BodyPartHttpQuery(object, null);
        return http.doPost(url);
    }

    public static InputStream doPut(Object object, String url) throws IOException {
        BodyPartHttpQuery http = new BodyPartHttpQuery(object, null);
        return http.doPut(url);
    }

    public static InputStream doPost(Object object, UpLoadHandler uploadHandler, String url) throws IOException {
        return doPut(object, null);
    }

    public static InputStream doPut(Object object, UpLoadHandler uploadHandler, String url) throws IOException {
        BodyPartHttpQuery http = new BodyPartHttpQuery(object);
        return http.doPut(url);
    }

    public static AsyncHttp createAsync(Object obj) {
        return createAsync(obj, null);
    }

    public static AsyncHttp createAsync(Object obj, UpLoadHandler uploadHandler) {
        BodyPartHttpQuery http = new BodyPartHttpQuery(obj, uploadHandler);
        return AsyncHttp.from(http);
    }

    final static long SIZE_1MB = 1024 * 1024 * 1024;

    @Override
    protected long onWriteDataInToOutputStream(String method, OutputStream dataOutputStream) throws IOException {
        long size;
        if (part instanceof InputStream) {
            InputStream inputStream = (InputStream) part;
            size = inputStream.available();
            this.currentConnection.setChunkedStreamingMode(mOptions.chunkedStreamingMode);
            getUploadHandler().onUploadStream(size, inputStream, dataOutputStream);
        } else if (part instanceof File) {
            this.currentConnection.setChunkedStreamingMode(mOptions.chunkedStreamingMode);
            size = onWriteFileToOutputStream((File) this.part, dataOutputStream);
        } else {
            String encoding = getOptions().encoding;
            String sendable = part.toString();
            size = sendable.length();
            if (size >= SIZE_1MB) {
                this.currentConnection.setChunkedStreamingMode(mOptions.chunkedStreamingMode);
            }
            InputStream inputStream = new ByteArrayInputStream(sendable.getBytes(encoding));
            getUploadHandler().onUploadStream(size, inputStream, dataOutputStream);
        }
        return size;
    }

    private long onWriteFileToOutputStream(File file, OutputStream dataOutputStream) throws IOException {
        long size;
        InputStream stream;
        if (file == null || !file.exists()) {
            if (file == null) {
                throw new IOException("File can't be NULL");
            }
            throw new IOException("File not found with path=" + file.getAbsolutePath());
        } else {
            size = file.length();
            stream = new FileInputStream(file);
        }
        getUploadHandler().onUploadStream(size, stream, dataOutputStream);
        return size;
    }

}
