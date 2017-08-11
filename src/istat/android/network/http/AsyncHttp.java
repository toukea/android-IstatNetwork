package istat.android.network.http;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Executor;

import istat.android.network.http.interfaces.DownloadHandler;
import istat.android.network.http.interfaces.ProgressionListener;
import istat.android.network.http.interfaces.UpLoadHandler;

/**
 * Created by istat on 16/10/16.
 */

public final class AsyncHttp {
    HttpAsyncQuery mAsyncQuery;

    public final static AsyncHttp from(HttpQuery http) {
        HttpAsyncQuery asycQ = new HttpAsyncQuery(http);
        return new AsyncHttp(asycQ);
    }

    /**
     * deprecated, use {@link #fromSimpleHttp()} instead.
     *
     * @return
     */
    @Deprecated
    public final static AsyncHttp fromDefaultHttp() {
        return fromSimpleHttp();
    }

    public final static AsyncHttp fromSimpleHttp() {
        SimpleHttpQuery http = new SimpleHttpQuery();
        return new AsyncHttp(new HttpAsyncQuery(http));
    }

    public final static AsyncHttp fromMultipartHttp() {
        MultipartHttpQuery http = new MultipartHttpQuery();
        return new AsyncHttp(new HttpAsyncQuery(http));
    }

    AsyncHttp(HttpAsyncQuery asycQuery) {
        this.mAsyncQuery = asycQuery;
    }

    String encoding;

    public AsyncHttp useEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public AsyncHttp addHttpParam(String name, String value) {
        this.mAsyncQuery.mHttp.addParam(name, value);
        return this;
    }

    public AsyncHttp addHttpParams(HashMap<String, ?> params) {
        this.mAsyncQuery.mHttp.addParams(params);
        return this;
    }

    public AsyncHttp addHttpHeader(String name, String value) {
        this.mAsyncQuery.mHttp.addHeader(name, value);
        return this;
    }

    public AsyncHttp addHttpHeaders(HashMap<String, String> headers) {
        this.mAsyncQuery.mHttp.addHeaders(headers);
        return this;
    }

    public HttpQuery<?> getHttpQuery() {
        return this.mAsyncQuery.mHttp;
    }

    public AsyncHttp useExecutor(Executor executor) {
        this.mAsyncQuery.mExecutor = executor;
        return this;
    }


    public AsyncHttp useUploader(UpLoadHandler uploader) {
        this.mAsyncQuery.setUploadHandler(uploader);
        return this;
    }

    public AsyncHttp useUploader(HttpAsyncQuery.HttpUploadHandler uploader) {
        this.mAsyncQuery.setUploadHandler(uploader);
        return this;
    }


    public AsyncHttp useDownloader(final DownloadHandler downloader) {
        DownloadHandler.WHEN when = null;
        return useDownloader(downloader, when);
    }

    public AsyncHttp useDownloader(HttpAsyncQuery.HttpDownloadHandler downloader) {
        DownloadHandler.WHEN when = null;
        return useDownloader(downloader, when);
    }

    public AsyncHttp useDownloader(final DownloadHandler downloader, final ProgressionListener progressionListener) {
        DownloadHandler.WHEN when = null;
        return useDownloader(downloader, when, progressionListener);
    }

    public AsyncHttp useDownloader(final DownloadHandler downloader, DownloadHandler.WHEN when) {
        return useDownloader(downloader, when, null);
    }

    public AsyncHttp useDownloader(HttpAsyncQuery.HttpDownloadHandler downloader, DownloadHandler.WHEN when) {
        this.mAsyncQuery.setDownloadHandler(downloader, when);
        return this;
    }

    public AsyncHttp useDownloader(final DownloadHandler downloader, DownloadHandler.WHEN when, final ProgressionListener progressionListener) {
        if (downloader == null && progressionListener == null) {
            return this;
        }
        this.mAsyncQuery.setDownloadHandler(new HttpAsyncQuery.HttpDownloadHandler() {
            @Override
            public void onProgress(HttpAsyncQuery query, long... integers) {
                if (progressionListener != null) {
                    progressionListener.onProgress(query, integers);
                }
            }

            @Override
            public Object onBuildResponseBody(HttpURLConnection connexion, InputStream stream) throws Exception {
                if (downloader != null) {
                    return downloader.onBuildResponseBody(connexion, stream);
                } else {
                    return AsyncHttp.this.mAsyncQuery.getDefaultDownloader();
                }
            }
        }, when);
        return this;
    }

    public AsyncHttp useBufferSize(int bufferSize) {
        this.mAsyncQuery.bufferSize = bufferSize;
        return this;
    }


    public HttpAsyncQuery doDelete(String url) {
        return doDelete(this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doDelete(HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_DELETE, mAsyncQuery.uploadHandler, callback, url);
    }


    public HttpAsyncQuery doGet(String url) {
        return doGet(this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doGet(HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_GET, mAsyncQuery.uploadHandler, callback, url);
    }

    public HttpAsyncQuery doPost(String url) {
        return doPost(this.mAsyncQuery.uploadHandler, this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doPost(HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doPost(this.mAsyncQuery.uploadHandler, callback, url);
    }

    public HttpAsyncQuery doPost(HttpAsyncQuery.HttpUploadHandler uploader, HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doPost((UpLoadHandler) uploader, callback, url);
    }

    public HttpAsyncQuery doPost(UpLoadHandler uploader, HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_POST, uploader, callback, url);
    }

    public HttpAsyncQuery doHead(String url) {
        return doHead(this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doHead(HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_HEAD, mAsyncQuery.uploadHandler, callback, url);
    }

    public HttpAsyncQuery doPatch(String url) {
        return doPatch(this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doPatch(HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_PATCH, mAsyncQuery.uploadHandler, callback, url);
    }

    public HttpAsyncQuery doPut(HttpAsyncQuery.HttpUploadHandler uploader, String url) {
        return doPut((UpLoadHandler) uploader, url);
    }

    public HttpAsyncQuery doPut(UpLoadHandler uploader, String url) {
        return doPut(uploader, this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doPut(HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doPut(this.mAsyncQuery.uploadHandler, callback, url);
    }

    public HttpAsyncQuery doPut(HttpAsyncQuery.HttpUploadHandler uploader, HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doPut((UpLoadHandler) uploader, callback, url);
    }

    public HttpAsyncQuery doPut(UpLoadHandler uploader, HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_PUT, uploader, callback, url);
    }

    public HttpAsyncQuery doPut(String url) {
        return doPut(this.mAsyncQuery.uploadHandler, this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doQuery(int method, HttpAsyncQuery.HttpQueryCallback callback, String url) {
        return this.doQuery(method, this.mAsyncQuery.uploadHandler, callback, url);
    }

    public HttpAsyncQuery doQuery(int method, UpLoadHandler uploader, HttpAsyncQuery.HttpQueryCallback callback, String url) {
        this.mAsyncQuery.type = method;
        this.mAsyncQuery.mHttpCallBack = callback;
        this.mAsyncQuery.setUploadHandler(uploader);
        this.mAsyncQuery.executeURLs(url);
        return this.mAsyncQuery;
    }

//    public HttpAsyncQuery doQuery(String method, UpLoadHandler uploader, HttpAsyncQuery.HttpQueryCallback callback, String url) {
//        int methodInt = HttpAsyncQuery.TYPE_GET;
//        case "GET":
//        methodInt = HttpAsyncQuery.TYPE_GET;
//        break;
//        case "POST":
//        methodInt = HttpAsyncQuery.TYPE_POST;
//        break;
//        case "PUT":
//        methodInt = HttpAsyncQuery.TYPE_PUT;
//        break;
//        case "HEAD":
//        methodInt = HttpAsyncQuery.TYPE_HEAD;
//        break;
//        case "PATCH":
//        methodInt = HttpAsyncQuery.TYPE_PATCH;
//        break;
//        case "DELETE":
//        methodInt = HttpAsyncQuery.TYPE_DELETE;
//        break;
//
//        return doQuery(methodInt, uploader, callback, url);
//    }

    public AsyncHttp setCancelListener(HttpAsyncQuery.CancelListener listener) {
        this.mAsyncQuery.mCancelListener = listener;
        return this;
    }

    public AsyncHttp setQueryCallback(HttpAsyncQuery.HttpQueryCallback callback) {
        this.mAsyncQuery.mHttpCallBack = callback;
        return this;
    }
}
