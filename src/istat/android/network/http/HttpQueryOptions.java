package istat.android.network.http;

import istat.android.network.utils.ToolKits.Stream;

public class HttpQueryOptions {
    public final static int DEFAULT_BUFFER_SIZE = 1024;
    int bufferSize = DEFAULT_BUFFER_SIZE;
    int connexionTimeOut = -1;
    int soTimeOut = -1;
    String encoding = Stream.DEFAULT_ENCODING;
    boolean autoClearRequestParams = false;
    int chunkedStreamingMode = 0, fixedLengthStreamingMode = 0;
    boolean followRedirects = true, instanceFollowRedirects = true,
            allowUserInteraction = true, useCaches = false;

    public static HttpQueryOptions build() {
        return new HttpQueryOptions();
    }

    HttpQueryOptions() {

    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public HttpQueryOptions setAllowUserInteraction(boolean allowUserInteraction) {
        this.allowUserInteraction = allowUserInteraction;
        return this;
    }

    public HttpQueryOptions setAutoClearRequestParams(
            boolean autoClearRequestParams) {
        this.autoClearRequestParams = autoClearRequestParams;
        return this;
    }

    public HttpQueryOptions setChunkedStreamingMode(int chunkedStreamingMode) {
        this.chunkedStreamingMode = chunkedStreamingMode;
        return this;
    }

    public HttpQueryOptions setConnexionTimeOut(int connexionTimeOut) {
        this.connexionTimeOut = connexionTimeOut;
        return this;
    }

    public HttpQueryOptions setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public HttpQueryOptions setFixedLengthStreamingMode(
            int fixedLengthStreamingMode) {
        this.fixedLengthStreamingMode = fixedLengthStreamingMode;
        return this;
    }

    public HttpQueryOptions setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public HttpQueryOptions setInstanceFollowRedirects(
            boolean instanceFollowRedirects) {
        this.instanceFollowRedirects = instanceFollowRedirects;
        return this;
    }

    public HttpQueryOptions setSoTimeOut(int soTimeOut) {
        this.soTimeOut = soTimeOut;
        return this;
    }

    public HttpQueryOptions setUseCaches(boolean useCaches) {
        this.useCaches = useCaches;
        return this;
    }
}
