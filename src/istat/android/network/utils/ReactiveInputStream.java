package istat.android.network.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by istat on 04/05/17.
 */

public abstract class ReactiveInputStream extends InputStream {
    InputStream stream;

    protected abstract InputStream onCreateInputStream();

    @Override
    public int read() throws IOException {
        if (stream == null) {
            stream = onCreateInputStream();
        }
        return stream.read();
    }
}
