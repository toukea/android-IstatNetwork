package istat.android.network.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by istat on 04/05/17.
 */

public abstract class ReactiveInputStream extends InputStream {
    InputStream stream;
    int lastRead = 0;

    protected abstract InputStream onCreateInputStream();

    @Override
    public int read() throws IOException {
        if (stream == null) {
            if (lastRead < 1) {
                return -1;
            }
            stream = onCreateInputStream();
            lastRead = -1;
        }
        lastRead = stream.read();
        return lastRead;
    }
}
