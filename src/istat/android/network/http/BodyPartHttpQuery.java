package istat.android.network.http;


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

    public BodyPartHttpQuery(Object obj, UpLoadHandler handler) {
        this.part = obj;
        if (handler != null) {
            this.uploadHandler = handler;
        }
    }

    /**
     * @param Name
     * @param Value
     * @return
     * @throws RuntimeException when called due to addParam not supported for BodyPart.
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

    @Override
    protected long onWriteDataToOutputStream(String method, OutputStream dataOutputStream) throws IOException {
        long size = 0;
        if (part instanceof File) {
            onWriteFileToOutputStream((File) this.part, dataOutputStream);
        } else {
            InputStream inputStream = new ByteArrayInputStream(part.toString().getBytes());
            getUploadHandler().onUploadStream(this, inputStream, dataOutputStream);
        }
        return size;
    }

    private long onWriteFileToOutputStream(File file, OutputStream dataOutputStream) throws IOException {
        long size = 0;
        InputStream stream;
        if (file == null || !file.exists()) {
            if (file == null) {
                throw new IOException("File can't be NULL");
            }
            throw new IOException("File not found with path=" + file.getAbsolutePath());
        } else {
            stream = new FileInputStream(file);
        }
        getUploadHandler().onUploadStream(this, stream, dataOutputStream);
        return size;
    }


    public void setUploadHandler(UpLoadHandler uploadHandler) {
        this.uploadHandler = uploadHandler;
    }

}
