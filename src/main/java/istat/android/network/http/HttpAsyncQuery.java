package istat.android.network.http;

import istat.android.network.http.interfaces.DownloadHandler;
import istat.android.network.http.interfaces.ProgressListener;
import istat.android.network.utils.StreamOperationTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import istat.android.network.http.interfaces.UpLoadHandler;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


public final class HttpAsyncQuery extends
        AsyncTask<String, HttpQueryResponse, HttpQueryResponse> {
    public final static int TYPE_GET = 1, TYPE_POST = 2, TYPE_PUT = 3,
            TYPE_HEAD = 4, TYPE_DELETE = 5, TYPE_COPY = 6, TYPE_PATCH = 7,
            TYPE_RENAME = 8, TYPE_MOVE = 9, DEFAULT_BUFFER_SIZE = 16384;
    public final static HashMap<Integer, String> METHOD_TYPE_NAME_MAP = new HashMap<Integer, String>() {
        {
            put(TYPE_COPY, "COPY");
            put(TYPE_MOVE, "MOVE");
            put(TYPE_GET, "GET");
            put(TYPE_POST, "POST");
            put(TYPE_PATCH, "PATCH");
            put(TYPE_HEAD, "HEAD");
            put(TYPE_PUT, "PUT");
            put(TYPE_DELETE, "DELETE");

        }
    };
    public final static HashMap<String, Integer> METHOD_NAME_TYPE_MAP = new HashMap() {
        {
            put("COPY", TYPE_COPY);
            put("MOVE", TYPE_MOVE);
            put("GET", TYPE_GET);
            put("POST", TYPE_POST);
            put("PATCH", TYPE_PATCH);
            put("HEAD", TYPE_HEAD);
            put("PUT", TYPE_PUT);
            put("DELETE", TYPE_DELETE);

        }
    };
    public final static String DEFAULT_ENCODING = "UTF-8";
    UpLoadHandler uploadHandler;
    Callback mHttpCallBack;
    CancelListener mCancelListener;
    final HttpQuery<?> mHttp;
    int type = TYPE_GET;
    int bufferSize = DEFAULT_BUFFER_SIZE;
    String encoding = DEFAULT_ENCODING;
    private long startTimeStamp = 0;
    private long endTimeStamp = 0;
    static final ConcurrentHashMap<Object, HttpAsyncQuery> taskQueue = new ConcurrentHashMap<Object, HttpAsyncQuery>();
    Executor mExecutor;

    HttpAsyncQuery(HttpQuery<?> http) {
        this.mHttp = http;
    }

    private HttpAsyncQuery(HttpQuery<?> http, Callback callBack) {
        this.mHttpCallBack = callBack;
        this.mHttp = http;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        notifyStarting();
        startTimeStamp = System.currentTimeMillis();
        this.id = createQueryId();
        taskQueue.put(this.id, this);
    }

    @Override
    protected HttpQueryResponse doInBackground(String... urls) {
        for (String url : urls) {
            if (isCancelled()) {
                break;
            }
            HttpQueryResponse httpQueryResponse = null;
            Log.d("HttpAsyncQuery", "doInBackground::type=" + type);
            try {
                switch (type) {
                    case TYPE_GET:
                        httpQueryResponse = mHttp.doGet(url);
                        break;
                    case TYPE_POST:
                        httpQueryResponse = mHttp.doPost(url);
                        break;
                    case TYPE_PUT:
                        httpQueryResponse = mHttp.doPut(url);
                        break;
                    case TYPE_HEAD:
                        httpQueryResponse = mHttp.doHead(url);
                        break;
                    case TYPE_COPY:
                        httpQueryResponse = mHttp.doCopy(url);
                        break;
                    case TYPE_RENAME:
                        httpQueryResponse = mHttp.doQuery(url, "RENAME");
                        break;
                    case TYPE_MOVE:
                        httpQueryResponse = mHttp.doQuery(url, "MOVE", true, true);
                        break;
                    case TYPE_DELETE:
                        httpQueryResponse = mHttp.doDelete(url);
                        break;
                    case TYPE_PATCH:
                        httpQueryResponse = mHttp.doPatch(url);
                        break;
                    default:
                        httpQueryResponse = mHttp.doGet(url);
                        break;
                }
                return httpQueryResponse;
            } catch (HttpQuery.AbortionException e) {
                e.printStackTrace();
                Log.i("HttpAsycQ", "doInBackground::was aborded");
            } catch (Exception e) {
                e.printStackTrace();
                HttpQueryResponseImpl errorResponse = HttpQueryResponseImpl.newErrorInstance(e);
                return errorResponse;
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(HttpQueryResponse... values) {

    }

    @Override
    protected void onPostExecute(HttpQueryResponse result) {
        super.onPostExecute(result);
        if (result == null) {
            return;
        }
        if (!mHttp.isAborted() && !isCancelled()) {
            dispatchQueryResponse(result);
        }
        taskQueue.values().removeAll(Collections.singletonList(this));
    }

    public HttpQueryResponse getResult() throws IllegalAccessException {
        if (result != null) {
            return result;
        }
        if (!isPending()) {
            throw new IllegalAccessException("Current httpAsyncQuery can't has result for now. it is still pending.");
        }
        throw new IllegalAccessException("Current httpAsyncQuery has not response for now.");
    }

    HttpQueryResponse result;

    private void dispatchQueryResponse(HttpQueryResponse resp) {
        this.result = resp;
        executedRunnable.clear();
        try {
            boolean aborted = isAborted();
            if (resp.isAccepted() && !aborted) {
                if (resp.isSuccess()) {
                    HttpQueryResult result = new HttpQueryResult(resp);
                    this.result = result;
                    notifySuccess(result);
                } else {
                    HttpQueryError error = new HttpQueryError(resp);
                    this.result = error;
                    notifyError(error);
                }
            } else {
                throw resp.getError() instanceof Exception ?
                        (Exception) resp.getError() :
                        new Exception(resp.getError());
            }
        } catch (Exception e) {
            notifyFail(HttpQueryResponseImpl.newErrorInstance(e));
        }
    }

    private void notifyStarting() {
        int when = WHEN_BEGIN;
        ConcurrentLinkedQueue<Runnable> runnableList = runnableTask.get(when);
        executeWhen(runnableList, when);
    }

    private void notifySuccess(HttpQueryResult result) {
        int when = WHEN_SUCCEED;
        if (mHttpCallBack != null) {
            mHttpCallBack.onHttpSuccess(result);
        }
        notifyCompleted(result);
        ConcurrentLinkedQueue<Runnable> runnableList = runnableTask.get(when);
        executeWhen(runnableList, when);
    }

    private void notifyError(HttpQueryError error) {
        int when = WHEN_ERROR;
        if (mHttpCallBack != null) {
            mHttpCallBack.onHttpError(error);
        }
        notifyCompleted(error);
        ConcurrentLinkedQueue<Runnable> runnableList = runnableTask.get(when);
        executeWhen(runnableList, when);
    }

    private void notifyCompleted(HttpQueryResponse resp) {
        int when = WHEN_ANYWAY;
        if (mHttpCallBack != null) {
            mHttpCallBack.onHttComplete(resp);
        }
        ConcurrentLinkedQueue<Runnable> runnableList = runnableTask.get(when);
        executeWhen(runnableList, when);
    }

    private void notifyFail(HttpQueryResponseImpl resp) {
        int when = WHEN_FAILED;
        if (mHttpCallBack != null) {
            mHttpCallBack.onHttpFailure(resp.getError());
        }
        notifyCompleted(resp);
        ConcurrentLinkedQueue<Runnable> runnableList = runnableTask.get(when);
        executeWhen(runnableList, when);
    }

    private void notifyAborted() {
        int when = WHEN_ABORTION;
        if (mHttpCallBack != null) {
            mHttpCallBack.onHttpAborted();
        }

        if (mCancelListener != null) {
            mCancelListener.onCanceling(this);
        }
        result = HttpQueryResponseImpl.newErrorInstance(new HttpQuery.AbortionException(this.mHttp));
        ConcurrentLinkedQueue<Runnable> runnableList = runnableTask.get(when);
        ConcurrentLinkedQueue<Runnable> runnableAnywayList = runnableTask.get(WHEN_ANYWAY);
        if (runnableList != null && runnableAnywayList != null) {
            runnableList.addAll(runnableAnywayList);
        } else {
            runnableList = runnableAnywayList;
        }
        executeWhen(runnableList, when);
    }

    public boolean isAborted() {
        return isCancelled() || mHttp.isAborted();
    }

    @Override
    protected void onCancelled() {
        Log.i("HttpAsyncQuery", "onCancelled::canceListener::" + mCancelListener);
        Log.i("HttpAsyncQuery", "onCancelled::httpCallback::" + mHttpCallBack);
        notifyAborted();
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
            HttpAsyncQuery.super.cancel(true);
        }
    };

    boolean prepareQuery() {
        if (uploadHandler != null) {
            mHttp.setUploadHandler(uploadHandler);
            if (uploadHandler instanceof UploadHandler) {
                ((UploadHandler) uploadHandler).query = this;
            }
            return true;
        }
        return false;
    }

    void setUploadHandler(UpLoadHandler uploader) {
        this.uploadHandler = uploader;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void executeURLs(String... urls) {
        prepareQuery();
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
        return this.getStatus().equals(Status.PENDING) || mHttp.hasPendingRequest();
    }

    public long getExecutionTime() {
        if (endTimeStamp <= startTimeStamp)
            return getDuration();
        return endTimeStamp - startTimeStamp;
    }

    public final boolean cancel() {
        Log.i("HttAsyncQuery", "cancel_start, running=" + mHttp.hasRunningRequest() + ", pending=\" + mHttp.hasPendingRequest() +, aborted=" + mHttp.isAborted());
        if (mHttp.hasPendingRequest()) {
            Log.i("HttQuery", "cancel_has_running");
            new Thread(httpAbortRunnable).start();
            return true;
        } else {
            return cancel(true);
        }
    }

    private long getDuration() {
        return System.currentTimeMillis() - startTimeStamp;
    }

    // DEFAULT PROCESS CALLBACK IF USER DON'T HAS DEFINE it Own
    HttpDownloadHandler defaultDownloader = getDefaultDownloader();
    HttpDownloadHandler successDownloader = null;
    HttpDownloadHandler errorDownloader = null;

    HttpAsyncQuery setDownloadHandler(final DownloadHandler downloader, DownloadHandler.WHEN when) {
        setDownloadHandler(downloader, null, when);
        return this;
    }

    HttpAsyncQuery setDownloadHandler(final DownloadHandler downloader, final ProgressListener progressListener, DownloadHandler.WHEN when) {
        HttpAsyncQuery.HttpDownloadHandler downloadHandler = new HttpAsyncQuery.HttpDownloadHandler() {
            @Override
            public void onProgress(HttpAsyncQuery query, long... integers) {
                if (progressListener != null) {
                    progressListener.onProgress(query, integers);
                }
            }

            @Override
            public Object onBuildResponseBody(HttpURLConnection connexion, InputStream stream) throws Exception {
                DownloadHandler handler = downloader != null ? downloader : getDefaultDownloader();
                return handler.onBuildResponseBody(connexion, stream);
            }

        };
        return setDownloadHandler(downloadHandler, when);
    }

    HttpAsyncQuery setDownloadHandler(HttpDownloadHandler downloader, DownloadHandler.WHEN when) {
        if (downloader == null) {
            downloader = getDefaultDownloader();
        }
        downloader.httpAsyncQuery = this;
        if (when == DownloadHandler.WHEN.SUCCESS) {
            this.successDownloader = downloader;
        } else if (when == DownloadHandler.WHEN.ERROR) {
            this.errorDownloader = downloader;
        } else {
            this.defaultDownloader = downloader;
        }
        return this;
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

    public boolean isSuccess() {
        try {
            HttpQueryResponse result = getResult();
            return result != null && result.isSuccess();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    HttpDownloadHandler getDefaultDownloader() {
        HttpDownloadHandler downloadHandler = new HttpDownloadHandler() {

            @Override
            public String onBuildResponseBody(HttpURLConnection currentConnexion,
                                              InputStream stream) throws IOException {
                return StreamOperationTools.streamToString(executionController,
                        stream, bufferSize, encoding);
            }

            @Override
            public void onProgress(HttpAsyncQuery query, long... vars) {
                // NOTHING TO DO
            }

        };
        downloadHandler.httpAsyncQuery = this;
        return downloadHandler;
    }


    public interface CancelListener {
        void onCanceling(HttpAsyncQuery asyncQ);

        void onCancelled(HttpAsyncQuery asyncQ);
    }

    public interface Callback {
        void onHttpSuccess(HttpQueryResult resp);

        void onHttpError(HttpQueryError e);

        void onHttpFailure(Exception e);

        void onHttComplete(HttpQueryResponse resp);

        void onHttpAborted();
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

    public static HttpAsyncQuery getTask(Callback callback) {
        return taskQueue.get(callback);
    }

    public static HttpAsyncQuery getTask(Object token) {
        return taskQueue.get(token);
    }

    public static List<HttpAsyncQuery> getTaskQueue() {
        return new ArrayList(taskQueue.values());
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

    public static abstract class UploadHandler implements
            UpLoadHandler, ProgressListener {
        Handler handler;
        HttpAsyncQuery query;

        public UploadHandler(Handler handler) {
            if (handler == null) {
                try {
                    this.handler = getHandler();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                this.handler = handler;
            }
        }

        public UploadHandler() {
            this(null);
        }

        private Handler getHandler() {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            return handler;
        }

        void notifyProcessFail(final Exception e) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    onFail(e);
                }
            });
            throw new RuntimeException(e);
        }

        protected void onFail(Exception e) {

        }

        Runnable publishRunner = new Runnable() {
            @Override
            public void run() {
                onProgress(query, processVars);
            }
        };
        long[] processVars;

        public void publishProgression(final long... vars) {
            Handler tmpHandler = getHandler();
            if (tmpHandler != null) {
                processVars = vars;
                tmpHandler.post(publishRunner);
            }
        }

        @Override
        public final void onUploadStream(long uploadSize, InputStream stream, OutputStream request)
                throws IOException {
            try {
                onProceedStreamUpload(uploadSize, stream, request, query);
            } catch (final Exception e) {
                e.printStackTrace();
                Handler tmpHandler = getHandler();
                if (tmpHandler != null) {
                    notifyProcessFail(e);
                }
            }
        }

        public abstract void onProceedStreamUpload(long uploadSize, InputStream stream, OutputStream request,
                                                   HttpAsyncQuery asyc) throws IOException;

        public abstract void onProgress(HttpAsyncQuery query,
                                        long... vars);
    }

    public static abstract class HttpDownloadHandler implements DownloadHandler<Object>, ProgressListener {
        Handler handler;
        HttpAsyncQuery httpAsyncQuery;

        public HttpDownloadHandler(Handler handler) {
            this.handler = handler;
        }

        public HttpDownloadHandler() {
            try {
                handler = getHandler();
            } catch (Exception e) {

            }
        }

        void setHandler(Handler handler) {
            this.handler = handler;
        }

        void setHttpAsyncQuery(HttpAsyncQuery httpAsyncQuery) {
            this.httpAsyncQuery = httpAsyncQuery;
        }

        public int getConnetionContentLenght() {
            return httpAsyncQuery != null && httpAsyncQuery.mHttp != null
                    && httpAsyncQuery.mHttp.currentConnection != null ? httpAsyncQuery.mHttp.currentConnection
                    .getContentLength() : 0;
        }

        public String getConnetionContentType() {
            return httpAsyncQuery != null && httpAsyncQuery.mHttp != null
                    && httpAsyncQuery.mHttp.currentConnection != null ? httpAsyncQuery.mHttp.currentConnection
                    .getContentType() : null;
        }

        public HttpAsyncQuery getAsyncQuery() {
            return httpAsyncQuery;
        }

        public HttpQuery<?> getHttpAsyncQuery() {
            return getAsyncQuery().mHttp;
        }

        private Handler getHandler() {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            return handler;
        }

        void notifyProcessFail(final Exception e) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    onFail(e);
                }
            });
            throw new RuntimeException(e);
        }

        /**
         * @param e
         * @return whether or not this method should rethrow the Exception.
         */
        protected void onFail(Exception e) {

        }

        Object buildResponseBody(HttpURLConnection connexion, InputStream stream) throws Exception {
            try {
                return onBuildResponseBody(connexion, stream);
            } catch (final Exception e) {
                e.printStackTrace();
                Handler tmpHandler = getHandler();
                if (tmpHandler != null) {
                    notifyProcessFail(e);
                }
                throw new RuntimeException(e);
            }
        }

        public void publishProgression(final long... vars) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    onProgress(httpAsyncQuery, vars);
                }
            });
        }


        public abstract void onProgress(HttpAsyncQuery query,
                                        long... vars);

    }

    public final StreamOperationTools.OperationController executionController = new StreamOperationTools.OperationController() {
        @Override
        public boolean isStopped() {
            return !isRunning();
        }
    };

    public HttpPromise then(Runnable runnable) {
        HttpPromise promise = new HttpPromise(this);
        promise.then(runnable);
        return promise;
    }

    public HttpPromise then(PromiseCallback callback) {
        HttpPromise promise = new HttpPromise(this);
        promise.then(callback);
        return promise;
    }

    public HttpPromise error(Runnable runnable) {
        HttpPromise promise = new HttpPromise(this);
        promise.error(runnable);
        return promise;
    }

    public HttpPromise error(WhenCallback callback) {
        HttpPromise promise = new HttpPromise(this);
        promise.error(callback);
        return promise;
    }

    public HttpPromise error(PromiseCallback callback, int when) {
        HttpPromise promise = new HttpPromise(this);
        promise.error(callback, when);
        return promise;
    }

    public final static int WHEN_BEGIN = -1;
    public final static int WHEN_ANYWAY = 0;
    public final static int WHEN_SUCCEED = 1;
    public final static int WHEN_ERROR = 2;
    public final static int WHEN_ABORTION = 3;
    public final static int WHEN_FAILED = 4;

    public interface WhenCallback {
        void onWhen(HttpQueryResponse resp, HttpAsyncQuery query, int when);
    }

    public interface PromiseCallback {
        void onPromise(HttpQueryResponse resp, HttpAsyncQuery query);
    }

    final ConcurrentHashMap<Runnable, Integer> executedRunnable = new ConcurrentHashMap();
    final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Runnable>> runnableTask = new ConcurrentHashMap();

    public HttpAsyncQuery runWhen(final WhenCallback callback, final int... when) {
        if (callback == null)
            return this;
        return runWhen(new Runnable() {
            @Override
            public void run() {
                HttpQueryResponse resp = null;
                int when = WHEN_ANYWAY;
                try {
                    resp = getResult();
//                    when = executedRunnable.get(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                callback.onWhen(resp, HttpAsyncQuery.this, when);
            }
        }, when);
    }

    public HttpAsyncQuery runWhen(Runnable runnable, int... when) {
        if (runnable == null) {
            return this;
        }
        for (int value : when) {
            addWhen(runnable, value);
        }
        return this;
    }

    private void addWhen(Runnable runnable, int conditionTime) {
        if (!isWhenContain(runnable, conditionTime)) {
            ConcurrentLinkedQueue<Runnable> runnableList = runnableTask.get(conditionTime);
            if (runnableList == null) {
                runnableList = new ConcurrentLinkedQueue();
            }
            runnableList.add(runnable);
            runnableTask.put(conditionTime, runnableList);
        }
    }

    private boolean isWhenContain(Runnable run, int conditionTime) {
        ConcurrentLinkedQueue<Runnable> runnableList = runnableTask.get(conditionTime);
        if (runnableList == null || runnableList.isEmpty()) {
            return false;
        }
        return runnableList.contains(run);
    }

    private void executeWhen(ConcurrentLinkedQueue<Runnable> runnableList, int when) {
        if (runnableList != null && runnableList.size() > 0) {
            for (Runnable runnable : runnableList) {
                if (!executedRunnable.contains(runnable)) {
                    try {
                        runnable.run();
                        executedRunnable.put(runnable, when);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public final static class HttpPromise {

        HttpAsyncQuery query;

        public HttpAsyncQuery getQuery() {
            return query;
        }

        HttpPromise(HttpAsyncQuery query) {
            this.query = query;
        }

        public HttpPromise then(final PromiseCallback callback) {
            if (callback == null) {
                return this;
            }
            query.runWhen(new WhenCallback() {
                @Override
                public void onWhen(HttpQueryResponse resp, HttpAsyncQuery query, int when) {
                    callback.onPromise(resp, query);
                }
            }, WHEN_SUCCEED);
            return this;
        }

        public HttpPromise then(Runnable runnable) {
            if (runnable == null) {
                return this;
            }
            query.runWhen(runnable, WHEN_SUCCEED);
            return this;
        }

        public HttpPromise error(final PromiseCallback pCallback, int when) {
            if (pCallback == null) {
                return this;
            }
            WhenCallback callback = new WhenCallback() {
                @Override
                public void onWhen(HttpQueryResponse resp, HttpAsyncQuery query, int when) {
                    pCallback.onPromise(resp, query);
                }
            };
            if (when != WHEN_FAILED && when != WHEN_ERROR && when != WHEN_ABORTION) {
                query.runWhen(callback, WHEN_FAILED, WHEN_ERROR, WHEN_ABORTION);
            } else {
                query.runWhen(callback, when);
            }
            return this;
        }

        public void error(WhenCallback callback) {
            if (callback == null) {
                return;
            }
            query.runWhen(callback, WHEN_FAILED, WHEN_ERROR, WHEN_ABORTION);
        }

        public HttpPromise error(Runnable runnable) {
            if (runnable == null) {
                return this;
            }
            query.runWhen(runnable, WHEN_FAILED, WHEN_ERROR, WHEN_ABORTION);
            return this;
        }
    }

    public boolean dismissAllRunWhen() {
        boolean isEmpty = runnableTask.isEmpty();
        runnableTask.clear();
        return !isEmpty;
    }

    public boolean dismissRunWhen(int... when) {
        boolean isEmpty = false;
        for (int i : when) {
            ConcurrentLinkedQueue<Runnable> runnables = runnableTask.get(i);
            if (runnables != null) {
                isEmpty &= runnables.isEmpty();
                runnables.clear();
            }
        }
        return !isEmpty;
    }

    public boolean dismissCallback() {
        boolean dismiss = mHttpCallBack != null;
        mHttpCallBack = null;
        return dismiss;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    String id;

    static String createQueryId() {
        long time = System.currentTimeMillis();
        while (taskQueue.contains(time + "")) {
            if (taskQueue.contains(time)) {
                time++;
            }
        }
        return time + "";
    }

    public String getID() {
        return this.id;
    }

    public HttpQuery<?> getHttpQuery() {
        return mHttp;
    }
}
