package istat.android.network.http;

import istat.android.network.util.StreamOperationTools;
import istat.android.network.util.ToolKits.Stream;

import java.io.DataOutputStream;
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

import istat.android.network.http.HttpAsyncQuery.HttpQueryResponse;
import istat.android.network.http.MultipartHttpQuery.UpLoadHandler;

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
    public final static String DEFAULT_ENCODING = "UTF-8";
    HttpQueryCallBack mHttpCallBack;
    CancelListener mCancelListener;
    HttpQuery<?> mHttp;
    int type = TYPE_GET;
    String typeString;
    int bufferSize = DEFAULT_BUFFER_SIZE;
    String encoding = DEFAULT_ENCODING;
    private long startTimeStamp = 0;
    private long endTimeStamp = 0;
    static final ConcurrentHashMap<Object, HttpAsyncQuery> taskQueue = new ConcurrentHashMap<Object, HttpAsyncQuery>();

    private HttpAsyncQuery(HttpQuery<?> http, HttpQueryCallBack callBack) {
        this.mHttpCallBack = callBack;
        this.mHttp = http;
//        Log.e("HttpAsyncQuery", "build:" + callBack.getClass());
//        if (callBack != null && callBack.getClass().isAssignableFrom(CancelListener.class)) {
//            this.setCancelListener((CancelListener) callBack);
//            Log.e("HttpAsyncQuery", "build:instanceofCancellable" + callBack.getClass());
//        }
//        try {
//            Log.e("HttpAsyncQuery", "build:try" + callBack.getClass());
//            CancelListener cancelListener = (CancelListener) callBack;
//            boolean added = this.setCancelListener(cancelListener);
//            Log.e("HttpAsyncQuery", "build:try" + cancelListener + ", cancelAdded=" + added);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
            mHttpCallBack.onHttRequestComplete(response);
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
            ((OnHttpQueryComplete) mHttpCallBack).onHttpAborted();
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

    public static HttpAsyncQuery doAsyncGet(HttpQuery<?> http, String... urls) {
        return doAsyncQuery(http, TYPE_GET, DEFAULT_BUFFER_SIZE,
                http.mOptions.encoding, null, urls);
    }

    public static HttpAsyncQuery doAsyncGet(HttpQuery<?> http,
                                            HttpQueryCallBack callBack, String... urls) {
        return doAsyncQuery(http, TYPE_GET, DEFAULT_BUFFER_SIZE,
                http.mOptions.encoding, callBack, urls);
    }

    public static HttpAsyncQuery doAsyncQuery(HttpQuery<?> http, int queryType,
                                              int bufferSize, String encoding, HttpQueryCallBack callBack,
                                              QueryProcessCallBack<?> processCallBack, String... urls) {
        return doAsyncQuery(http, queryType, bufferSize, encoding, callBack,
                processCallBack, null, urls);
    }

    public static HttpAsyncQuery doAsyncQuery(HttpQuery<?> http, int queryType,
                                              int bufferSize, String encoding, HttpQueryCallBack callBack,
                                              String... urls) {
        return doAsyncQuery(http, queryType, bufferSize, encoding, callBack,
                null, null, urls);
    }

    public static HttpAsyncQuery doAsyncPost(HttpQuery<?> http, String... urls) {
        return doAsyncQuery(http, TYPE_POST, DEFAULT_BUFFER_SIZE,
                http.mOptions.encoding, null, urls);
    }

    public static HttpAsyncQuery doAsyncPost(HttpQuery<?> http,
                                             HttpQueryCallBack callBack, String... urls) {
        return doAsyncQuery(http, TYPE_POST, DEFAULT_BUFFER_SIZE,
                http.mOptions.encoding, callBack, urls);
    }

    public static HttpAsyncQuery doAsyncPost(HttpQuery<?> http,
                                             HttpQueryCallBack callBack,
                                             UploadProcessCallBack<?> uploadCallback, String... urls) {
        HttpAsyncQuery query = doAsyncQuery(http, TYPE_POST,
                DEFAULT_BUFFER_SIZE, http.mOptions.encoding, callBack, urls);
        query.setUploadProcessCallBack(uploadCallback);
        return query;
    }

    public static HttpAsyncQuery doAsyncPost(MultipartHttpQuery http,
                                             int buffersize, String encoding, HttpQueryCallBack callBack,
                                             QueryProcessCallBack<?> processCallBack,
                                             CancelListener cancelCallback,
                                             UploadProcessCallBack<?> uploadCallBack, String... urls) {
        HttpAsyncQuery query = new HttpAsyncQuery(http, callBack);
        query.setProgressCallBack(processCallBack);
        query.setCancelListener(cancelCallback);
        query.setUploadProcessCallBack(uploadCallBack);
        query.type = TYPE_POST;
        query.encoding = encoding;
        query.bufferSize = buffersize;
        query.executeURLs(urls);
        return query;
    }

    boolean setUploadProcessCallBack(UploadProcessCallBack<?> uploadCallBack) {
        if (uploadCallBack != null && mHttp instanceof MultipartHttpQuery) {
            uploadCallBack.query = this;
            MultipartHttpQuery multipartHttp = (MultipartHttpQuery) mHttp;
            multipartHttp.setUploadHandler(uploadCallBack);
            return true;
        }
        return false;
    }

    public static HttpAsyncQuery doAsyncQuery(HttpQuery<?> http, int queryType,
                                              int bufferSize, String encoding, HttpQueryCallBack callBack,
                                              QueryProcessCallBack<?> processCallBack,
                                              CancelListener cancelListener, String... urls) {
        HttpAsyncQuery query = new HttpAsyncQuery(http, callBack);
        query.setProgressCallBack(processCallBack);
        query.setCancelListener(cancelListener);
        query.type = queryType;
        query.encoding = encoding;
        query.bufferSize = bufferSize;
        query.executeURLs(urls);
        return query;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void executeURLs(String... urls) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urls);
        } else {
            execute(urls);
        }
    }

    public boolean isComplete() {
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
    QueryProcessCallBack<?> processCallBack = new QueryProcessCallBack<Integer>() {
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
                return "";
            }
        }

        @Override
        public void onUpdateQueryProcess(HttpAsyncQuery query, Integer... vars) {
            // NOTHIG TO DO
        }

    };

    public boolean setProgressCallBack(QueryProcessCallBack<?> callBack) {
        if (callBack == null || this.processCallBack == callBack) {
            return false;
        }
        this.processCallBack = callBack;
        this.processCallBack.query = this;
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
                          int buffersize, HttpAsyncQuery asyncQ) {
            mAsyncQ = asyncQ;
            init(stream, encoding, buffersize, e);
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
                        this.body = mAsyncQ.processCallBack.buildResponseBody(
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
                this.error = new HttpQueryException(code, message);
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

        public long getHeaderAslong(String name) {
            return getHeaderAslong(name, 0);
        }

        public long getHeaderAslong(String name, long deflt) {
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

        public boolean isAccepeted() {
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

    public static interface HttpQueryCallBack {
        public abstract void onHttRequestComplete(HttpQueryResponse result);
    }

    public static interface CancelListener {
        public abstract void onCancelled(HttpAsyncQuery asyncQ);
    }

    public static interface OnHttpQueryComplete extends HttpQueryCallBack {

        public abstract void onHttpRequestSuccess(HttpQueryResponse result);

        public abstract void onHttpRequestError(HttpQueryResponse result,
                                                HttpQueryException e);

        public abstract void onHttpRequestFail(Exception e);

        public abstract void onHttRequestComplete(HttpQueryResponse result);

        public abstract void onHttpAborted();
    }

    public static abstract class HttpCallBack implements OnHttpQueryComplete/*,
            CancelListener*/ {
        public final void onHttRequestComplete(HttpQueryResponse resp) {
            if (resp.isAccepeted()) {
                if (resp.isSuccess()) {
                    onHttpRequestSuccess(resp);
                } else {
                    onHttpRequestError(resp,
                            new HttpQueryException(resp.getError()));
                }
            } else {
                onHttpRequestFail(resp.getError());
            }
            onHttRequestCompleted(resp);
        }

//        @Override
//        public final void onCancelled(HttpAsyncQuery asyncQ) {
//            onHttpAborted();
//        }

        public abstract void onHttRequestCompleted(HttpQueryResponse result);

    }

    public boolean setCancelListener(CancelListener listener) {
        if (listener != null) {
            this.mCancelListener = listener;
            return true;
        }
        return false;
    }

    public void addTocken(String unikToken) {
        taskQueue.put(unikToken, this);
    }

    public static HttpAsyncQuery getTask(HttpQueryCallBack callback) {
        return taskQueue.get(callback);
    }

    public static HttpAsyncQuery getTask(Object tocken) {
        return taskQueue.get(tocken);
    }

    public static List<HttpAsyncQuery> getTaskqueue() {
        return new ArrayList<HttpAsyncQuery>(taskQueue.values());
    }

    public static void cancelAll() {
        for (HttpAsyncQuery http : getTaskqueue()) {
            http.cancel(true);
        }
    }

    public static void cancel(Object tocken) {
        HttpAsyncQuery http = getTask(tocken);
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

    public static abstract class UploadProcessCallBack<ProgressVar> implements
            UpLoadHandler {
        Handler handler;
        HttpAsyncQuery query;

        public UploadProcessCallBack(Handler handler) {
            this.handler = handler;
        }

        public UploadProcessCallBack() {
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
                onUpdateUploadProcess(query, processVars);
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
        public final void onProceedStreamUpload(MultipartHttpQuery httpQuery,
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
                MultipartHttpQuery httpQuery, OutputStream request,
                InputStream stream, HttpAsyncQuery asyc) throws IOException;

        public abstract void onUpdateUploadProcess(HttpAsyncQuery query,
                                                   ProgressVar... vars);
    }

    public static abstract class QueryProcessCallBack<ProgressVar> {
        Handler handler;
        HttpAsyncQuery query;

        public QueryProcessCallBack(Handler handler) {
            this.handler = handler;
        }

        public QueryProcessCallBack() {
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

        public HttpAsyncQuery getQueryer() {
            return query;
        }

        public HttpQuery<?> getQuery() {
            return getQueryer().mHttp;
        }

        private Handler getHandler() {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            return handler;
        }

        protected void onProcessFail(Exception e) {

        }

        Object buildResponseBody(HttpURLConnection connexion, InputStream stream) {
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
            }
            return null;
        }

        public void publishProgression(final ProgressVar... vars) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    onUpdateQueryProcess(query, vars);
                }
            });
        }

        public abstract Object onBuildResponseBody(HttpURLConnection connexion,
                                                   InputStream stream, HttpAsyncQuery query);

        public abstract void onUpdateQueryProcess(HttpAsyncQuery query,
                                                  ProgressVar... vars);
    }

    private StreamOperationTools.OperationController executionController = new StreamOperationTools.OperationController() {
        @Override
        public boolean isStopped() {
            return !isRunning();
        }
    };


}
