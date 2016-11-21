package istat.android.network.http.interfaces;

import java.util.HashMap;

import istat.android.network.http.HttpQuery;

public interface HttpSendable<HttpQ extends HttpQuery<?>> {
    void onFillHttpQuery(HttpQ httpQuery);


}
