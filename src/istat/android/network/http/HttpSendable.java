package istat.android.network.http;

public interface HttpSendable<HttpQ extends HttpQuery<?>> {
	public void onFillHttpQuery(HttpQ httpQuery);
}
