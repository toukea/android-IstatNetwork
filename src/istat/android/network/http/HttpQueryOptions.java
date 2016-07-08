package istat.android.network.http;

public class HttpQueryOptions {
	public int connexionTimeOut = -1;
	public int soTimeOut = -1;
	public String encoding = "UTF-8";
	public boolean autoClearRequestParams = false;
	public int chunkedStreamingMode = 0, fixedLengthStreamingMode = 0;
	public boolean followRedirects = true, instanceFollowRedirects = true,
			allowUserInteraction = true, useCaches = false;

	public HttpQueryOptions() {
		// TODO Auto-generated constructor stub
	}
}
