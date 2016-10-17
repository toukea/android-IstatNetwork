package istat.android.network.http;

/**
 * Created by istat on 16/10/16.
 */

public class HttpAsyncQueryBuilder {
    HttpAsyncQuery mAsyncQuery;

    HttpAsyncQueryBuilder(HttpAsyncQuery asycQuery) {
        this.mAsyncQuery = asycQuery;
    }

    String encoding;

    public HttpAsyncQueryBuilder useEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public HttpAsyncQueryBuilder useDownloader(HttpAsyncQuery.HttpDownloadHandler downloader) {
        this.mAsyncQuery.downloadHandler = downloader;
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

    public HttpAsyncQuery doQuery(int method, MultipartHttpQuery.UpLoadHandler handler, HttpAsyncQuery.HttpQueryCallBack callback, String url) {
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