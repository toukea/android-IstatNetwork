package istat.android.network.http.interfaces;

import istat.android.network.http.HttpQuery;

public interface HttpSendable<HttpQ extends HttpQuery<?>> {
    void onFillHttpQuery(HttpQ httpQuery);
}
