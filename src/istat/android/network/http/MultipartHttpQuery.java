package istat.android.network.http;

import istat.android.network.util.ToolKits.Stream;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;

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
	protected HashMap<String, String> getParametres = new HashMap<String, String>();
	int uploadBufferSize = Stream.DEFAULT_BUFFER_SIZE;
	private static final String LINE_FEED = "\n";

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

	private HttpURLConnection preparConnexionForPost(final String url)
			throws IOException {
		HttpURLConnection conn = preparConnexion(url);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
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
				String data = createBoundaryingParamsCanvas(boundary,
						parametres);
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
					onFillOutPutStream(request, file);
					request.writeBytes("\n");

				}
			}
			request.writeBytes(boundary);
		}
	}

	protected void onFillOutPutStream(DataOutputStream request, File file)
			throws FileNotFoundException {
		InputStream stream = new FileInputStream(file);
		byte[] b = new byte[uploadBufferSize];
		int read = 0;
		try {
			while ((read = stream.read(b)) > -1) {
				if (isAborted()) {
					stream.close();
					currentConnexion.disconnect();
					break;
				}
				request.write(b, 0, read);
			}
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized InputStream POSTM(String url) throws IOException {
		HttpURLConnection conn = preparConnexionForPost(url);
		InputStream stream = null;
		int responseCode = conn.getResponseCode();
		if (responseCode == HttpsURLConnection.HTTP_OK) {
			stream = eval(conn.getInputStream());
		}
		onQueryComplete();
		return stream;
	}

	@Override
	public MultipartHttpQuery clearParams() {
		// TODO Auto-generated method stub
		super.clearParams();
		clearParts();
		return this;
	}

	public MultipartHttpQuery clearParts() {
		fileParts.clear();
		return this;
	}

	@Override
	public MultipartHttpQuery clearExtraData() {
		// TODO Auto-generated method stub
		super.clearExtraData();
		clearParts();
		return this;
	}

	private static String createBoundary() {
		return /* "AaB03x"; */"===" + System.currentTimeMillis() + "===";
	}

	public void setUploadBufferSize(int uploadBufferSize) {
		this.uploadBufferSize = uploadBufferSize;
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
}
