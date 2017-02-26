package istat.android.network.http.interfaces;

import istat.android.network.http.HttpAsyncQuery;

/**
 * Created by istat on 26/02/17.
 */

public interface ProgressionListener<ProgressVar> {
    public abstract void onProgress(HttpAsyncQuery query,
                                    ProgressVar... vars);
}
