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
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import istat.android.network.http.HttpAsyncQuery.HttpQueryResponse;
import istat.android.network.util.ToolKits.Text;
import org.apache.http.message.BasicNameValuePair;
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
	private boolean aborted = false, querying = false;;
	static String TAG_INPUT = "input", TAG_OUTPUT = "output";
	HttpURLConnection currentConnexion;
	long lastConnextionTime = System.currentTimeMillis();
	protected HashMap<String, List<Integer>> historic = new HashMap<String, List<Integer>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		{
			put(TAG_INPUT, new ArrayList<Integer>());
			put(TAG_OUTPUT, new ArrayList<Integer>());
		}

	};
	static HashMap<String, List<Integer>> historics = new HashMap<String, List<Integer>>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		{
			put(TAG_INPUT, new ArrayList<Integer>());
			put(TAG_OUTPUT, new ArrayList<Integer>());
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

	protected HttpURLConnection preparConnexion(final String url)
			throws IOException {
		onQueryStarting();
		URL Url = new URL(url);
		URLConnection urlConnexion = Url.openConnection();
		HttpURLConnection conn = (HttpURLConnection) urlConnexion;
		conn.setDoOutput(true);
		if (mOptions != null) {
			applyOptions(conn);
		}
		conn.setDoInput(true);
		fillHeader(conn);
		currentConnexion = conn;
		aborted = false;
		querying = true;
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
		HttpURLConnection conn = preparConnexion(url);
		conn.setRequestMethod(method);
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
		HttpURLConnection conn = preparConnexion(url);
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
		HttpURLConnection conn = preparConnexion(url);
		conn.setDoOutput(true);
		conn.setRequestMethod(method);
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
		InputStream stream = eval(conn, handleerror);
		addToOutputHistoric(data.length());
		onQueryComplete();
		return stream;
	}

	public InputStream doGet(String url) throws IOException {
		return doGet(url, true);
	}

	public InputStream doPut(String url) throws IOException {
		String method = "PUT";
		HttpURLConnection conn = preparConnexion(url);
		conn.setDoOutput(true);
		conn.setRequestMethod(method);
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
		return adresse + "?"
				+ createStringularQueryableData(parametres, mOptions.encoding);
	}

	public void shutDownConnection() {
		currentConnexion.disconnect();
		currentConnexion = null;
		aborted = true;
		querying = false;
	}

	public boolean isAutoClearParamsEnable() {
		return mOptions.autoClearRequestParams;
	}

	public HttpURLConnection getCurrentConnexion() {
		return currentConnexion;
	}

	public boolean hasRunningRequest() {
		return querying && currentConnexion != null;
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
	}

	void onQueryComplete() {
		if (mOptions != null && mOptions.autoClearRequestParams) {
			clearParams();
		}
		querying = false;
		// currentConnexion. = null;
	}

	void addToInputHistoric(int input) {
		historic.get(TAG_INPUT).add(input);
		historics.get(TAG_INPUT).add(input);
		Long elapsed = System.currentTimeMillis() - lastConnextionTime;
		timeHistoric.get(TAG_INPUT).add(elapsed);
		timeHistorics.get(TAG_INPUT).add(elapsed);
	}

	void addToOutputHistoric(int input) {
		historic.get(TAG_OUTPUT).add(input);
		historics.get(TAG_OUTPUT).add(input);
		Long elapsed = System.currentTimeMillis() - lastConnextionTime;
		timeHistoric.get(TAG_OUTPUT).add(elapsed);
		timeHistorics.get(TAG_OUTPUT).add(elapsed);
	}

	InputStream eval(HttpURLConnection conn) {
		return eval(conn, true);
	}

	InputStream eval(HttpURLConnection conn, boolean handleerrror) {
		int eval = 0;
		InputStream stream = null;
		if (conn != null) {
			eval = conn.getContentLength();
		}
		try {
			if (HttpQueryResponse.isSuccessCode(conn.getResponseCode())) {
				stream = conn.getInputStream();
			} else if (handleerrror) {
				stream = conn.getErrorStream();
			}
			if (stream != null) {
				eval = stream.available();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		if (out) {
			currentConnexion.disconnect();
			onQueryComplete();
		}
		aborted = true;
		querying = false;
		return out;
	}

	public List<Integer> getCurrentOutputContentLegthHistoric() {
		return historic.get(TAG_OUTPUT);
	}

	public List<Integer> getCurrentInputContentLegthHistoric() {
		return historic.get(TAG_INPUT);
	}

	public int getCurrentOutputContentLegth() {
		int out = 0;
		for (int i : historic.get(TAG_OUTPUT)) {
			out += i;
		}
		return out;
	}

	public int getCurrentInputContentLegth() {
		int out = 0;
		for (int i : historic.get(TAG_INPUT)) {
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

	public static List<Integer> getOutputContentLegthHistoric() {
		return historics.get(TAG_OUTPUT);
	}

	public static List<Integer> getInputContentLegthHistoric() {
		return historics.get(TAG_INPUT);
	}

	public static int getOutputContentLegth() {
		int out = 0;
		for (int i : historics.get(TAG_OUTPUT)) {
			out += i;
		}
		return out;
	}

	public static int getInputContentLegth() {
		int out = 0;
		for (int i : historics.get(TAG_INPUT)) {
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
