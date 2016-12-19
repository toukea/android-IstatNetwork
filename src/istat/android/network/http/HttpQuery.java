package istat.android.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import istat.android.network.http.HttpAsyncQuery.HttpQueryResponse;
import istat.android.network.http.interfaces.HttpSendable;
import istat.android.network.utils.ToolKits;
import istat.android.network.utils.ToolKits.Text;

import org.apache.http.message.BasicNameValuePair;

import android.text.TextUtils;
import android.util.Log;

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
    protected HttpQueryOptions mOptions = new HttpQueryOptions();
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
    static HashMap<String, List<Long>> historics = new HashMap<String, List<Long>>() {

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

    @SuppressWarnings("unchecked")
    public HttpQ addHeader(String name, String value) {
        headers.put(name, value);
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ addParam(String Name, String Value) {
        parameters.put(Name, Value);
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ addParam(String Name, String... values) {
        for (int i = 0; i < values.length; i++) {
            addParam(Name + "[" + i + "]", values[i]);
        }
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ addParam(String Name, HashMap<?, ?> values) {
        Iterator<?> iterator = values.keySet().iterator();
        while (iterator.hasNext()) {
            Object name = iterator.next();
            Object value = values.get(name);
            addParam(Name + "[" + name + "]", value + "");
        }
        return (HttpQ) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HttpQ addSendable(HttpSendable sendable) {
        sendable.onFillHttpQuery(this);
        return (HttpQ) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HttpQ addSendable(HttpSendable... sendableArray) {
        for (HttpSendable sendable : sendableArray) {
            sendable.onFillHttpQuery(this);
        }
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ addParams(List<BasicNameValuePair> nameValues) {
        for (BasicNameValuePair pair : nameValues)
            addParam(pair.getName(), pair.getValue());
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ addParams(HashMap<?, ?> nameValues) {
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

    public HttpQ addParams(Object container) {
        ToolKits.toHashMap(container, true, false, false);
        return (HttpQ) this;
    }

    public HttpQ addParams(Object container, boolean privateAndSuper) {
        ToolKits.toHashMap(container, false, privateAndSuper, false);
        return (HttpQ) this;
    }

    public HttpQ addParams(Object container, String... ignoredFields) {
        ToolKits.toHashMap(container, true, false, false, ignoredFields);
        return (HttpQ) this;
    }

    public HttpQ addParams(Object container, boolean privateAndSuper, String... ignoredFields) {
        ToolKits.toHashMap(container, privateAndSuper, false, false, ignoredFields);
        return (HttpQ) this;
    }

    public void removeParam(String name) {
        parameters.remove(name);
    }

    public void removeHeder(String name) {
        headers.remove(name);
    }

    @SuppressWarnings("unchecked")
    public HttpQ clearParams() {
        parameters.clear();
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


    protected synchronized InputStream doQuery(String url, String method, boolean bodyData, boolean holdError)
            throws IOException {
        Log.d("HttpQuery", "Method=" + method + ", bodyData=" + bodyData + ", holdError=" + holdError + ", url=" + getURL(url));
        long length = 0;
        String data = "";
        if (!bodyData) {
            if (parameterHandler != null) {
                data = parameterHandler.onStringifyQueryParams(method, parameters,
                        mOptions.encoding);
            }
            if (!Text.isEmpty(data)) {
                url += (url.contains("?") ? "" : "?") + data;
                length = data.length();
            }
        }
        HttpURLConnection conn = prepareConnexion(url, method);
        if (bodyData) {
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            length = writeDataToOutputStream(method, os);
            os.close();
        }
        InputStream stream = eval(conn, holdError);
        addToOutputHistoric(length);
        onQueryComplete();
        return stream;
    }

//    protected synchronized InputStream POST(String url, boolean holdError)
//            throws IOException {
//        String method = "POST";
//        HttpURLConnection conn = prepareConnexion(url, method);
//        conn.setDoOutput(true);
//        OutputStream os = conn.getOutputStream();
//        long length = writeDataToOutputStream(method, os);
//        os.close();
//        InputStream stream = eval(conn, holdError);
//        addToOutputHistoric(length);
//        onQueryComplete();
//        return stream;
//    }
//
//    protected synchronized InputStream doQuery(String url, String method, boolean bodyData, boolean holdError)
//            throws IOException {
////        if (method.equalsIgnoreCase("POST")) {
////            return POST(url, holdError);
////        }
//        Log.d("HttpQuery", "Method=" + method);
//        long length = 0;
//        String data = "";
//        if (!bodyData) {
//            if (parameterHandler != null) {
//                data = parameterHandler.onStringifyQueryParams(method, parameters,
//                        mOptions.encoding);
//            }
//            if (!Text.isEmpty(data)) {
//                url += (url.contains("?") ? "" : "?") + data;
//                length = data.length();
//            }
//        }
//        HttpURLConnection conn = prepareConnexion(url, method);
//        if (bodyData) {
//            conn.setDoOutput(true);
//            if (parameters != null && parameters.size() > 0) {
//                OutputStream os = conn.getOutputStream();
//                length = writeDataToOutputStream(method, os);
//                os.close();
//            }
//        }
//        InputStream stream = eval(conn, holdError);
//        addToOutputHistoric(length);
//        onQueryComplete();
//        return stream;
//    }

    protected HttpURLConnection prepareConnexion(final String url,
                                                 String method) throws IOException {
        onQueryStarting();
        URL Url = new URL(url);
        URLConnection urlConnexion = Url.openConnection();
        HttpURLConnection conn = (HttpURLConnection) urlConnexion;
        if (mOptions != null) {
            applyOptions(conn);
        }
        fillHeader(conn);
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

    public InputStream doPost(String url) throws IOException {
        return doQuery(url, "POST", true, true);
    }

    public interface ParameterHandler {
        public static ParameterHandler DEFAULT_HANDLER = new ParameterHandler() {

            @Override
            public String onStringifyQueryParams(String method,
                                                 HashMap<String, String> params, String encoding) {
                try {
                    return createStringularQueryableData(params, encoding);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        };

        public abstract String onStringifyQueryParams(String method, HashMap<String, String> params, String encoding);
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

    public InputStream doGet(String url, boolean handleerrror)
            throws IOException {
        // ---------------------------
        String method = "GET";
        return doQuery(url, method, false, true);
    }

    public InputStream doQuery(String url, String method, boolean holdError)
            throws IOException {
        return doQuery(url, method, false, holdError);
    }

    protected final long writeDataToOutputStream(String method,
                                                 OutputStream os) throws IOException {
        currentOutputStream = os;
        long length = onWriteDataToOutputStream(method, currentOutputStream);
        if (length > 0) {
            currentOutputStream.flush();
        }
        return length;
    }

    protected long onWriteDataToOutputStream(String method,
                                             OutputStream dataOutputStream) throws IOException {
        String encoding = getOptions().encoding;
        OutputStreamWriter writer = new OutputStreamWriter(dataOutputStream, encoding);
        String data = "";
        if (parameterHandler != null) {
            data = parameterHandler.onStringifyQueryParams(method, parameters, encoding);
        }
        if (!TextUtils.isEmpty(data)) {
            writer.write(data);
            writer.flush();
            return data.length();
        } else {
            return 0;
        }
    }

    public InputStream doGet(String url) throws IOException {
        return doGet(url, true);
    }

    public InputStream doPut(String url) throws IOException {
        String method = "PUT";
        return doQuery(url, method, true, true);
    }

    public InputStream doHead(String url) throws IOException {
        return doQuery(url, "HEAD");
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

    public InputStream doQuery(String url, String method) throws IOException {
        return doQuery(url, method, true);
    }

    public String getURL(String address) throws IOException {
        // --------------------------
        String paramString = createStringularQueryableData(parameters,
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

    public boolean hasRunningRequest() {
        Log.i("HttpQuery", "hasRunningRequest::currentInputStream=" + currentInputStream);
        Log.i("HttpQuery", "hasRunningRequest::currentOutputStream=" + currentOutputStream);
        return /* querying && */currentConnection != null && (currentInputStream != null || currentOutputStream != null);
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

    public static String createStringularQueryableData(
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
        if (mOptions != null && mOptions.autoClearRequestParams) {
            clearParams();
        }
        currentInputStream = null;
        currentOutputStream = null;
    }

    void addToInputHistoric(long input) {
        historic.get(TAG_INPUT).add(input);
        historics.get(TAG_INPUT).add(input);
        Long elapsed = System.currentTimeMillis() - lastConnectionTime;
        timeHistoric.get(TAG_INPUT).add(elapsed);
        timeHistories.get(TAG_INPUT).add(elapsed);
    }

    void addToOutputHistoric(long input) {
        historic.get(TAG_OUTPUT).add(input);
        historics.get(TAG_OUTPUT).add(input);
        Long elapsed = System.currentTimeMillis() - lastConnectionTime;
        timeHistoric.get(TAG_OUTPUT).add(elapsed);
        timeHistories.get(TAG_OUTPUT).add(elapsed);
    }

    InputStream eval(HttpURLConnection conn) throws IOException {
        return eval(conn, true);
    }

    protected volatile InputStream currentInputStream;
    protected volatile OutputStream currentOutputStream;

    InputStream eval(HttpURLConnection conn, boolean handleError)
            throws IOException {
        int eval = 0;
        InputStream stream = null;
        if (conn != null) {
            eval = conn.getContentLength();
        }

        if (HttpQueryResponse.isSuccessCode(conn.getResponseCode())) {
            stream = conn.getInputStream();
        } else if (handleError) {
            stream = conn.getErrorStream();
        }
        if (stream != null) {
            eval = stream.available();
        }
        currentInputStream = stream;
        addToInputHistoric(eval);
        return stream;
    }

    int getCurrentResponseCode() {
        try {
            if (getCurrentConnection() != null) {
                return getCurrentConnection().getResponseCode();
            } else {
                Log.d("HttpQuery", "getCurrentResponseCode::connexion::"
                        + getCurrentConnection());
            }
        } catch (IOException e) {

        }
        return -1;
    }

    String getCurrentResponseMessage() {
        try {
            if (getCurrentConnection() != null) {
                return getCurrentConnection().getResponseMessage();
            }
        } catch (IOException e) {

        }
        return "";
    }

    public boolean abortRequest() {
        aborted = true;
        boolean out = hasRunningRequest();
        Log.e("HttQuery", "abortRequest_start::runningRequest=" + out);
        if (out) {
            try {
                currentConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            onQueryComplete();
        }
        return out;
    }

    // private void notifyAborted() {
    // onQueryComplete();
    // aborted = true;
    // }

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
        return historics.get(TAG_OUTPUT);
    }

    public static List<Long> getInputContentLegthHistoric() {
        return historics.get(TAG_INPUT);
    }

    public static long getOutputContentLegth() {
        long out = 0;
        for (long i : historics.get(TAG_OUTPUT)) {
            out += i;
        }
        return out;
    }

    public static long getInputContentLegth() {
        long out = 0;
        for (long i : historics.get(TAG_INPUT)) {
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
        boolean out = hasRunningRequest();
        if (getCurrentConnection() != null) {
            getCurrentConnection().disconnect();
        }
        return out;
    }
}
