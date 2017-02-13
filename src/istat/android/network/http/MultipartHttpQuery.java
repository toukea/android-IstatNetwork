package istat.android.network.http;

import android.text.TextUtils;

import istat.android.network.http.interfaces.UpLoadHandler;
import istat.android.network.utils.ToolKits.Stream;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.HashMap;

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
 * @author Toukea Tatsi (Istat)
 */
public class MultipartHttpQuery extends HttpQuery<MultipartHttpQuery> {

    HashMap<String, File> fileParts = new HashMap<String, File>();
    private static final String LINE_FEED = "\n";

    public MultipartHttpQuery addURLParam(String Name, String[] values) {
        for (int i = 0; i < values.length; i++) {
            addURLParam(Name + "[" + i + "]", values[i]);
        }
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
    public InputStream doPost(String url) throws IOException {
        return POST(url, true);
    }

    public InputStream doPost(String url, boolean holdError) throws IOException {
        return POST(url, holdError);
    }

    @Override
    public MultipartHttpQuery addHeader(String name, String value) {
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
        super.clearParams();
        clearParts();
        return this;
    }

    public MultipartHttpQuery clearParts() {
        fileParts.clear();
        return this;
    }

    @Override
    public void setParameterHandler(ParameterHandler parameterHandler) {
        super.setParameterHandler(parameterHandler);
    }

    private HttpURLConnection sendMultipartData(String url)
            throws IOException {
        String method = "POST";
        HttpURLConnection conn = prepareConnexion(url, method);
        conn.setDoOutput(true);
        conn.setRequestMethod(method);
        String data;
        if (!fileParts.isEmpty()) {
            conn.setChunkedStreamingMode(mOptions.chunkedStreamingMode);
        }
        if (!parameters.isEmpty() || !fileParts.isEmpty()) {
            String boundary = createBoundary();

            conn.addRequestProperty("Content-Type",
                    "multipart/form-data, boundary=" + boundary);
            if (TextUtils.isEmpty(conn.getRequestProperty("User-Agent"))) {
                conn.addRequestProperty("User-Agent", "istat.android.network.V2.4.0");
            }
            OutputStream os = conn.getOutputStream();
            DataOutputStream request = new DataOutputStream(os);
            this.currentOutputStream = request;
            if (!parameters.isEmpty()) {
                data = createBoundaryParamsCanvas(boundary, parameters);
                request.writeBytes(data);
            }
            if (!fileParts.isEmpty()) {
                handleFileParts(boundary, request, fileParts);
            }
            boundary = "--" + boundary + "--" + LINE_FEED;
            request.writeBytes(boundary);
            addToOutputHistoric(request.size());
            try {
                request.flush();
                request.close();
                os.flush();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    private String createBoundaryParamsCanvas(String boundary,
                                              HashMap<String, String> params) {
        String data = "";
        if (!params.keySet().isEmpty()) {
            String[] table = new String[params.size()];
            table = params.keySet().toArray(table);
            for (String name : table) {
                if (isAborted()) {
                    currentConnection.disconnect();
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

    private void handleFileParts(String boundary,
                                 DataOutputStream request, HashMap<String, File> params)
            throws IOException {
        boundary = "--" + boundary + "\n";
        if (!params.keySet().isEmpty()) {
            String[] table = new String[params.size()];
            table = params.keySet().toArray(table);
            for (int i = 0; i < table.length; i++) {
                String tmp = table[i];
                if (isAborted()) {
                    currentConnection.disconnect();
                    break;
                }
                if (tmp != null) {
                    File file = params.get(tmp);
                    String data = boundary;
                    String contentType = URLConnection
                            .guessContentTypeFromName(file.getAbsolutePath());
                    if (TextUtils.isEmpty(contentType)) {
                        contentType = "application/octets-stream";
                    }
                    data += "Content-Disposition: form-data; name=\"" + tmp
                            + "\"; filename=\"" + file.getName() + "\"\n";
                    data += "Content-Type: " + contentType + "\n";
                    data += "Content-Transfer-Encoding: binary\n\n";
                    request.writeBytes(data);
                    InputStream stream = new FileInputStream(file);
                    UpLoadHandler uHandler = getUploadHandler();
                    if (uHandler != null) {
                        currentInputStream = stream;
                        uHandler.onUploadStream(this, stream, request);
                    }
                    request.writeBytes("\n");
                    if (i < table.length - 1) {
                        request.writeBytes(boundary);
                    }
                }
            }

        }
    }

    protected synchronized InputStream POST(String url, boolean holdError) throws IOException {
        HttpURLConnection conn = sendMultipartData(url);
        InputStream stream;
        stream = eval(conn, holdError);
        onQueryComplete();
        return stream;
    }

    private static String createBoundary() {
        return "===" + System.currentTimeMillis() + "===";
    }

    public final MultipartHttpQuery addParams(HashMap<?, ?> nameValues) {
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


    @Override
    public boolean hasRunningRequest() {
        super.hasRunningRequest();
        return true;
    }
}
