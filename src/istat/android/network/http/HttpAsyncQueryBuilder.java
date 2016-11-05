package istat.android.network.http;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;

import istat.android.network.http.interfaces.DownloadHandler;
import istat.android.network.http.interfaces.UpLoadHandler;

/**
 * Created by istat on 16/10/16.
 */

public final class HttpAsyncQueryBuilder {
    HttpAsyncQuery mAsyncQuery;

    HttpAsyncQueryBuilder(HttpAsyncQuery asycQuery) {
        this.mAsyncQuery = asycQuery;
    }

    String encoding;

    public HttpAsyncQueryBuilder useEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public HttpAsyncQueryBuilder addHttpParam(String name, String value) {
        this.mAsyncQuery.mHttp.addParam(name, value);
        return this;
    }

    public HttpAsyncQueryBuilder addHttpHeader(String name, String value) {
        this.mAsyncQuery.mHttp.addHeader(name, value);
        return this;
    }

    public HttpQuery<?> getHttpQuery() {
        return this.mAsyncQuery.mHttp;
    }

    public HttpAsyncQueryBuilder useExecutor(Executor executor) {
        this.mAsyncQuery.mExecutor = executor;
        return this;
    }

    public HttpAsyncQueryBuilder useDownloader(HttpAsyncQuery.HttpDownloadHandler downloader) {
        this.mAsyncQuery.downloadHandler = downloader;
        return this;
    }

    public HttpAsyncQueryBuilder useDownloader(final DownloadHandler downloader) {
        this.mAsyncQuery.downloadHandler = new HttpAsyncQuery.HttpDownloadHandler<Integer>() {
            @Override
            public void onDownloadProgress(HttpAsyncQuery query, Integer... integers) {

            }

            @Override
            public Object onBuildResponseBody(HttpURLConnection connexion, InputStream stream, HttpAsyncQuery query) {
                return downloader.onBuildResponseBody(connexion, stream, query);
            }

            @Override
            protected void onProcessFail(Exception e) {
                throw new RuntimeException(e);
            }
        };
        return this;
    }

    public HttpAsyncQueryBuilder useBufferSize(int bufferSize) {
        this.mAsyncQuery.bufferSize = bufferSize;
        return this;
    }


    public HttpAsyncQuery doDelete(String url) {
        return doDelete(null, url);
    }

    public HttpAsyncQuery doDelete(HttpAsyncQuery.HttpQueryCallBack callback, String url) {
        this.mAsyncQuery.type = HttpAsyncQuery.TYPE_DELETE;
        this.mAsyncQuery.mHttpCallBack = callback;
        this.mAsyncQuery.executeURLs(url);
        return this.mAsyncQuery;
    }


    public HttpAsyncQuery doGet(String url) {
        return doGet(null, url);
    }

    public HttpAsyncQuery doGet(HttpAsyncQuery.HttpQueryCallBack callback, String url) {
        this.mAsyncQuery.type = HttpAsyncQuery.TYPE_GET;
        this.mAsyncQuery.mHttpCallBack = callback;
        this.mAsyncQuery.executeURLs(url);
        return this.mAsyncQuery;
    }

    public HttpAsyncQuery doPost(String url) {
        return doPost(null, null, url);
    }

    public HttpAsyncQuery doPost(HttpAsyncQuery.HttpQueryCallBack callback, String url) {
        return doPost(null, callback, url);
    }

    public HttpAsyncQuery doPost(HttpAsyncQuery.HttpUploadHandler<?> uploader, HttpAsyncQuery.HttpQueryCallBack callback, String url) {
        this.mAsyncQuery.type = HttpAsyncQuery.TYPE_POST;
        this.mAsyncQuery.setUploadHandler(uploader);
        this.mAsyncQuery.mHttpCallBack = callback;
        this.mAsyncQuery.executeURLs(url);
        return this.mAsyncQuery;
    }

    public HttpAsyncQuery doHead(String url) {
        return doHead(null, url);
    }

    public HttpAsyncQuery doHead(HttpAsyncQuery.HttpQueryCallBack callback, String url) {
        this.mAsyncQuery.type = HttpAsyncQuery.TYPE_HEAD;
        this.mAsyncQuery.mHttpCallBack = callback;
        this.mAsyncQuery.executeURLs(url);
        return this.mAsyncQuery;
    }

    public HttpAsyncQuery doPatch(String url) {
        return doPatch(null, url);
    }

    public HttpAsyncQuery doPatch(HttpAsyncQuery.HttpQueryCallBack callback, String url) {
        this.mAsyncQuery.type = HttpAsyncQuery.TYPE_PATCH;
        this.mAsyncQuery.mHttpCallBack = callback;
        this.mAsyncQuery.executeURLs(url);
        return this.mAsyncQuery;
    }


    public HttpAsyncQuery doPush(HttpAsyncQuery.HttpUploadHandler<?> uploader, String url) {
        return doPush(uploader, null, url);
    }

    public HttpAsyncQuery doPush(HttpAsyncQuery.HttpQueryCallBack callback, String url) {
        return doPush(null, callback, url);
    }

    public HttpAsyncQuery doPush(HttpAsyncQuery.HttpUploadHandler<?> uploader, HttpAsyncQuery.HttpQueryCallBack callback, String url) {
        this.mAsyncQuery.type = HttpAsyncQuery.TYPE_POST;
        this.mAsyncQuery.setUploadHandler(uploader);
        this.mAsyncQuery.mHttpCallBack = callback;
        this.mAsyncQuery.executeURLs(url);
        return this.mAsyncQuery;
    }

    public HttpAsyncQuery doQuery(int method, UpLoadHandler handler, HttpAsyncQuery.HttpQueryCallBack callback, String url) {
        this.mAsyncQuery.type = method;
        this.mAsyncQuery.mHttpCallBack = callback;
        this.mAsyncQuery.executeURLs(url);
        return this.mAsyncQuery;
    }


    public HttpAsyncQueryBuilder setCancelListener(HttpAsyncQuery.CancelListener listener) {
        this.mAsyncQuery.mCancelListener = listener;
        return this;
    }

    public HttpAsyncQueryBuilder setQueryCallBack(HttpAsyncQuery.HttpQueryCallBack callback) {
        this.mAsyncQuery.mHttpCallBack = callback;
        return this;
    }
}
