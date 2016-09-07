package istat.android.network.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import istat.android.network.http.HttpAsyncQuery.HttpQueryResponse;
import istat.android.network.util.ToolKits.Text;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.util.Log;

/*
 * Copyright (C) 2014 Istat Dev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 * @author Toukea Tatsi (Istat)
 * 
 */
public abstract class HttpQuery<HttpQ extends HttpQuery<?>> implements
		HttpInterface {
	protected HttpQueryOptions mOptions = new HttpQueryOptions();
	protected HashMap<String, String> parametres = new HashMap<String, String>();
	protected HashMap<String, String> headers = new HashMap<String, String>();
	private volatile boolean aborted = false, querying = false;
	static String TAG_INPUT = "input", TAG_OUTPUT = "output";
	volatile HttpURLConnection currentConnexion;
	long lastConnextionTime = System.currentTimeMillis();
	protected HashMap<String, List<Long>> historic = new HashMap<String, List<Long>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		{
			put(TAG_INPUT, new ArrayList<Long>());
			put(TAG_OUTPUT, new ArrayList<Long>());
		}

	};
	static HashMap<String, List<Long>> historics = new HashMap<String, List<Long>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		{
			put(TAG_INPUT, new ArrayList<Long>());
			put(TAG_OUTPUT, new ArrayList<Long>());
		}

	};
	protected HashMap<String, List<Long>> timeHistoric = new HashMap<String, List<Long>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		{
			put(TAG_INPUT, new ArrayList<Long>());
			put(TAG_OUTPUT, new ArrayList<Long>());
		}

	};
	static HashMap<String, List<Long>> timeHistorics = new HashMap<String, List<Long>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		{
			put(TAG_INPUT, new ArrayList<Long>());
			put(TAG_OUTPUT, new ArrayList<Long>());
		}

	};

	@SuppressWarnings("unchecked")
	public HttpQ addHeader(String name, String value) {
		headers.put(name, value);
		return (HttpQ) this;
	}

	@SuppressWarnings("unchecked")
	public HttpQ addParam(String Name, String Value) {
		parametres.put(Name, Value);
		return (HttpQ) this;
	}

	@SuppressWarnings("unchecked")
	public HttpQ addParam(String Name, String[] values) {
		for (int i = 0; i < values.length; i++) {
			addParam(Name + "[" + i + "]", values[i]);
		}
		return (HttpQ) this;
	}

	@SuppressWarnings("unchecked")
	public HttpQ addParam(String Name, HashMap<Object, String> values) {
		Iterator<Object> iterator = values.keySet().iterator();
		while (iterator.hasNext()) {
			Object name = iterator.next();
			String value = values.get(name);
			addParam(Name + "[" + name + "]", value);
		}
		return (HttpQ) this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HttpQ addSendable(HttpSendable sendable) {
		sendable.onFillHttpQuery(this);
		return (HttpQ) this;
	}

	@SuppressWarnings("unchecked")
	public HttpQ addParams(List<BasicNameValuePair> nameValues) {
		for (BasicNameValuePair pair : nameValues)
			addParam(pair.getName(), pair.getValue());
		return (HttpQ) this;
	}

	@SuppressWarnings("unchecked")
	public HttpQ addParams(HashMap<String, Object> nameValues) {
		if (!nameValues.keySet().isEmpty()) {
			String[] table = new String[nameValues.size()];
			table = nameValues.keySet().toArray(table);
			for (String tmp : table) {
				if (tmp != null) {
					addParam(tmp, nameValues.get(tmp).toString());
				}
			}
		}
		return (HttpQ) this;
	}

	public void removeParam(String name) {
		parametres.remove(name);
	}

	public void removeHeder(String name) {
		headers.remove(name);
	}

	@SuppressWarnings("unchecked")
	public HttpQ clearParams() {
		parametres.clear();
		return (HttpQ) this;

	}

	@SuppressWarnings("unchecked")
	public HttpQ clearHeaders() {
		headers.clear();
		return (HttpQ) this;
	}

	@SuppressWarnings("unchecked")
	public HttpQ clearExtraData() {
		clearHeaders();
		clearParams();
		return (HttpQ) this;
	}

	protected synchronized HttpURLConnection preparConnexion(final String url, String method)
			throws IOException {
		onQueryStarting();
		URL Url = new URL(url);
		URLConnection urlConnexion = Url.openConnection();
		HttpURLConnection conn = (HttpURLConnection) urlConnexion;
		if (method.equals("POST") || method.equals("PUT")
				|| method.equals("PATCH")) {
			conn.setDoOutput(true);
		}
		if (!"COPY".equals(method) && !"RENAME".equals(method)
				&& !"MOVE".equals(method)) {
			conn.setRequestMethod(method);
		}
		if (mOptions != null) {
			applyOptions(conn);
		}
		fillHeader(conn);
		currentConnexion = conn;
		return conn;
	}

	public interface ParameterHandler {
		public static ParameterHandler DEFAULT_HANDLER = new ParameterHandler() {

			@Override
			public String onStringifyQueryParams(String method,
					HashMap<String, String> params, String encoding) {
				// TODO Auto-generated method stub
				try {
					return createStringularQueryableData(params, encoding);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		};

		public abstract String onStringifyQueryParams(String method,
				HashMap<String, String> params, String encoding);
	}

	ParameterHandler parameterHandler = ParameterHandler.DEFAULT_HANDLER;

	void setParameterHandler(ParameterHandler parameterHandler) {
		if (parameterHandler != null) {
			this.parameterHandler = parameterHandler;
		}
	}

	private void applyOptions(HttpURLConnection conn) {
		if (mOptions.chunkedStreamingMode > 0)
			conn.setChunkedStreamingMode(mOptions.chunkedStreamingMode);
		if (mOptions.fixedLengthStreamingMode > 0)
			conn.setFixedLengthStreamingMode(mOptions.fixedLengthStreamingMode);
		if (mOptions.followRedirects)
			HttpURLConnection.setFollowRedirects(mOptions.followRedirects);
		if (mOptions.instanceFollowRedirects)
			conn.setInstanceFollowRedirects(mOptions.instanceFollowRedirects);
		if (mOptions.useCaches)
			conn.setUseCaches(mOptions.useCaches);
		if (mOptions.soTimeOut > 0)
			conn.setReadTimeout(mOptions.soTimeOut);
		if (mOptions.connexionTimeOut > 0)
			conn.setConnectTimeout(mOptions.connexionTimeOut);
	}

	private void fillHeader(HttpURLConnection conn) {
		if (!headers.keySet().isEmpty()) {
			String[] table = new String[headers.size()];
			table = headers.keySet().toArray(table);
			for (String tmp : table) {
				if (tmp != null) {
					conn.addRequestProperty(tmp, headers.get(tmp));

				}
			}
		}
	}

	public InputStream doGet(String url, boolean handleerrror)
			throws IOException {
		// ---------------------------
		String method = "GET";
		String data = "";
		if (parameterHandler != null) {
			data = parameterHandler.onStringifyQueryParams(method, parametres,
					mOptions.encoding);
		}
		if (!Text.isEmpty(data)) {
			url += (url.contains("?") ? "" : "?") + data;
		}
		HttpURLConnection conn = preparConnexion(url, method);
		conn.setDoOutput(false);
		Log.d("HttpQuery.doGet", "method:" + conn.getRequestMethod() + " | "
				+ method);
		InputStream stream = eval(conn, handleerrror);
		addToOutputHistoric(data.length());
		onQueryComplete();
		return stream;
	}

	public InputStream doQuery(String url, String method, boolean handleerror)
			throws IOException {
		if ("GET".equalsIgnoreCase(method)) {
			return doGet(url);
		} else if ("POST".equalsIgnoreCase(method)) {
			return doPost(url);
		}
		// ---------------------------
		String data = "";
		if (parameterHandler != null) {
			data = parameterHandler.onStringifyQueryParams(method, parametres,
					mOptions.encoding);
		}
		if (!Text.isEmpty(data)) {
			url += (url.contains("?") ? "" : "?") + data;
		}
		HttpURLConnection conn = preparConnexion(url, method);
		try {
			final Class<?> httpURLConnectionClass = conn.getClass();
			final Class<?> parentClass = httpURLConnectionClass.getSuperclass();
			final Field methodField;
			if (parentClass == HttpsURLConnection.class) {
				methodField = parentClass.getSuperclass().getDeclaredField(
						"method");
			} else {
				methodField = parentClass.getDeclaredField("method");
			}
			methodField.setAccessible(true);
			methodField.set(conn, method);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		// .setRequestMethod(method);
		InputStream stream = eval(conn, handleerror);
		addToOutputHistoric(data.length());
		onQueryComplete();
		return stream;
	}

	protected InputStream POST(String url, boolean handleerror)
			throws IOException {
		String method = "POST";
		HttpURLConnection conn = preparConnexion(url, method);
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		DataOutputStream writer = new DataOutputStream(os);
		long length = onWriteDataOnOutputStream(method, writer);
		writer.flush();
		writer.close();
		os.close();
		InputStream stream = eval(conn, handleerror);
		addToOutputHistoric(length);
		onQueryComplete();
		return stream;
	}

	protected long onWriteDataOnOutputStream(String method,
			DataOutputStream writer) throws IOException {
		// TODO Auto-generated method stub
		String data = "";
		if (parameterHandler != null) {
			data = parameterHandler.onStringifyQueryParams(method, parametres,
					mOptions.encoding);
		}
		writer.writeBytes(data);
		return data.length();
	}

	public InputStream doGet(String url) throws IOException {
		return doGet(url, true);
	}

	public InputStream doPut(String url) throws IOException {
		String method = "PUT";
		HttpURLConnection conn = preparConnexion(url, method);
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		DataOutputStream writer = new DataOutputStream(os);
		String data = "";
		if (parameterHandler != null) {
			data = parameterHandler.onStringifyQueryParams(method, parametres,
					mOptions.encoding);
		}
		writer.writeBytes(data);
		writer.flush();
		writer.close();
		os.close();
		InputStream stream = eval(conn);
		addToOutputHistoric(data.length());
		onQueryComplete();
		return stream;
	}

	public InputStream doHead(String url) throws IOException {
		return doQuery(url, "HEAD");
	}

	public InputStream doDelete(String url) throws IOException {
		return doQuery(url, "DELETE");
	}

	public InputStream doCopy(String url) throws IOException {
		return doQuery(url, "COPY");
	}

	public InputStream doPatch(String url) throws IOException {
		return doQuery(url, "PATCH");
	}

	public InputStream doQuery(String url, String method) throws IOException {
		return doQuery(url, method, true);
	}

	public String getURL(String adresse) throws IOException {
		// --------------------------
		String parmString = createStringularQueryableData(parametres,
				mOptions.encoding);
		return adresse
				+ (parmString == null || parmString.equals("") ? "" : "?"
						+ parmString);
	}

	public void shutDownConnection() {
		currentConnexion.disconnect();
		currentConnexion = null;
		onQueryComplete();
		aborted = true;
	}

	public boolean isAutoClearParamsEnable() {
		return mOptions.autoClearRequestParams;
	}

	public HttpURLConnection getCurrentConnexion() {
		return currentConnexion;
	}

	public boolean hasRunningRequest() {
		Log.d("HttQuery", "hasRunningRequest::querying=" + querying);
		return /* querying && */currentConnexion != null;
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setConnectionTimeOut(int milisec) {
		mOptions.connexionTimeOut = milisec;

	}

	public void setSoTimeOut(int milisec) {
		mOptions.connexionTimeOut = milisec;
	}

	public int getConnectionTimeOut() {
		return mOptions.connexionTimeOut;
	}

	public int getSoTimeOut() {
		return mOptions.soTimeOut;
	}

	public void setEncoding(String encoding) {
		this.mOptions.encoding = encoding;
	}

	public void setClearParamsAfterEachQueryEnable(boolean autoClearParams) {
		this.mOptions.autoClearRequestParams = autoClearParams;
	}

	public static String createStringularQueryableData(
			HashMap<String, String> params, String encoding)
			throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (value == null) {
				continue;
			}
			if (first) {
				first = false;
			} else {
				result.append("&");
			}

			result.append(URLEncoder.encode(name, encoding));
			result.append("=");
			result.append(URLEncoder.encode(value, encoding));
		}

		return result.toString();
	}

	void onQueryStarting() {
		// TODO Auto-generated method stub
		lastConnextionTime = System.currentTimeMillis();
		aborted = false;
		querying = true;
	}

	void onQueryComplete() {
		if (mOptions != null && mOptions.autoClearRequestParams) {
			clearParams();
		}
		querying = false;
	}

	void addToInputHistoric(long input) {
		historic.get(TAG_INPUT).add(input);
		historics.get(TAG_INPUT).add(input);
		Long elapsed = System.currentTimeMillis() - lastConnextionTime;
		timeHistoric.get(TAG_INPUT).add(elapsed);
		timeHistorics.get(TAG_INPUT).add(elapsed);
	}

	void addToOutputHistoric(long input) {
		historic.get(TAG_OUTPUT).add(input);
		historics.get(TAG_OUTPUT).add(input);
		Long elapsed = System.currentTimeMillis() - lastConnextionTime;
		timeHistoric.get(TAG_OUTPUT).add(elapsed);
		timeHistorics.get(TAG_OUTPUT).add(elapsed);
	}

	InputStream eval(HttpURLConnection conn) throws IOException {
		return eval(conn, true);
	}

	protected InputStream currentInputStream;
	protected OutputStream currentOutputStream;

	InputStream eval(HttpURLConnection conn, boolean handleerrror)
			throws IOException {
		int eval = 0;
		InputStream stream = null;
		if (conn != null) {
			eval = conn.getContentLength();
		}

		if (HttpQueryResponse.isSuccessCode(conn.getResponseCode())) {
			stream = conn.getInputStream();
		} else if (handleerrror) {
			stream = conn.getErrorStream();
		}
		if (stream != null) {
			eval = stream.available();
		}
		currentInputStream = stream;
		addToInputHistoric(eval);
		return stream;
	}

	int getCurrentResponseCode() {
		try {
			if (getCurrentConnexion() != null) {
				return getCurrentConnexion().getResponseCode();
			} else {
				Log.d("HttpQuery.getCurrentResponseCode", "connexion::"
						+ getCurrentConnexion());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return -1;
	}

	String getCurrentResponseMessage() {
		try {
			if (getCurrentConnexion() != null) {
				return getCurrentConnexion().getResponseMessage();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return "";
	}

	/**
	 * this method is deprecated. be shure it is really what you need.
	 * 
	 */
	public boolean abortRequest() {
		boolean out = hasRunningRequest();
		Log.d("HttQuery", "abortRequest_start");
		if (out) {
			Log.d("HttQuery", "abortRequest_has_running");
			try {

				if (currentInputStream != null) {
					currentInputStream.close();
				}
				Log.d("HttQuery", "abortRequest_stream_closed::"+currentInputStream);
				// if (outp != null) {
				// currentConnexion.getOutputStream().close();
				// }
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.d("HttQuery", "abortRequest_stream_Yooooo::"+currentInputStream);
			currentConnexion.disconnect();
			onQueryComplete();
		}
		aborted = true;
		return out;
	}

	// private void notifyAborted() {
	// onQueryComplete();
	// aborted = true;
	// }

	public List<Long> getCurrentOutputContentLegthHistoric() {
		return historic.get(TAG_OUTPUT);
	}

	public List<Long> getCurrentInputContentLegthHistoric() {
		return historic.get(TAG_INPUT);
	}

	public long getCurrentOutputContentLegth() {
		long out = 0;
		for (long i : historic.get(TAG_OUTPUT)) {
			out += i;
		}
		return out;
	}

	public long getCurrentInputContentLegth() {
		long out = 0;
		for (long i : historic.get(TAG_INPUT)) {
			out += i;
		}
		return out;
	}

	// -----------------------------------------
	public List<Long> getCurrentOutputTimeHistoric() {
		return timeHistoric.get(TAG_OUTPUT);
	}

	public List<Long> getCurrentInputTimeHistoric() {
		return timeHistoric.get(TAG_INPUT);
	}

	public long getCurrentOutputTime() {
		long out = 0;
		for (long i : timeHistoric.get(TAG_OUTPUT)) {
			out += i;
		}
		return out;
	}

	public long getCurrentInputTime() {
		long out = 0;
		for (long i : timeHistoric.get(TAG_INPUT)) {
			out += i;
		}
		return out;
	}

	public void setOptions(HttpQueryOptions option) {
		this.mOptions = option;
		if (this.mOptions == null) {
			this.mOptions = new HttpQueryOptions();
		}
	}

	public HttpQueryOptions getOptions() {
		return mOptions;
	}

	public static List<Long> getOutputContentLegthHistoric() {
		return historics.get(TAG_OUTPUT);
	}

	public static List<Long> getInputContentLegthHistoric() {
		return historics.get(TAG_INPUT);
	}

	public static long getOutputContentLegth() {
		long out = 0;
		for (long i : historics.get(TAG_OUTPUT)) {
			out += i;
		}
		return out;
	}

	public static long getInputContentLegth() {
		long out = 0;
		for (long i : historics.get(TAG_INPUT)) {
			out += i;
		}
		return out;
	}

	public static List<Long> getOutputTimeHistoric() {
		return timeHistorics.get(TAG_OUTPUT);
	}

	public static List<Long> getInputTimeHistoric() {
		return timeHistorics.get(TAG_INPUT);
	}

	public static long getOutputTime() {
		long out = 0;
		for (long i : timeHistorics.get(TAG_OUTPUT)) {
			out += i;
		}
		return out;
	}

	public static long getInputTime() {
		long out = 0;
		for (long i : timeHistorics.get(TAG_INPUT)) {
			out += i;
		}
		return out;
	}
}
