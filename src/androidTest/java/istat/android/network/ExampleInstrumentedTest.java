package istat.android.network;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import istat.android.network.http.HttpQueryResponse;
import istat.android.network.http.SimpleHttpQuery;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("istat.android.base.tools.test", appContext.getPackageName());
        SimpleHttpQuery http = new SimpleHttpQuery();
        http.putParam("query", "name");
        http.putParam("hl", "en");
        HttpQueryResponse response = http.doGet("www.google.com");
        assertTrue(response.isSuccess());
    }
}
