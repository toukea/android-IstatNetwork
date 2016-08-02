package istat.android.network.http;

public class HttpQueryOptions {
	int connexionTimeOut = -1;
	int soTimeOut = -1;
	String encoding = "UTF-8";
	boolean autoClearRequestParams = false;
	int chunkedStreamingMode = 0, fixedLengthStreamingMode = 0;
	boolean followRedirects = true, instanceFollowRedirects = true,
			allowUserInteraction = true, useCaches = false;

	public static HttpQueryOptions build() {
		return new HttpQueryOptions();
	}

	HttpQueryOptions() {
		// TODO Auto-generated constructor stub
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
