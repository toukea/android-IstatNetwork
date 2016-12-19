package istat.android.network.http;

import istat.android.network.http.interfaces.DownloadHandler;
import istat.android.network.utils.StreamOperationTools;
import istat.android.network.utils.ToolKits.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import istat.android.network.http.HttpAsyncQuery.HttpQueryResponse;
import istat.android.network.http.interfaces.UpLoadHandler;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;


public final class HttpAsyncQuery extends
        AsyncTask<String, HttpQueryResponse, Void> {
    public final static int TYPE_GET = 1, TYPE_POST = 2, TYPE_PUT = 3,
            TYPE_HEAD = 4, TYPE_DELETE = 5, TYPE_COPY = 6, TYPE_PATCH = 7,
            TYPE_RENAME = 8, TYPE_MOVE = 9, DEFAULT_BUFFER_SIZE = 16384;
    //    final static HashMap<Integer, String> METHOD_MAP = new HashMap<Integer, String>() {
//        {
//            put(TYPE_COPY, "COPY");
//            put(TYPE_MOVE, "MOVE");
//            put(TYPE_GET, "GET");
//            put(TYPE_POST, "POST");
//            put(TYPE_PATCH, "PATCH");
//            put(TYPE_HEAD, "HEAD");
//            put(TYPE_PUT, "PUT");
//            put(TYPE_DELETE, "DELETE");
//
//        }
//    };
    public final static String DEFAULT_ENCODING = "UTF-8";
    HttpQueryCallback mHttpCallBack;
    CancelListener mCancelListener;
    HttpQuery<?> mHttp;
    int type = TYPE_GET;
    int bufferSize = DEFAULT_BUFFER_SIZE;
    String encoding = DEFAULT_ENCODING;
    private long startTimeStamp = 0;
    private long endTimeStamp = 0;
    static final ConcurrentHashMap<Object, HttpAsyncQuery> taskQueue = new ConcurrentHashMap<Object, HttpAsyncQuery>();

    HttpAsyncQuery(HttpQuery<?> http) {
        this.mHttp = http;
    }

    private HttpAsyncQuery(HttpQuery<?> http, HttpQueryCallback callBack) {
        this.mHttpCallBack = callBack;
        this.mHttp = http;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // running = true;
        startTimeStamp = System.currentTimeMillis();
        taskQueue.put(mHttpCallBack != null ? mHttpCallBack : Math.random()
                + "", this);
    }

    @Override
    protected Void doInBackground(String... urls) {
        for (String url : urls) {
            if (isCancelled()) {
                break;
            }
            InputStream stream = null;
            Exception error = null;
            Log.d("HttpAsyncQuery", "doInBackground::type=" + type);
            try {
                switch (type) {
                    case TYPE_GET:
                        stream = mHttp.doGet(url);
                        break;
                    case TYPE_POST:
                        stream = mHttp.doPost(url);
                        break;
                    case TYPE_PUT:
                        stream = mHttp.doPut(url);
                        break;
                    case TYPE_HEAD:
                        stream = mHttp.doHead(url);
                        break;
                    case TYPE_COPY:
                        stream = mHttp.doCopy(url);
                        break;
                    case TYPE_RENAME:
                        stream = mHttp.doQuery(url, "RENAME");
                        break;
                    case TYPE_MOVE:
                        stream = mHttp.doQuery(url, "MOVE", true, true);
                        break;
                    case TYPE_DELETE:
                        stream = mHttp.doDelete(url);
                        break;
                    case TYPE_PATCH:
                        stream = mHttp.doPatch(url);
                        break;
                    default:
                        stream = mHttp.doGet(url);
                        break;
                }
            } catch (Exception e) {
                error = e;
                e.printStackTrace();
            }
            HttpQueryResponse response = new HttpQueryResponse(stream, error,
                    encoding, bufferSize, this);
            if (!mHttp.isAborted() && !isCancelled()) {
                Log.i("HttpAsycQ", "doInBackground::publish_response");
                publishProgress(response);
            } else {
                Log.i("HttpAsycQ", "doInBackground::was aborded");
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(HttpQueryResponse... values) {
        HttpQueryResponse response = values.length > 0 ? values[0] : null;
        if (mHttpCallBack != null && !mHttp.isAborted() && !isCancelled()) {
            dispatchQueryResponse(response);
        }
    }

    private void dispatchQueryResponse(HttpQueryResponse resp) {
        try {
            if (resp.isAccepted()) {
                if (resp.isSuccess()) {
                    mHttpCallBack.onHttpRequestSuccess(resp);
                } else {
                    HttpQueryError error = new HttpQueryError(resp.getError());
                    resp.error = error;
                    mHttpCallBack.onHttpRequestError(resp, error);
                }
            } else {
                mHttpCallBack.onHttpRequestFail(resp.getError());
            }
            mHttpCallBack.onHttRequestComplete(resp);
        } catch (Exception e) {
            mHttpCallBack.onHttpRequestFail(e);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        taskQueue.values().removeAll(Collections.singletonList(this));
    }

    @Override
    protected void onCancelled() {
        Log.i("HttpAsyncQuery", "onCancelled::canceListener::" + mCancelListener);
        Log.i("HttpAsyncQuery", "onCancelled::httpCallback::" + mHttpCallBack);
        try {
            // if (mHttpCallBack != null && mHttpCallBack instanceof OnHttpQueryComplete) {
            mHttpCallBack.onHttpAborted();
            Log.i("HttpAsyncQuery", "onCancelled::abort-callaed::" + mHttpCallBack);
            //
        } catch (Exception e) {
            // e.printStackTrace();
        }
        endTimeStamp = System.currentTimeMillis();
        if (mCancelListener != null) {
            mCancelListener.onCancelled(this);
        }
        taskQueue.values().removeAll(Collections.singletonList(this));
        super.onCancelled();
    }

    private Runnable httpAbortRunnable = new Runnable() {
        public void run() {
            mHttp.abortRequest();
        }
    };

    public static HttpAsyncQuery doAsyncGet(HttpQuery<?> http, String url) {
        return doAsyncQuery(http, TYPE_GET, DEFAULT_BUFFER_SIZE,
                http.mOptions.encoding, null, url);
    }

    public static HttpAsyncQuery doAsyncGet(HttpQuery<?> http,
                                            HttpQueryCallback callBack, String url) {
        return doAsyncQuery(http, TYPE_GET, DEFAULT_BUFFER_SIZE,
                http.mOptions.encoding, callBack, url);
    }

    public static HttpAsyncQuery doAsyncQuery(HttpQuery<?> http, int queryType,
                                              int bufferSize, String encoding, HttpQueryCallback callBack,
                                              HttpDownloadHandler<?> processCallBack, String url) {
        return doAsyncQuery(http, queryType, bufferSize, encoding, callBack,
                processCallBack, null, url);
    }

    public static HttpAsyncQuery doAsyncQuery(HttpQuery<?> http, int queryType,
                                              int bufferSize, String encoding, HttpQueryCallback callBack,
                                              String url) {
        return doAsyncQuery(http, queryType, bufferSize, encoding, callBack,
                null, null, url);
    }

    public static HttpAsyncQuery doAsyncPost(HttpQuery<?> http, String url) {
        return doAsyncQuery(http, TYPE_POST, DEFAULT_BUFFER_SIZE,
                http.mOptions.encoding, null, url);
    }

    public static HttpAsyncQuery doAsyncPost(HttpQuery<?> http,
                                             HttpQueryCallback callBack, String url) {
        return doAsyncQuery(http, TYPE_POST, DEFAULT_BUFFER_SIZE,
                http.mOptions.encoding, callBack, url);
    }

    public static HttpAsyncQuery doAsyncPost(HttpQuery<?> http,
                                             HttpQueryCallback callBack,
                                             HttpUploadHandler<?> uploadCallback, String url) {
        HttpAsyncQuery query = doAsyncQuery(http, TYPE_POST,
                DEFAULT_BUFFER_SIZE, http.mOptions.encoding, callBack, url);
        query.setUploadHandler(uploadCallback);
        return query;
    }

    public static HttpAsyncQuery doAsyncPost(MultipartHttpQuery http,
                                             int bufferSize, String encoding, HttpQueryCallback callBack,
                                             HttpDownloadHandler<?> processCallBack,
                                             CancelListener cancelCallback,
                                             HttpUploadHandler<?> uploadCallBack, String... urls) {
        HttpAsyncQuery query = new HttpAsyncQuery(http, callBack);
        query.setDownloadHandler(processCallBack);
        query.setCancelListener(cancelCallback);
        query.setUploadHandler(uploadCallBack);
        query.type = TYPE_POST;
        query.encoding = encoding;
        query.bufferSize = bufferSize;
        query.executeURLs(urls);
        return query;
    }

    boolean setUploadHandler(HttpUploadHandler<?> uploader) {
        if (uploader != null && mHttp instanceof MultipartHttpQuery) {
            uploader.query = this;
            MultipartHttpQuery multipartHttp = (MultipartHttpQuery) mHttp;
            multipartHttp.setUploadHandler(uploader);
            return true;
        }
        return false;
    }

    public static HttpAsyncQuery doAsyncQuery(HttpQuery<?> http, int queryType,
                                              int bufferSize, String encoding, HttpQueryCallback callBack,
                                              HttpDownloadHandler<?> processCallBack,
                                              CancelListener cancelListener, String... urls) {
        HttpAsyncQuery query = new HttpAsyncQuery(http, callBack);
        query.setDownloadHandler(processCallBack);
        query.setCancelListener(cancelListener);
        query.type = queryType;
        query.encoding = encoding;
        query.bufferSize = bufferSize;
        query.executeURLs(urls);
        return query;
    }

    Executor mExecutor;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void executeURLs(String... urls) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mExecutor == null) {
                mExecutor = AsyncTask.THREAD_POOL_EXECUTOR;
            }
            executeOnExecutor(mExecutor, urls);
        } else {
            execute(urls);
        }
    }

    public boolean isCompleted() {
        if (isCancelled())
            return false;
        return this.getStatus().equals(AsyncTask.Status.FINISHED);
    }

    public boolean isRunning() {
        return this.getStatus().equals(Status.RUNNING) || (mHttp.hasRunningRequest() && !this.getStatus().equals(Status.FINISHED));
    }

    public boolean isPending() {
        return this.getStatus().equals(Status.PENDING);
    }

    public long getExecutionTime() {
        if (endTimeStamp <= startTimeStamp)
            return getDuration();
        return endTimeStamp - startTimeStamp;
    }

    public final boolean cancel() {
        Log.i("HttAsyncQuery", "cancel_start, running=" + mHttp.hasRunningRequest() + ", aborted=" + mHttp.isAborted());
        if (mHttp.hasRunningRequest()) {
            Log.i("HttQuery", "cancel_has_running");
            new Thread(httpAbortRunnable).start();
        }
        return cancel(true);
    }

    private long getDuration() {
        return System.currentTimeMillis() - startTimeStamp;
    }

    // DEFAULT PROCESS CALLBACK IF USER DON'T HAS DEFINE it Own
    HttpDownloadHandler<?> downloadHandler = new HttpDownloadHandler<Integer>() {
        {
            this.query = HttpAsyncQuery.this;
        }

        @Override
        public String onBuildResponseBody(HttpURLConnection currentConnexion,
                                          InputStream stream, HttpAsyncQuery query) {
            try {
                return StreamOperationTools.streamToString(executionController,
                        stream, bufferSize, encoding);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        public void onDownloadProgress(HttpAsyncQuery query, Integer... vars) {
            // NOTHIG TO DO
        }

    };

    public boolean setDownloadHandler(final DownloadHandler downloader) {
        HttpAsyncQuery.HttpDownloadHandler<Integer> downloadHandler = new HttpAsyncQuery.HttpDownloadHandler<Integer>() {
            @Override
            public void onDownloadProgress(HttpAsyncQuery query, Integer... integers) {

            }

            @Override
            public Object onBuildResponseBody(HttpURLConnection connexion, InputStream stream, HttpAsyncQuery query) throws Exception {
                return downloader.onBuildResponseBody(connexion, stream, query);
            }

            @Override
            protected void onProcessFail(Exception e) {
                throw new RuntimeException(e);
            }
        };
        return setDownloadHandler(downloadHandler);
    }

    public boolean setDownloadHandler(HttpDownloadHandler<?> downloader) {
        if (downloader == null || this.downloadHandler == downloader) {
            return false;
        }
        this.downloadHandler = downloader;
        this.downloadHandler.query = this;
        return true;
    }

    public boolean isPaused() {
        return executionController.isPaused();
    }

    public void resume() {
        executionController.resume();
    }

    public void pause() {
        executionController.pause();
    }

    public static class HttpQueryResponse {
        Object body;
        Exception error;
        HttpAsyncQuery mAsyncQ;
        int code = -1;
        String message;
        HttpURLConnection connexion;
        Map<String, List<String>> headers = new HashMap<String, List<String>>();

        public static HttpQueryResponse getErrorInstance(Exception e) {
            return new HttpQueryResponse(null, e, null);
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        HttpQueryResponse(InputStream stream, Exception e, HttpAsyncQuery asyncQ) {
            mAsyncQ = asyncQ;
            init(stream, Stream.DEFAULT_ENCODING, Stream.DEFAULT_BUFFER_SIZE, e);
        }

        HttpQueryResponse(InputStream stream, Exception e, String encoding,
                          int bufferSize, HttpAsyncQuery asyncQ) {
            mAsyncQ = asyncQ;
            init(stream, encoding, bufferSize, e);
        }

        private void init(InputStream stream, String encoding, int buffersize,
                          Exception e) {
            HttpQuery<?> http = mAsyncQ.mHttp;
            connexion = http.getCurrentConnection();
            this.error = e;
            this.code = http.getCurrentResponseCode();
            this.message = http.getCurrentResponseMessage();
            if (connexion != null) {
                try {
                    this.body = null;
                    if (stream != null) {
                        this.body = mAsyncQ.downloadHandler.buildResponseBody(
                                connexion, stream);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    code = 0;
                    this.error = ex;
                }
                this.headers = connexion.getHeaderFields();
            }
            if (e == null && !isSuccess() && !TextUtils.isEmpty(message)
                    && code > 0) {
                this.error = new istat.android.network.http.HttpQueryError(code, message, body);
            }
        }

        public boolean containHeader(String name) {
            return getHeaders() != null && getHeaders().containsKey(name);
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public String getHeader(String name) {
            if (connexion != null) {
                return connexion.getHeaderField(name);
            }
            return "";
        }

        public long getHeaderAsLong(String name) {
            return getHeaderAsLong(name, 0);
        }

        public long getHeaderAsLong(String name, long deflt) {
            if (connexion != null) {
                return connexion.getHeaderFieldDate(name, deflt);
            }
            return deflt;
        }

        public int getHeaderAsInt(String name) {
            return getHeaderAsInt(name, 0);
        }

        public int getHeaderAsInt(String name, int deflt) {
            if (connexion != null) {
                return connexion.getHeaderFieldInt(name, deflt);
            }
            return deflt;
        }

        public boolean hasError() {
            // Log.d("HttpAsyc", "haserror:" + error + ", code=" + code);
            // if (error != null) {
            // error.printStackTrace();
            // }
            return error != null || !isSuccessCode(code);
        }

        public boolean isSuccess() {
            return !hasError();
        }

        public boolean isAccepted() {
            return code > 0;
        }

        @SuppressWarnings("unchecked")
        public <T> T getBody() {
            if (body == null) {
                return null;
            }
            try {
                return (T) body;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public <T> T optBody() {
            if (body == null) {
                return null;
            }
            try {
                return (T) body;
            } catch (Exception e) {
                return null;
            }
        }

        public String getBodyAsString() {
            if (body == null)
                return null;
            return body.toString();
        }

        public Exception getError() {
            return error;
        }

        public static boolean isSuccessCode(int code) {
            return code > 0 && code >= 200 && code <= 299;
        }

        public static boolean isErrorCode(int code) {
            return !isSuccessCode(code);
        }

        public static boolean isClientErrorCode(int code) {
            return code > 0 && code >= 400 && code <= 499;
        }

        public static boolean isServerErrorCode(int code) {
            return code > 0 && code >= 500 && code <= 599;
        }
    }


    public static interface CancelListener {
        public abstract void onCancelled(HttpAsyncQuery asyncQ);
    }

    public static interface HttpQueryCallback {
        abstract void onHttpRequestSuccess(HttpQueryResponse result);

        abstract void onHttpRequestError(HttpQueryResponse result,
                                         istat.android.network.http.HttpQueryError e);

        abstract void onHttpRequestFail(Exception e);

        abstract void onHttRequestComplete(HttpQueryResponse result);

        abstract void onHttpAborted();
    }

    public boolean setCancelListener(CancelListener listener) {
        if (listener != null) {
            this.mCancelListener = listener;
            return true;
        }
        return false;
    }

    public void addTocken(String uniqueToken) {
        taskQueue.put(uniqueToken, this);
    }

    public static HttpAsyncQuery getTask(HttpQueryCallback callback) {
        return taskQueue.get(callback);
    }

    public static HttpAsyncQuery getTask(Object token) {
        return taskQueue.get(token);
    }

    public static List<HttpAsyncQuery> getTaskQueue() {
        return new ArrayList<HttpAsyncQuery>(taskQueue.values());
    }

    public static void cancelAll() {
        for (HttpAsyncQuery http : getTaskQueue()) {
            http.cancel(true);
        }
    }

    public static void cancel(Object token) {
        HttpAsyncQuery http = getTask(token);
        if (http != null) {
            http.cancel(true);
        }
    }

    <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static abstract class HttpUploadHandler<ProgressVar> implements
            UpLoadHandler {
        Handler handler;
        HttpAsyncQuery query;

        public HttpUploadHandler(Handler handler) {
            this.handler = handler;
        }

        public HttpUploadHandler() {
            try {
                this.handler = getHandler();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Handler getHandler() {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            return handler;
        }

        protected void onProcessFail(Exception e) {

        }

        Runnable publishRunner = new Runnable() {
            @Override
            public void run() {
                onUploadProgress(query, processVars);
            }
        };
        ProgressVar[] processVars;

        public void publishProgression(final ProgressVar... vars) {
            Handler tmpHandler = getHandler();
            if (tmpHandler != null) {
                processVars = vars;
                tmpHandler.post(publishRunner);
            }
        }

        @Override
        public final void onProceedStreamUpload(HttpQuery httpQuery,
                                                OutputStream request, InputStream stream)
                throws IOException {
            try {
                onProceedStreamUpload(httpQuery, request, stream, query);
            } catch (final Exception e) {
                e.printStackTrace();
                Handler tmpHandler = getHandler();
                if (tmpHandler != null) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            onProcessFail(e);
                        }
                    });
                }
            }
        }

        public abstract void onProceedStreamUpload(
                HttpQuery httpQuery, OutputStream request,
                InputStream stream, HttpAsyncQuery asyc) throws IOException;

        public abstract void onUploadProgress(HttpAsyncQuery query,
                                              ProgressVar... vars);
    }

    public static abstract class HttpDownloadHandler<ProgressVar> implements DownloadHandler {
        Handler handler;
        HttpAsyncQuery query;

        public HttpDownloadHandler(Handler handler) {
            this.handler = handler;
        }

        public HttpDownloadHandler() {
            try {
                handler = getHandler();
            } catch (Exception e) {

            }
        }

        public int getConnetionContentLenght() {
            return query != null && query.mHttp != null
                    && query.mHttp.currentConnection != null ? query.mHttp.currentConnection
                    .getContentLength() : 0;
        }

        public String getConnetionContentType() {
            return query != null && query.mHttp != null
                    && query.mHttp.currentConnection != null ? query.mHttp.currentConnection
                    .getContentType() : null;
        }

        public HttpAsyncQuery getAsyncQuery() {
            return query;
        }

        public HttpQuery<?> getQuery() {
            return getAsyncQuery().mHttp;
        }

        private Handler getHandler() {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            return handler;
        }

        protected void onProcessFail(Exception e) {

        }

        Object buildResponseBody(HttpURLConnection connexion, InputStream stream) throws Exception {
            try {
                return onBuildResponseBody(connexion, stream, query);
            } catch (final Exception e) {
                e.printStackTrace();
                Handler tmpHandler = getHandler();
                if (tmpHandler != null) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            onProcessFail(e);
                        }
                    });
                }
                throw new RuntimeException(e);
            }
        }

        public void publishProgression(final ProgressVar... vars) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    onDownloadProgress(query, vars);
                }
            });
        }


        public abstract void onDownloadProgress(HttpAsyncQuery query,
                                                ProgressVar... vars);
    }

    public final StreamOperationTools.OperationController executionController = new StreamOperationTools.OperationController() {
        @Override
        public boolean isStopped() {
            return !isRunning();
        }
    };

    /**
     * use {@link AsyncHttp#from(HttpQuery)} instead.
     *
     * @param http
     * @return
     */
    @Deprecated
    public final static AsyncHttp from(HttpQuery<?> http) {
        return new AsyncHttp(new HttpAsyncQuery(http));
    }

    /**
     * * use {@link AsyncHttp#fromDefaultHttp()} instead.
     *
     * @return
     */
    @Deprecated
    public final static AsyncHttp fromDefaultHttp() {
        SimpleHttpQuery http = new SimpleHttpQuery();
        return new AsyncHttp(new HttpAsyncQuery(http));
    }

    /**
     * * use {@link AsyncHttp#fromMultipartHttp()} instead.
     *
     * @return
     */
    @Deprecated
    public final static AsyncHttp fromMultipartHttp() {
        MultipartHttpQuery http = new MultipartHttpQuery();
        return new AsyncHttp(new HttpAsyncQuery(http));
    }

    public class HttpPromise {
        public final static int WHEN_ANAWAY = 0;
        public final static int WHEN_SUCCED = 0;
        public final static int WHEN_ERROR = 0;
        public final static int WHEN_FAILED = 0;

        public HttpPromise runWhen(Runnable runnable, int... when) {
            return this;
        }
    }

    public boolean dismissCallback() {
        boolean dismiss = mHttpCallBack != null;
        mHttpCallBack = null;
        return dismiss;
    }

}
