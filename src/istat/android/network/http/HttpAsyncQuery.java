package istat.android.network.http;

import istat.android.network.util.ToolKits.Stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import istat.android.network.http.HttpAsyncQuery.HttpQueryResponse;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

public final class HttpAsyncQuery extends
		AsyncTask<String, HttpQueryResponse, Void> {
	public final static int TYPE_GET = 1, TYPE_POST = 2,
			DEFAULT_BUFFER_SIZE = 16384;
	public final static String DEFAULT_ENCODING = "UTF-8";
	HttpQueryCallBack mHttpCallBack;
	OnQueryCancelListener mOnQueryCancel;
	HttpQuery<?> mHttp;
	int type = TYPE_GET;
	int buffersize = DEFAULT_BUFFER_SIZE;
	String encoding = DEFAULT_ENCODING;
	private boolean running = true, complete = false;
	private long startTimeStamp = 0;
	private long endTimeStamp = 0;
	static final HashMap<Object, HttpAsyncQuery> taskQueue = new HashMap<Object, HttpAsyncQuery>();

	private HttpAsyncQuery(int type, HttpQuery<?> http,
			HttpQueryCallBack callBack) {
		mHttpCallBack = callBack;
		mHttp = http;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		running = true;
		startTimeStamp = System.currentTimeMillis();
		taskQueue.put(mHttpCallBack != null ? mHttpCallBack : Math.random()
				+ "", this);
	}

	@Override
	protected Void doInBackground(String... urls) {
		// TODO Auto-generated method stub
		for (String url : urls) {
			InputStream stream = null;
			Exception error = null;
			try {
				switch (type) {
				case TYPE_GET:
					stream = mHttp.doGet(url);
					break;
				case TYPE_POST:
					stream = mHttp.doPost(url);
					break;
				default:
					stream = mHttp.doGet(url);
					break;
				}

			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				error = e;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				error = e;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				error = e;
			}
			HttpQueryResponse response = new HttpQueryResponse(stream, error,
					encoding, buffersize, this);
			if (!mHttp.isAborted() && !isCancelled()) {
				publishProgress(response);
			} else {
				Log.i("istat.http.asyncQuery.query", "was aborded");
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(HttpQueryResponse... values) {
		// TODO Auto-generated method stub
		HttpQueryResponse response = values.length > 0 ? values[0] : null;
		if (mHttpCallBack != null && !mHttp.isAborted() && !isCancelled()) {
			mHttpCallBack.onHttRequestComplete(response);
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		complete = true;
		running = false;
		// Log.e("TAGI TAG", "post Execute");
		taskQueue.values().removeAll(Collections.singletonList(this));
	}

	@Override
	protected void onCancelled() {
		// TODO Auto-generated method stub
		running = false;
		complete = false;
		endTimeStamp = System.currentTimeMillis();
		if (mHttp.hasRunningRequest()) {
			mHttp.abortRequest();
		}
		if (mHttpCallBack != null && mHttpCallBack instanceof HttpCallBack) {
			((HttpCallBack) mHttpCallBack).onHttpAborted();
		}
		if (mOnQueryCancel != null) {
			mOnQueryCancel.onCancelled(this);
		}
		// taskQueue.remove(this);
		taskQueue.values().removeAll(Collections.singletonList(this));
		super.onCancelled();
	}

	public static HttpAsyncQuery doAsyncGet(HttpQuery<?> http, String... urls) {
		return doAsyncQuery(http, TYPE_GET, DEFAULT_BUFFER_SIZE,
				http.mOptions.encoding, null, urls);
	}

	public static HttpAsyncQuery doAsyncPost(HttpQuery<?> http, String... urls) {
		return doAsyncQuery(http, TYPE_POST, DEFAULT_BUFFER_SIZE,
				http.mOptions.encoding, null, urls);
	}

	public static HttpAsyncQuery doAsyncGet(HttpQuery<?> http,
			HttpQueryCallBack callBack, String... urls) {
		return doAsyncQuery(http, TYPE_GET, DEFAULT_BUFFER_SIZE,
				http.mOptions.encoding, callBack, urls);
	}

	public static HttpAsyncQuery doAsyncPost(HttpQuery<?> http,
			HttpQueryCallBack callBack, String... urls) {
		return doAsyncQuery(http, TYPE_POST, DEFAULT_BUFFER_SIZE,
				http.mOptions.encoding, callBack, urls);
	}

	public static HttpAsyncQuery doAsyncQuery(HttpQuery<?> http, int queryType,
			int buffersize, String encoding, HttpQueryCallBack callBack,
			String... urls) {

		HttpAsyncQuery query = new HttpAsyncQuery(queryType, http, callBack);
		query.type = queryType;
		query.encoding = encoding;
		query.buffersize = buffersize;
		query.execute(urls);
		return query;
	}

	public boolean isComplete() {
		if (isCancelled())
			return false;
		return complete;
	}

	public boolean isRunning() {
		return running;
	}

	public long getExecutionTime() {
		if (endTimeStamp <= startTimeStamp)
			return getDuration();
		return endTimeStamp - startTimeStamp;
	}

	private long getDuration() {
		return System.currentTimeMillis() - startTimeStamp;
	}

	// DEFAULT PROCESS CALLBACK IF USER DONT HAS DEFINE it Own
	QueryProcessCallBack<?> processCallBack = new QueryProcessCallBack<Integer>() {
		{
			this.query = HttpAsyncQuery.this;
		}

		@Override
		public String onBuildResponseBody(InputStream stream,
				HttpAsyncQuery query) {
			// TODO Auto-generated method stub
			return Stream.streamToString(stream, buffersize, encoding,
					getQuery().mHttp);
		}

		@Override
		public void onUpdateQueryProcess(HttpAsyncQuery query, Integer... vars) {
			// NOTHIG TO DO

		}
	};

	public boolean registerForProgressCallBack(QueryProcessCallBack<?> callBack) {
		if (callBack != null && processCallBack == callBack) {
			return false;
		}
		this.processCallBack = callBack;
		this.processCallBack.query = this;
		return true;
	}

	public static class HttpQueryResponse {
		String body;
		Exception error;
		HttpAsyncQuery mAsyncQ;

		public static HttpQueryResponse getErrorInstance(Exception e) {
			return new HttpQueryResponse(null, e);
		}

		HttpQueryResponse(InputStream stream, Exception e) {
			init(stream, Stream.DEFAULT_ENCODING, Stream.DEFAULT_BUFFER_SIZE, e);
		}

		HttpQueryResponse(InputStream stream, Exception e, String encoding,
				int buffersize, HttpAsyncQuery asyncQ) {
			mAsyncQ = asyncQ;
			init(stream, encoding, buffersize, e);
		}

		private void init(InputStream stream, String encoding, int buffersize,
				Exception e) {
			body = stream != null ? mAsyncQ.processCallBack
					.buildResponseBody(stream) : null;
			error = e;
		}

		public boolean hasError() {
			return error != null || TextUtils.isEmpty(body);
		}

		public boolean isSuccess() {
			return error == null && !TextUtils.isEmpty(body);
		}

		public String getBody() {
			return body;
		}

		public Exception getError() {
			return error;
		}
	}

	public static interface HttpQueryCallBack {
		public abstract void onHttRequestComplete(HttpQueryResponse result);
	}

	public static interface OnQueryCancelListener {
		public abstract void onCancelled(HttpAsyncQuery asyncQ);
	}

	public static interface OnHttpQueryComplete extends HttpQueryCallBack {
		public abstract void onHttRequestComplete(HttpQueryResponse result);

		public abstract void onHttpRequestSuccess(HttpQueryResponse result);

		public abstract void onHttpRequestFail(Exception e);

		public abstract void onHttpAborted();
	}

	public static abstract class HttpCallBack implements OnHttpQueryComplete {
		public final void onHttRequestComplete(HttpQueryResponse result) {
			if (result.isSuccess()) {
				onHttpRequestSuccess(result);
			} else {
				onHttpRequestFail(result.getError());
			}
			onHttRequestCompleted(result);
		}

		public abstract void onHttRequestCompleted(HttpQueryResponse result);

	}

	public void setOnQueryCancelListener(OnQueryCancelListener mOnQueryCancel) {
		this.mOnQueryCancel = mOnQueryCancel;
	}

	public void addTocken(String unikToken) {
		taskQueue.put(unikToken, this);
	}

	public static HttpAsyncQuery getTask(HttpQueryCallBack callback) {
		return taskQueue.get(callback);
	}

	public static HttpAsyncQuery getTask(Object tocken) {
		return taskQueue.get(tocken);
	}

	public static List<HttpAsyncQuery> getTaskqueue() {
		return new ArrayList<HttpAsyncQuery>(taskQueue.values());
	}

	public static void cancelAll() {
		for (HttpAsyncQuery http : getTaskqueue()) {
			http.cancel(true);
		}
	}

	public static void cancel(Object tocken) {
		HttpAsyncQuery http = getTask(tocken);
		if (http != null) {
			http.cancel(true);
		}
	}

	<K, V> K getKeyByValue(Map<K, V> map, V value) {
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static abstract class QueryProcessCallBack<ProgressVar> {
		Handler handler;
		HttpAsyncQuery query;

		public QueryProcessCallBack(Handler handler) {
			this.handler = handler;
		}

		public QueryProcessCallBack() {

		}

		public int getConnexionContentLenght() {
			return query != null && query.mHttp != null
					&& query.mHttp.currentConnexion != null ? query.mHttp.currentConnexion
					.getContentLength() : 0;
		}

		public String getConnexionContentType() {
			return query != null && query.mHttp != null
					&& query.mHttp.currentConnexion != null ? query.mHttp.currentConnexion
					.getContentType() : null;
		}

		public HttpAsyncQuery getQuery() {
			return query;
		}

		private Handler getHandler() {
			if (handler != null) {
				handler = new Handler();
			}
			return handler;
		}

		String buildResponseBody(InputStream stream) {
			return onBuildResponseBody(stream, query);
		}

		public abstract String onBuildResponseBody(InputStream stream,
				HttpAsyncQuery query);

		public void publishProgression(final ProgressVar... vars) {
			getHandler().post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					onUpdateQueryProcess(query, vars);
				}
			});
		}

		public abstract void onUpdateQueryProcess(HttpAsyncQuery query,
				ProgressVar... vars);
	}
}
