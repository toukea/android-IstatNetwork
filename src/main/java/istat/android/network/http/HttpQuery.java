package istat.android.network.http;

import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import istat.android.network.http.interfaces.UpLoadHandler;
import istat.android.network.http.utils.HttpUtils;
import istat.android.network.utils.ToolKits;
import istat.android.network.utils.ToolKits.Text;

/*
 * Copyright (C) 2014 Istat Dev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Toukea Tatsi (Istat)
 */
public abstract class HttpQuery<HttpQ extends HttpQuery<?>> {
    public final static String METHOD_GET = "GET",
            METHOD_POST = "POST",
            METHOD_HEAD = "HEAD",
            METHOD_PUT = "PUT",
            METHOD_PATCH = "PATCH",
            METHOD_DELETE = "DELETE";
    protected HttpQueryOptions mOptions = new HttpQueryOptions();
    protected List<String> urlPramNames = new ArrayList<String>();
    protected HashMap<String, String> parameters = new HashMap<String, String>();
    protected HashMap<String, String> headers = new HashMap<String, String>();
    private volatile boolean aborted = false;
    static String TAG_INPUT = "input", TAG_OUTPUT = "output";
    volatile HttpURLConnection currentConnection;
    long lastConnectionTime = System.currentTimeMillis();
    protected HashMap<String, List<Long>> historic = new HashMap<String, List<Long>>() {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put(TAG_INPUT, new ArrayList<Long>());
            put(TAG_OUTPUT, new ArrayList<Long>());
        }

    };
    static HashMap<String, List<Long>> histories = new HashMap<String, List<Long>>() {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put(TAG_INPUT, new ArrayList<Long>());
            put(TAG_OUTPUT, new ArrayList<Long>());
        }

    };
    protected HashMap<String, List<Long>> timeHistoric = new HashMap<String, List<Long>>() {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put(TAG_INPUT, new ArrayList<Long>());
            put(TAG_OUTPUT, new ArrayList<Long>());
        }

    };
    static HashMap<String, List<Long>> timeHistories = new HashMap<String, List<Long>>() {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put(TAG_INPUT, new ArrayList<Long>());
            put(TAG_OUTPUT, new ArrayList<Long>());
        }

    };

    HttpQuery() {

    }

    @SuppressWarnings("unchecked")
    public HttpQ addHeader(String name, String value) {
        headers.put(name, value);
        return (HttpQ) this;
    }

    public HttpQ addHeaders(HashMap<String, String> headers) {
        this.headers.putAll(headers);
        return (HttpQ) this;
    }

    private HashMap<String, String> getUrlParameters() {
        HashMap<String, String> urlParameters = new HashMap<String, String>();
        for (String tmp : urlPramNames) {
            if (parameters.containsKey(tmp))
                urlParameters.put(tmp, parameters.get(tmp));
        }
        return urlParameters;
    }

    protected HttpQ addParam(String Name, String Value, boolean urlParam) {
        addParam(Name, Value);
        if (urlParam) {
            urlPramNames.add(Name);
        }
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    protected HttpQ addParam(String Name, String Value) {
        parameters.put(Name, Value);
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    protected HttpQ addParams(HashMap<?, ?> nameValues) {
        if (!nameValues.keySet().isEmpty()) {
            String[] table = new String[nameValues.size()];
            table = nameValues.keySet().toArray(table);
            for (String tmp : table) {
                if (tmp != null) {
                    Object obj = nameValues.get(tmp);
                    if (obj != null) {
                        addParam(tmp, obj.toString());
                    }
                }
            }
        }
        return (HttpQ) this;
    }


    /**
     * add param as URL parameter.
     *
     * @param Name
     * @param Value
     * @return
     */
    public HttpQ addURLParam(String Name, String Value) {
        return addParam(Name, Value, true);
    }

    public HttpQ addURLParam(String Name, String[] values) {
        for (int i = 0; i < values.length; i++) {
            addURLParam(Name + "[" + i + "]", values[i]);
        }
        return (HttpQ) this;
    }

    public HttpQ addURLParams(HashMap<String, Object> nameValues) {
        if (!nameValues.keySet().isEmpty()) {
            String[] table = new String[nameValues.size()];
            table = nameValues.keySet().toArray(table);
            for (String tmp : table) {
                if (tmp != null) {
                    addURLParam(tmp, nameValues.get(tmp).toString());
                }
            }
        }
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ setContentType(String name) {
        addHeader("Content-Type", name);
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ setAccept(String name) {
        addHeader("Accept", name);
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ setUserAgent(String name) {
        addHeader("User-Agent", name);
        return (HttpQ) this;
    }

    public void removeHeader(String name) {
        headers.remove(name);
    }

    @SuppressWarnings("unchecked")
    public HttpQ clearParams() {
        parameters.clear();
        urlPramNames.clear();
        return (HttpQ) this;

    }

    @SuppressWarnings("unchecked")
    public HttpQ clearHeaders() {
        headers.clear();
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ clearExtraData() {
        clearHeaders();
        clearParams();
        return (HttpQ) this;
    }

    public void setUploadHandler(UpLoadHandler uploadHandler) {
        if (uploadHandler == null) {
            uploadHandler = getDefaultUploader();
        }
        this.uploadHandler = uploadHandler;
    }

    int uploadBufferSize = ToolKits.Stream.DEFAULT_BUFFER_SIZE;

    public void setUploadBufferSize(int uploadBufferSize) {
        this.uploadBufferSize = uploadBufferSize;
    }

    protected UpLoadHandler getUploadHandler() {
        return uploadHandler;
    }

    UpLoadHandler uploadHandler = getDefaultUploader();

    public InputStream doPost(String url) throws IOException {
        return doQuery(url, "POST", true, true);
    }

    public InputStream doGet(String url) throws IOException {
        return doGet(url, true);
    }

    public InputStream doPut(String url) throws IOException {
        String method = "PUT";
        return doQuery(url, method, true, true);
    }

    public Map<String, List<String>> doHead(String url) throws IOException {
        doQuery(url, "HEAD");
        return currentConnection.getHeaderFields();
    }

    public InputStream doDelete(String url) throws IOException {
        return doQuery(url, "DELETE", true, true);
    }

    public InputStream doCopy(String url) throws IOException {
        return doQuery(url, "COPY", true, true);
    }

    public InputStream doPatch(String url) throws IOException {
        return doQuery(url, "PATCH", true, true);
    }

    public InputStream doGet(String url, boolean handleError)
            throws IOException {
        // ---------------------------
        String method = "GET";
        return doQuery(url, method, false, handleError);
    }

    public InputStream doQuery(String url, String method) throws IOException {
        return doQuery(url, method, true);
    }

    public InputStream doQuery(String url, String method, boolean holdError)
            throws IOException {
        return doQuery(url, method, false, holdError);
    }

    protected synchronized InputStream doQuery(String url, String method, boolean bodyDataEnable, boolean holdError)
            throws IOException {
        long length = 0;
        String data = "";
        if (!bodyDataEnable || !urlPramNames.isEmpty()) {
            HashMap<String, String> urlParameters = getUrlParameters();
            HashMap<String, String> parameters = new HashMap();
            if (!bodyDataEnable) { //pas de body Data
                parameters.putAll(this.parameters);
            } else if (!urlParameters.isEmpty()) {//hasUrl param enable
                parameters.putAll(urlParameters);
            }

            if (parameterHandler != null) {
                data = parameterHandler.onStringifyQueryParams(method, parameters,
                        mOptions.encoding);
            }
            if (!Text.isEmpty(data)) {
                url += (url.contains("?") ? "" : "?") + data;
                length = data.length();
            }
        }
        try {
            HttpURLConnection connection = prepareConnection(url, method);
            if (bodyDataEnable) {//data uploading
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                length = writeDataInToOutputStream(method, os);
                os.close();
            }
            InputStream stream = eval(connection, holdError);
            addToOutputHistoric(length);
            onQueryComplete();
            return stream;
        } catch (IOException e) {
            if (isAborted()) {
                throw new AbortionException(this, e);
            } else {
                throw e;
            }
        }
    }

    protected void configureConnection(HttpURLConnection conn) throws IOException {
        if (mOptions != null) {
            applyOptions(conn);
        }
        fillHeader(conn);
    }

    protected HttpURLConnection prepareConnection(final String url,
                                                  String method) throws IOException {
        onQueryStarting();
        URL Url = new URL(url);
        URLConnection urlConnexion = Url.openConnection();
        HttpURLConnection conn = (HttpURLConnection) urlConnexion;
        configureConnection(conn);
        try {
            conn.setRequestMethod(method);
        } catch (Exception ex) {
            try {
                final Class<?> httpURLConnectionClass = conn.getClass();
                final Class<?> parentClass = httpURLConnectionClass.getSuperclass();
                final Field methodField;
                if (parentClass == HttpsURLConnection.class) {
                    methodField = parentClass.getSuperclass().getDeclaredField(
                            "method");
                } else {
                    methodField = parentClass.getDeclaredField("method");
                }
                methodField.setAccessible(true);
                methodField.set(conn, method);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        currentConnection = conn;
        return conn;
    }

    private UpLoadHandler getDefaultUploader() {
        return new UpLoadHandler() {
            @Override
            public void onUploadStream(long uploadSize, InputStream stream, OutputStream request)
                    throws IOException {
                byte[] b = new byte[uploadBufferSize];
                int read;
                while ((read = stream.read(b)) > -1) {
                    request.write(b, 0, read);
                }
            }
        };
    }

    public interface ParameterHandler {
        ParameterHandler DEFAULT_HANDLER = new ParameterHandler() {

            @Override
            public String onStringifyQueryParams(String method,
                                                 HashMap<String, String> params, String encoding) {
                try {
                    return createUrlQueryParamSequence(params, encoding);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        };

        String onStringifyQueryParams(String method, HashMap<String, String> params, String encoding);
    }

    ParameterHandler parameterHandler = ParameterHandler.DEFAULT_HANDLER;

    void setParameterHandler(ParameterHandler parameterHandler) {
        if (parameterHandler != null) {
            this.parameterHandler = parameterHandler;
        }
    }

    private void applyOptions(HttpURLConnection conn) {
        if (mOptions.chunkedStreamingMode > 0)
            conn.setChunkedStreamingMode(mOptions.chunkedStreamingMode);
        if (mOptions.fixedLengthStreamingMode > 0)
            conn.setFixedLengthStreamingMode(mOptions.fixedLengthStreamingMode);
        if (mOptions.followRedirects)
            HttpURLConnection.setFollowRedirects(mOptions.followRedirects);
        if (mOptions.instanceFollowRedirects)
            conn.setInstanceFollowRedirects(mOptions.instanceFollowRedirects);
        if (mOptions.useCaches)
            conn.setUseCaches(mOptions.useCaches);
        if (mOptions.soTimeOut > 0)
            conn.setReadTimeout(mOptions.soTimeOut);
        if (mOptions.connexionTimeOut > 0)
            conn.setConnectTimeout(mOptions.connexionTimeOut);
    }

    private void fillHeader(HttpURLConnection conn) {
        if (!headers.keySet().isEmpty()) {
            String[] table = new String[headers.size()];
            table = headers.keySet().toArray(table);
            for (String tmp : table) {
                if (tmp != null) {
                    conn.addRequestProperty(tmp, headers.get(tmp));
                }
            }
        }
    }

    protected final long writeDataInToOutputStream(String method,
                                                   OutputStream os) throws IOException {
        currentOutputStream = os;
        long length = onWriteDataInToOutputStream(method, currentOutputStream);
        if (length > 0) {
            currentOutputStream.flush();
        }
        return length;
    }

    protected long onWriteDataInToOutputStream(String method,
                                               OutputStream dataOutputStream) throws IOException {
        String encoding = getOptions().encoding;
        String data = "";
        if (parameterHandler != null) {
            data = parameterHandler.onStringifyQueryParams(method, parameters, encoding);
        }
        if (!TextUtils.isEmpty(data)) {
            ByteArrayInputStream stream = new ByteArrayInputStream(data.getBytes(encoding));
            this.uploadHandler.onUploadStream(data.length(), stream, dataOutputStream);
            return data.length();
        } else {
            return 0;
        }
    }

    public String getURL(String address) throws IOException {
        // --------------------------
        String paramString = createUrlQueryParamSequence(parameters,
                mOptions.encoding);
        return address
                + (paramString == null || paramString.equals("") ? "" : "?"
                + paramString);
    }

    public void shutDownConnection() {
        currentConnection.disconnect();
        currentConnection = null;
        onQueryComplete();
        aborted = true;
    }

    public boolean isAutoClearParamsEnable() {
        return mOptions.autoClearRequestParams;
    }

    public HttpURLConnection getCurrentConnection() {
        return currentConnection;
    }

    public boolean hasPendingRequest() {
        return currentConnection != null;
    }

    public boolean hasRunningRequest() {
        return hasPendingRequest()
                && (currentInputStream != null || currentOutputStream != null);
    }

    public boolean isAborted() {
        return aborted;
    }

    public void setConnectionTimeOut(int milliSec) {
        mOptions.connexionTimeOut = milliSec;

    }

    public void setSoTimeOut(int milliSec) {
        mOptions.connexionTimeOut = milliSec;
    }

    public int getConnectionTimeOut() {
        return mOptions.connexionTimeOut;
    }

    public int getSoTimeOut() {
        return mOptions.soTimeOut;
    }

    public void setEncoding(String encoding) {
        this.mOptions.encoding = encoding;
    }

    public void setClearParamsAfterEachQueryEnable(boolean autoClearParams) {
        this.mOptions.autoClearRequestParams = autoClearParams;
    }

    public static String createUrlQueryParamSequence(
            HashMap<String, String> params, String encoding)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(name, encoding));
            result.append("=");
            result.append(URLEncoder.encode(value, encoding));
        }

        return result.toString();
    }

    void onQueryStarting() {
        lastConnectionTime = System.currentTimeMillis();
        aborted = false;
    }

    void onQueryComplete() {
        onQueryFinished();
    }

    void onQueryFinished() {
        if (mOptions != null && mOptions.autoClearRequestParams) {
            clearParams();
        }
        currentInputStream = null;
        currentOutputStream = null;
    }

    void addToInputHistoric(long input) {
        historic.get(TAG_INPUT).add(input);
        histories.get(TAG_INPUT).add(input);
        Long elapsed = System.currentTimeMillis() - lastConnectionTime;
        timeHistoric.get(TAG_INPUT).add(elapsed);
        timeHistories.get(TAG_INPUT).add(elapsed);
    }

    void addToOutputHistoric(long input) {
        historic.get(TAG_OUTPUT).add(input);
        histories.get(TAG_OUTPUT).add(input);
        Long elapsed = System.currentTimeMillis() - lastConnectionTime;
        timeHistoric.get(TAG_OUTPUT).add(elapsed);
        timeHistories.get(TAG_OUTPUT).add(elapsed);
    }

    protected volatile InputStream currentInputStream;
    protected volatile OutputStream currentOutputStream;

    InputStream eval(HttpURLConnection conn, boolean handleError)
            throws IOException {
        int eval = 0;
        InputStream stream = null;
        int responseCode = 500;
        if (conn != null) {
            responseCode = conn.getResponseCode();
            eval = conn.getContentLength();
        }
        if (!isAborted()) {
            if (HttpUtils.isSuccessCode(responseCode)) {
                stream = conn.getInputStream();
            } else if (handleError) {
                stream = conn.getErrorStream();
            }
        }
        currentInputStream = stream;
        if (isAborted()) {
            if (stream != null) {
                stream.close();
                conn.disconnect();
            }
            throw new AbortionException(this);
        }
        if (stream != null) {
            eval = stream.available();
        }
        addToInputHistoric(eval);
        return stream;
    }

    int getCurrentResponseCode() {
        try {
            final HttpURLConnection connection = getCurrentConnection();
            int responseCode = connection.getResponseCode();
            return responseCode;
        } catch (IOException e) {
            return -1;
        }
    }

    String getCurrentResponseMessage() {
        try {
            final HttpURLConnection connection = getCurrentConnection();
            String responseMessage = connection.getResponseMessage();
            //         Log.e("HttQuery", "httpMessage=" + responseMessage);
            return responseMessage;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean abortRequest() {
        aborted = true;
        boolean out = hasPendingRequest();
        Log.e("HttQuery", "abortRequest_start::runningRequest=" + out);
        if (out) {
            if (currentOutputStream != null) {
                try {
                    currentOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (currentInputStream != null) {
                try {
                    currentInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                currentConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            onQueryFinished();
        }
        return out;
    }

    public List<Long> getCurrentOutputContentLegthHistoric() {
        return historic.get(TAG_OUTPUT);
    }

    public List<Long> getCurrentInputContentLegthHistoric() {
        return historic.get(TAG_INPUT);
    }

    public long getCurrentOutputContentLegth() {
        long out = 0;
        for (long i : historic.get(TAG_OUTPUT)) {
            out += i;
        }
        return out;
    }

    public long getCurrentInputContentLegth() {
        long out = 0;
        for (long i : historic.get(TAG_INPUT)) {
            out += i;
        }
        return out;
    }

    // -----------------------------------------
    public List<Long> getCurrentOutputTimeHistoric() {
        return timeHistoric.get(TAG_OUTPUT);
    }

    public List<Long> getCurrentInputTimeHistoric() {
        return timeHistoric.get(TAG_INPUT);
    }

    public long getCurrentOutputTime() {
        long out = 0;
        for (long i : timeHistoric.get(TAG_OUTPUT)) {
            out += i;
        }
        return out;
    }

    public long getCurrentInputTime() {
        long out = 0;
        for (long i : timeHistoric.get(TAG_INPUT)) {
            out += i;
        }
        return out;
    }

    public void setOptions(HttpQueryOptions option) {
        this.mOptions = option;
        if (this.mOptions == null) {
            this.mOptions = new HttpQueryOptions();
        }
    }

    public HttpQueryOptions getOptions() {
        return mOptions;
    }

    public static List<Long> getOutputContentLegthHistoric() {
        return histories.get(TAG_OUTPUT);
    }

    public static List<Long> getInputContentLegthHistoric() {
        return histories.get(TAG_INPUT);
    }

    public static long getOutputContentLegth() {
        long out = 0;
        for (long i : histories.get(TAG_OUTPUT)) {
            out += i;
        }
        return out;
    }

    public static long getInputContentLegth() {
        long out = 0;
        for (long i : histories.get(TAG_INPUT)) {
            out += i;
        }
        return out;
    }

    public static List<Long> getOutputTimeHistoric() {
        return timeHistories.get(TAG_OUTPUT);
    }

    public static List<Long> getInputTimeHistoric() {
        return timeHistories.get(TAG_INPUT);
    }

    public static long getOutputTime() {
        long out = 0;
        for (long i : timeHistories.get(TAG_OUTPUT)) {
            out += i;
        }
        return out;
    }

    public static long getInputTime() {
        long out = 0;
        for (long i : timeHistories.get(TAG_INPUT)) {
            out += i;
        }
        return out;
    }

    public boolean disconnect() {
        boolean out = hasPendingRequest();
        if (getCurrentConnection() != null) {
            getCurrentConnection().disconnect();
        }
        return out;
    }

    public static class AbortionException extends IOException {
        HttpQuery httpQuery;

        AbortionException(HttpQuery http) {
            super("HttpQuery defined by: " + http + ", has been aborted. None IO action can be done anymore.");
            this.httpQuery = http;
        }

        AbortionException(HttpQuery http, Throwable cause) {
            this(http);
            initCause(cause);
        }

        public HttpQuery getHttpQuery() {
            return httpQuery;
        }
    }
}
