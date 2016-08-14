package istat.android.network.http;

import istat.android.network.util.ToolKits.Stream;
import istat.android.network.util.ToolKits.Text;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.message.BasicNameValuePair;

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
public class MultipartHttpQuery extends HttpQuery<MultipartHttpQuery> {

	HashMap<String, File> fileParts = new HashMap<String, File>();
	protected HashMap<String, String> URLParametres = new HashMap<String, String>();
	int uploadBufferSize = Stream.DEFAULT_BUFFER_SIZE;
	private static final String LINE_FEED = "\n";

	public MultipartHttpQuery addURLParam(String Name, String Value) {
		URLParametres.put(Name, Value);
		return this;
	}

	public MultipartHttpQuery addURLParam(String Name, String[] values) {
		for (int i = 0; i < values.length; i++) {
			addURLParam(Name + "[" + i + "]", values[i]);
		}
		return this;
	}

	public MultipartHttpQuery addURLParams(List<BasicNameValuePair> nameValues) {
		for (BasicNameValuePair pair : nameValues)
			addURLParam(pair.getName(), pair.getValue());
		return this;
	}

	public MultipartHttpQuery addURLParams(HashMap<String, Object> nameValues) {
		if (!nameValues.keySet().isEmpty()) {
			String[] table = new String[nameValues.size()];
			table = nameValues.keySet().toArray(table);
			for (String tmp : table) {
				if (tmp != null) {
					addURLParam(tmp, nameValues.get(tmp).toString());
				}
			}
		}
		return this;
	}

	@Override
	public void removeParam(String name) {
		// TODO Auto-generated method stub
		super.removeParam(name);
		if (URLParametres != null && URLParametres.containsKey(name)) {
			URLParametres.clear();
		}
	}

	@Override
	public InputStream doPost(String url) throws IOException {
		// TODO Auto-generated method stub
		return POSTM(url);
	}

	@Override
	public MultipartHttpQuery addHeader(String name, String value) {
		// TODO Auto-generated method stub
		super.addHeader(name, value);
		return this;
	}

	public MultipartHttpQuery addFilePart(String name, File file) {
		fileParts.put(name, file);
		return this;
	}

	public MultipartHttpQuery addFilePart(String name, String file) {
		addFilePart(name, new File(file));
		return this;
	}

	@Override
	public MultipartHttpQuery clearParams() {
		// TODO Auto-generated method stub
		super.clearParams();
		clearParts();
		URLParametres.clear();
		return this;
	}

	public MultipartHttpQuery clearParts() {
		fileParts.clear();
		return this;
	}

	public void setUploadBufferSize(int uploadBufferSize) {
		this.uploadBufferSize = uploadBufferSize;
	}

	@Override
	public void setParameterHandler(
			istat.android.network.http.HttpQuery.ParameterHandler parameterHandler) {
		// TODO Auto-generated method stub
		super.setParameterHandler(parameterHandler);
	}

	private HttpURLConnection preparConnexionForPost(String url)
			throws IOException {
		String method = "POST";
		HttpURLConnection conn = preparConnexion(url);
		conn.setDoOutput(true);
		conn.setRequestMethod(method);
		String data = "";
		if (parameterHandler != null) {
			data = parameterHandler.onStringifyQueryParams(method, parametres,
					mOptions.encoding);
		}
		if (!Text.isEmpty(data)) {
			url += (url.contains("?") ? "" : "?") + data;
		}
		if (!parametres.isEmpty() || !fileParts.isEmpty()) {
			String boundary = createBoundary();

			conn.addRequestProperty("Content-Type",
					"multipart/form-data, boundary=" + boundary);
			conn.addRequestProperty("User-Agent", "istat_java_agent");
			// int dataLength = 0; conn.addRequestProperty("Content-Length", ""
			// + dataLength);
			OutputStream os = conn.getOutputStream();
			DataOutputStream request = new DataOutputStream(os);
			if (!parametres.isEmpty()) {
				data = createBoundaryingParamsCanvas(boundary, parametres);
				request.writeBytes(data);
			}
			if (!fileParts.isEmpty()) {
				fillStreamWithFileParts(boundary, request, fileParts);
			}
			boundary = "--" + boundary + "--" + LINE_FEED;
			request.writeBytes(boundary);
			addToOutputHistoric(request.size());
			request.flush();
			request.close();
			os.close();
		}
		return conn;
	}

	private String createBoundaryingParamsCanvas(String boundary,
			HashMap<String, String> params) {
		String data = "";
		if (!params.keySet().isEmpty()) {
			String[] table = new String[params.size()];
			table = params.keySet().toArray(table);
			for (String name : table) {
				if (isAborted()) {
					currentConnexion.disconnect();
					break;
				}
				if (name != null) {
					String value = params.get(name);
					data += "--" + boundary + LINE_FEED;
					data += "content-disposition: form-data; name=\"" + name
							+ "\"" + LINE_FEED;
					data += "Content-Type: text/plain; charset="
							+ mOptions.encoding + LINE_FEED;
					data += LINE_FEED;
					data += value + LINE_FEED;
				}
			}
		}
		System.out.println(data);
		return data;
	}

	private void fillStreamWithFileParts(String boundary,
			DataOutputStream request, HashMap<String, File> params)
			throws IOException {
		boundary = "--" + boundary + "\n";
		if (!params.keySet().isEmpty()) {
			String[] table = new String[params.size()];
			table = params.keySet().toArray(table);
			for (String tmp : table) {
				if (isAborted()) {
					currentConnexion.disconnect();
					break;
				}
				if (tmp != null) {
					File file = params.get(tmp);
					String data = boundary;
					data += "Content-Disposition: form-data; name=\"" + tmp
							+ "\"; filename=\"" + file.getName() + "\"\n";
					data += "Content-Type: application/data\n";
					data += "Content-Transfer-Encoding: binary\n\n";
					request.writeBytes(data);
					InputStream stream = new FileInputStream(file);
					if (uploadHandler != null) {
						uploadHandler.onProceedStreamUpload(this, request,
								stream);
					}
					request.writeBytes("\n");
				}
			}
			request.writeBytes(boundary);
		}
	}

	private synchronized InputStream POSTM(String url) throws IOException {
		HttpURLConnection conn = preparConnexionForPost(url);
		InputStream stream = null;
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpsURLConnection.HTTP_OK) {
			stream = eval(conn);
		}
		onQueryComplete();
		return stream;
	}

	private static String createBoundary() {
		return /* "AaB03x"; */"===" + System.currentTimeMillis() + "===";
	}

	public final MultipartHttpQuery addParams(HashMap<String, Object> nameValues) {
		if (!nameValues.keySet().isEmpty()) {
			String[] table = new String[nameValues.size()];
			table = nameValues.keySet().toArray(table);
			for (String tmp : table) {

				if (tmp != null) {
					Object value = nameValues.get(tmp);
					if (value instanceof File) {
						addFilePart(tmp, (File) value);
					} else {
						addParam(tmp, nameValues.get(tmp).toString());
					}
				}
			}
		}
		return this;
	}

	public void setUploadHandler(UpLoadHandler uploadHandler) {
		if (uploadHandler != null) {
			this.uploadHandler = uploadHandler;
		}
	}

	UpLoadHandler uploadHandler = new UpLoadHandler() {

		@Override
		public void onProceedStreamUpload(MultipartHttpQuery httpQuery,
				DataOutputStream request, InputStream stream)
				throws IOException {
			// TODO Auto-generated method stub
			byte[] b = new byte[uploadBufferSize];
			int read = 0;
			while ((read = stream.read(b)) > -1) {
				if (httpQuery.isAborted()) {
					stream.close();
					currentConnexion.disconnect();
					break;
				}
				request.write(b, 0, read);
			}
			stream.close();
		}
	};

	public interface UpLoadHandler {
		public void onProceedStreamUpload(MultipartHttpQuery httpQuery,
				DataOutputStream request, InputStream stream)
				throws IOException;
	}
}
