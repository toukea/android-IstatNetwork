package istat.android.network.http;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.concurrent.Executor;

import istat.android.network.http.interfaces.DownloadHandler;
import istat.android.network.http.interfaces.ProgressListener;
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

    public static AsyncHttp fromBodyPartHttp(Object body) {
        BodyPartHttpQuery http = new BodyPartHttpQuery(body);
        return new AsyncHttp(new HttpAsyncQuery(http));
    }

    public final static AsyncHttp fromSimpleHttp() {
        SimpleHttpQuery http = new SimpleHttpQuery();
        return new AsyncHttp(new HttpAsyncQuery(http));
    }

    public final static AsyncHttp fromMultipartHttp() {
        MultipartHttpQuery http = new MultipartHttpQuery();
        return new AsyncHttp(new HttpAsyncQuery(http));
    }

    AsyncHttp(HttpAsyncQuery asyncQuery) {
        this.mAsyncQuery = asyncQuery;
    }

    String encoding;

    public AsyncHttp useEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public AsyncHttp putHttpParam(String name, String value) {
        this.mAsyncQuery.mHttp.putParam(name, value);
        return this;
    }

    public AsyncHttp putHttpParams(HashMap<String, ?> params) {
        this.mAsyncQuery.mHttp.putParams(params);
        return this;
    }

    public AsyncHttp putHttpHeader(String name, String value) {
        this.mAsyncQuery.mHttp.putHeader(name, value);
        return this;
    }

    public AsyncHttp putHttpHeaders(HashMap<String, String> headers) {
        this.mAsyncQuery.mHttp.putHeaders(headers);
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

    public AsyncHttp useUploader(HttpAsyncQuery.UploadHandler uploader) {
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

    public AsyncHttp useDownloader(final DownloadHandler downloader, final ProgressListener progressionListener) {
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

    public AsyncHttp useDownloader(final DownloadHandler downloader, DownloadHandler.WHEN when, final ProgressListener progressionListener) {
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

    public HttpAsyncQuery doDelete(HttpAsyncQuery.Callback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_DELETE, url, callback, mAsyncQuery.uploadHandler);
    }


    public HttpAsyncQuery doGet(String url) {
        return doGet(this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doGet(HttpAsyncQuery.Callback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_GET, url, callback, mAsyncQuery.uploadHandler);
    }

    public HttpAsyncQuery doPost(String url) {
        return doPost(this.mAsyncQuery.uploadHandler, this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doPost(String url, HttpAsyncQuery.Callback callback) {
        return doPost(this.mAsyncQuery.uploadHandler, callback, url);
    }

    public HttpAsyncQuery doPost(HttpAsyncQuery.UploadHandler uploader, HttpAsyncQuery.Callback callback, String url) {
        return doPost((UpLoadHandler) uploader, callback, url);
    }

    public HttpAsyncQuery doPost(UpLoadHandler uploader, HttpAsyncQuery.Callback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_POST, url, callback, uploader);
    }

    public HttpAsyncQuery doHead(String url) {
        return doHead(url, this.mAsyncQuery.mHttpCallBack);
    }

    public HttpAsyncQuery doHead(String url, HttpAsyncQuery.Callback callback) {
        return doQuery(HttpAsyncQuery.TYPE_HEAD, url, callback, mAsyncQuery.uploadHandler);
    }

    public HttpAsyncQuery doPatch(String url) {
        return doPatch(this.mAsyncQuery.mHttpCallBack, url);
    }

    public HttpAsyncQuery doPatch(HttpAsyncQuery.Callback callback, String url) {
        return doQuery(HttpAsyncQuery.TYPE_PATCH, url, callback, mAsyncQuery.uploadHandler);
    }

    public HttpAsyncQuery doPut(HttpAsyncQuery.UploadHandler uploader, String url) {
        return doPut((UpLoadHandler) uploader, url);
    }

    public HttpAsyncQuery doPut(UpLoadHandler uploader, String url) {
        return doPut(url, this.mAsyncQuery.mHttpCallBack, uploader);
    }

    public HttpAsyncQuery doPut(HttpAsyncQuery.Callback callback, String url) {
        return doPut(url, callback, this.mAsyncQuery.uploadHandler);
    }

    public HttpAsyncQuery doPut(HttpAsyncQuery.UploadHandler uploader, HttpAsyncQuery.Callback callback, String url) {
        return doPut(url, callback, uploader);
    }

    public HttpAsyncQuery doPut(String url, HttpAsyncQuery.Callback callback, UpLoadHandler uploader) {
        return doQuery(HttpAsyncQuery.TYPE_PUT, url, callback, uploader);
    }

    public HttpAsyncQuery doPut(String url) {
        return doPut(url, this.mAsyncQuery.mHttpCallBack, this.mAsyncQuery.uploadHandler);
    }

    public HttpAsyncQuery doQuery(int method, String url) {
        return this.doQuery(method, url, this.mAsyncQuery.mHttpCallBack, this.mAsyncQuery.uploadHandler);
    }

    public HttpAsyncQuery doQuery(int method, String url, HttpAsyncQuery.Callback callback) {
        return this.doQuery(method, url, callback, this.mAsyncQuery.uploadHandler);
    }

    public HttpAsyncQuery doQuery(int method, String url, HttpAsyncQuery.Callback callback, UpLoadHandler uploader) {
        this.mAsyncQuery.type = method;
        this.mAsyncQuery.mHttpCallBack = callback;
        this.mAsyncQuery.setUploadHandler(uploader);
        this.mAsyncQuery.executeURLs(url);
        return this.mAsyncQuery;
    }

//    public HttpAsyncQuery doQuery(String method, UpLoadHandler uploader, HttpAsyncQuery.Callback callback, String url) {
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

    public AsyncHttp setQueryCallback(HttpAsyncQuery.Callback callback) {
        this.mAsyncQuery.mHttpCallBack = callback;
        return this;
    }
}
