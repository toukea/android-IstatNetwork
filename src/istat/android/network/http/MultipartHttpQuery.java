package istat.android.network.http;

import android.text.TextUtils;

import istat.android.network.utils.ToolKits;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class MultipartHttpQuery extends ParameterHttpQuery<MultipartHttpQuery> {

    HashMap<String, File> fileParts = new HashMap<String, File>();
    private static final String LINE_FEED = "\r\n";

    @Override
    public void setParameterHandler(ParameterHandler parameterHandler) {
        super.setParameterHandler(parameterHandler);
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

    private HttpURLConnection sendMultipartData(String url)
            throws IOException {
        String method = "POST";
        HttpURLConnection conn = prepareConnection(url, method);
        conn.setDoOutput(true);
        conn.setRequestMethod(method);
        String data;
        String encoding = getOptions().encoding;
        if (!fileParts.isEmpty()) {
            conn.setChunkedStreamingMode(mOptions.chunkedStreamingMode);
        }
        if (!parameters.isEmpty() || !fileParts.isEmpty()) {
            String boundary = createBoundary();
            conn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            if (TextUtils.isEmpty(conn.getRequestProperty("User-Agent"))) {
                conn.addRequestProperty("User-Agent", "istat.android.network.V2.4.0");
            }
            long size = 0;
            InputStream stringDataInputStream = null;
            InputStream filePartInputStream = null;
            if (!parameters.isEmpty()) {
                data = createBoundaryParamsCanvas(boundary, parameters);
                stringDataInputStream = new ByteArrayInputStream(data.getBytes(encoding));
                size += data.length();
            }
            if (!fileParts.isEmpty()) {
                Map.Entry<Long, InputStream> sizeStream = handleFileParts(boundary, fileParts);
                if (sizeStream != null) {
                    size += sizeStream.getKey();
                    filePartInputStream = sizeStream.getValue();
                }
            }
            boundary = "--" + boundary + "--" + LINE_FEED;
            size += boundary.length();
            ByteArrayInputStream endSegmentDataInputStream = new ByteArrayInputStream(boundary.getBytes(encoding));
            InputStream multipartInputStream = ToolKits.Stream.merge(stringDataInputStream, filePartInputStream, endSegmentDataInputStream);
            //      String summary = ToolKits.Stream.streamToString(multipartInputStream);
            //       System.out.println(summary);
            OutputStream os = conn.getOutputStream();
            this.currentOutputStream = os;
            getUploadHandler().onUploadStream(size, multipartInputStream, os);
            this.currentInputStream = multipartInputStream;
            addToOutputHistoric(size);
            try {
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
                    data += "Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED;
                    data += "Content-Type: text/plain; charset=" + mOptions.encoding + LINE_FEED;
                    data += LINE_FEED;
                    data += value + LINE_FEED;
                }
            }
        }
        return data;
    }

    private Map.Entry<Long, InputStream> handleFileParts(String boundary,
                                                         HashMap<String, File> params)
            throws IOException {
        String encoding = getOptions().getEncoding();
        boundary = "--" + boundary + LINE_FEED;
        if (!params.keySet().isEmpty()) {
            String[] table = new String[params.size()];
            table = params.keySet().toArray(table);
            InputStream inputStream = null;
            int size = 0;
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
                    data += "Content-Disposition: form-data; name=\"" + tmp + "\"; filename=\"" + file.getName() + "\"" + LINE_FEED;
                    data += "Content-Type: " + contentType + LINE_FEED;
                    data += "Content-Transfer-Encoding: binary" + LINE_FEED;
                    data += LINE_FEED;
                    final ByteArrayInputStream dataInputStream = new ByteArrayInputStream(data.getBytes(encoding));
                    final InputStream fileInputStream = new FileInputStream(file);
                    String returnCharSequence = LINE_FEED;
                    final ByteArrayInputStream returnInputStream = new ByteArrayInputStream(returnCharSequence.getBytes(encoding));
                    size += data.length();
                    size += file.length();
                    size += returnCharSequence.length();
                    List<InputStream> streams = new ArrayList<InputStream>() {
                        {

                            add(dataInputStream);
                            add(fileInputStream);
                            add(returnInputStream);
                        }
                    };
                    if (inputStream != null) {
                        streams.add(0, inputStream);
                    }
                    inputStream = ToolKits.Stream.merge(streams);
                }
            }
            final Long finalSize = new Long(size);
            final InputStream finalStream = inputStream;
            return new Map.Entry<Long, InputStream>() {
                @Override
                public Long getKey() {
                    return finalSize;
                }

                @Override
                public InputStream getValue() {
                    return finalStream;
                }

                @Override
                public InputStream setValue(InputStream object) {
                    return null;
                }
            };
        }
        return null;
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
}
