package istat.android.network.utils;

import istat.android.network.http.HttpAsyncQuery;
import istat.android.network.http.HttpQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
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
 * @author Toukea Tatsi (Istat)
 */
public final class ToolKits {
    public static class Text {
        public static boolean isEmpty(String txt) {
            return txt == null || txt.equals("");
        }

        public static String urlEncode(String url) throws UnsupportedEncodingException {
            return java.net.URLEncoder.encode(url, "UTF-8").replace("+", "%20")
                    .replaceAll("%2F", "/");
        }
    }

    public static class Software {
        public static void installApk(Context context, String apkFile) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(apkFile)),
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        public static Boolean isActivityRunning(Context context,
                                                Class<?> activityClass) {
            try {
                ActivityManager activityManager = (ActivityManager) context
                        .getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> tasks = activityManager
                        .getRunningTasks(Integer.MAX_VALUE);

                for (ActivityManager.RunningTaskInfo task : tasks) {
                    if (activityClass.getCanonicalName().equalsIgnoreCase(
                            task.baseActivity.getClassName()))
                        return true;
                }
            } catch (Exception e) {
            }
            return false;
        }

        public static Boolean isActivityTop(Context context,
                                            Class<?> activityClass) {
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager.getRunningTasks(1).get(0).topActivity
                    .getClassName().equals(activityClass.getCanonicalName())) {

                return true;
            }

            return false;
        }

        public static Boolean isProcessRunning(Context context,
                                               String processName) {
            ActivityManager manager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            for (RunningAppProcessInfo process : manager
                    .getRunningAppProcesses()) {
                if (processName.equals(process.processName)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean hasPermission(Context context, String permission) {
            int res = context.checkCallingOrSelfPermission(permission);
            return (res == PackageManager.PERMISSION_GRANTED);
        }

        public static boolean isServiceRunning(Context context,
                                               Class<?> serviceClass) {
            ActivityManager manager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            for (RunningServiceInfo service : manager
                    .getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(
                        service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isUnknowAppEnable(Context context) {
            try {
                return Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public static class Network {
        public static boolean isNetworkConnected(Context context) {
            final ConnectivityManager conMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                return true;
            } else {
                return false;
            }
        }
    }

    // ------------------------------------------------------------------
    public static class Stream {
        public final static int DEFAULT_BUFFER_SIZE = 16384;
        public final static String DEFAULT_ENCODING = "UTF-8";

        public static InputStream merge(List<InputStream> streams) {
            if (streams.isEmpty()) {
                return null;
            }
            InputStream inputStream = streams.get(0);
            if (streams.size() == 1) {
                return inputStream;
            }
            for (int i = 1; i < streams.size(); i++) {
                InputStream stream = streams.get(i);
                if (stream == null) {
                    continue;
                }
                inputStream = new SequenceInputStream(inputStream, stream);
            }
            return inputStream;
        }

        public static InputStream merge(InputStream... streams) {
            if (streams.length == 0) {
                return null;
            }
            InputStream inputStream = streams[0];
            if (streams.length == 1) {
                return inputStream;
            }
            for (int i = 1; i < streams.length; i++) {
                InputStream stream = streams[i];
                if (stream == null) {
                    continue;
                }
                inputStream = new SequenceInputStream(inputStream, stream);
            }
            return inputStream;
        }

        public static String streamToLinearisedString(java.io.InputStream inp,
                                                      String encoding) {
            String out = "";
            StringBuilder total = new StringBuilder();
            String line;
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(
                        inp, encoding));
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                out = total.toString();
                inp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return out;

        }

        public static String streamToString(java.io.InputStream inp) {
            String out = "";
            byte[] b = new byte[DEFAULT_BUFFER_SIZE];
            int read = 0;
            try {
                while ((read = inp.read(b)) > -1) {
                    out = out + new String(b, 0, read);
                }
                inp.close();
            } catch (Exception e) {
            }

            return out;

        }

        public static String streamToString(java.io.InputStream inp, int buffer) {
            return streamToString(inp, buffer, null);
        }

        public static String streamToString(java.io.InputStream inp,
                                            String encoding) {
            return streamToString(inp, DEFAULT_BUFFER_SIZE, encoding);

        }

        public static String streamToString(java.io.InputStream inp,
                                            int buffer, String encoding) {
            String out = "";
            byte[] b = new byte[buffer];
            int read = 0;
            try {
                while ((read = inp.read(b)) > -1) {
                    out = out
                            + (encoding != null ? new String(b, 0, read,
                            encoding) : new String(b, 0, read));
                }
                inp.close();
            } catch (Exception e) {
            }

            return out;

        }

        public static String streamToString(java.io.InputStream inp,
                                            int buffer, String encoding, HttpAsyncQuery asyc) {
            String out = "";
            byte[] b = new byte[buffer];
            int read;
            try {
                while ((read = inp.read(b)) > -1) {
                    if (asyc.isPaused()) {
                        while (asyc.isPaused()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (!asyc.isRunning()) {
                        Log.i("HttpQuery", "streamToString::aborted");
                        break;
                    }
                    out = out
                            + (encoding != null ? new String(b, 0, read,
                            encoding) : new String(b, 0, read));
                }
                inp.close();
            } catch (Exception e) {
            }

            return out;
        }

        public static String streamToString(java.io.InputStream inp,
                                            int buffer, String encoding, HttpQuery<?> mHttp) {
            String out = "";
            byte[] b = new byte[buffer];
            int read = 0;
            try {
                while ((read = inp.read(b)) > -1) {
                    if (mHttp.isAborted()) {
                        Log.i("HttpQuery", "streamToString::aborted");
                        break;
                    }
                    out = out
                            + (encoding != null ? new String(b, 0, read,
                            encoding) : new String(b, 0, read));
                }
                inp.close();
            } catch (Exception e) {
            }

            return out;
        }

        public static OutputStream copyStream(InputStream is, OutputStream os) {
            return copyStream(is, os, DEFAULT_BUFFER_SIZE, 0);
        }

        public static OutputStream copyStream(byte[] prefix, byte[] suffix,
                                              InputStream is, OutputStream os) {
            return copyStream(prefix, suffix, is, os, DEFAULT_BUFFER_SIZE, 0);
        }

        public static OutputStream copyStream(InputStream is, OutputStream os,
                                              int buffer_size) {
            return copyStream(is, os, buffer_size, 0);
        }

        public static OutputStream copyStream(InputStream is, OutputStream os,
                                              int buffer_size, int startByte) {
            return copyStream(null, null, is, os, buffer_size, startByte);
        }

        public static OutputStream copyStream(byte[] prefix, byte[] suffix,
                                              InputStream is, OutputStream os, int buffer_size, int startByte) {
            try {

                byte[] bytes = new byte[buffer_size];

                is.skip(startByte);
                if (prefix != null && prefix.length > 0)
                    os.write(prefix);
                for (; ; ) {
                    // Read byte from input stream

                    int count = is.read(bytes, 0, buffer_size);
                    if (count == -1)
                        break;

                    // Write byte from output stream
                    os.write(bytes, 0, count);
                }
                if (suffix != null && suffix.length > 0)
                    os.write(suffix);
                os.close();
            } catch (Exception ex) {

            }
            return os;
        }

        public static RandomAccessFile copyStream(byte[] prefix, byte[] suffix,
                                                  InputStream is, RandomAccessFile os, int buffer_size,
                                                  int startByte) {
            try {

                byte[] bytes = new byte[buffer_size];

                is.skip(startByte);
                os.seek(startByte);
                if (prefix != null && prefix.length > 0)
                    os.write(prefix);
                for (; ; ) {
                    // Read byte from input stream

                    int count = is.read(bytes, 0, buffer_size);
                    if (count == -1)
                        break;

                    // Write byte from output stream
                    os.write(bytes, 0, count);
                }
                if (suffix != null && suffix.length > 0)
                    os.write(suffix);
                os.close();
            } catch (Exception ex) {
            }
            return os;
        }

        public static OutputStream[] breakStream(int buffer_size,
                                                 int bundleSize, InputStream is, OutputStream... os) {
            try {

                byte[] bytes = new byte[buffer_size];
                int indexCurrentOutput = 0;
                int bytesReadCurrentOutput = 0;
                for (; ; ) {
                    // Read byte from input stream

                    int count = is.read(bytes, 0, buffer_size);
                    bytesReadCurrentOutput += count;
                    if (count == -1) {
                        break;
                    }
                    os[indexCurrentOutput].write(bytes, 0, count);
                    if (bytesReadCurrentOutput >= bundleSize) {
                        indexCurrentOutput++;
                        bytesReadCurrentOutput = 0;
                    }
                    // Write byte from output stream

                }
                os.clone();
            } catch (Exception ex) {
            }
            return os;
        }

        public static OutputStream[] dispatch(int buffer_size, InputStream is, OutputStream... oss) throws IOException {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                // Read byte from input stream
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                for (OutputStream os : oss) {
                    os.write(bytes, 0, count);
                }
            }
            return oss;
        }

        public static InputStream repeat(final byte[] sample, final int times) {
            return new InputStream() {
                private long pos = 0;
                private final long total = (long) sample.length * times;

                public int read() throws IOException {
                    return pos < total ?
                            sample[(int) (pos++ % sample.length)] :
                            -1;
                }
            };
        }
    }

    public static final <T> HashMap<String, Object> toHashMap(T obj, boolean privateAndSuper, boolean acceptStatic, boolean nullable) {
        return toHashMap(obj, privateAndSuper, acceptStatic, nullable, new String[0]);
    }

    public static final <T> void filterPut(T obj, HashMap<Object, Object> map, String fieldName) {
        filterPut(obj, map, fieldName, false);
    }

    public static final <T> void filterPut(T obj, HashMap<Object, Object> map, String fieldName, boolean nullable) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            if (nullable || value != null) {
                map.put(fieldName, value);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static final <T> HashMap<String, Object> toHashMap(T obj,
                                                              boolean privateAndSuper,
                                                              boolean acceptStatic,
                                                              boolean nullable,
                                                              String... ignores) {
        List<String> ignoreList = Arrays.asList(ignores);
        HashMap<String, Object> map = new HashMap<String, Object>();
        List<Field> fields = new ArrayList<Field>();
        Class<?> clazz = obj.getClass();
        if (privateAndSuper) {
            fields.addAll(getAllFieldIncludingPrivateAndSuper(clazz, acceptStatic));
        } else {
            Collections.addAll(fields, clazz.getDeclaredFields());
        }
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            String name = field.getName();
            if (!ignoreList.contains(name)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (nullable || value != null) {
                        map.put(name, value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    public static List<Field> getAllFieldIncludingPrivateAndSuper(Class<?> cLass, boolean acceptStatic) {
        List<Field> fields = new ArrayList<Field>();
        while (!cLass.equals(Object.class)) {
            for (Field field : cLass.getDeclaredFields()) {
                if (field != null && (field.toString().contains("static") && !acceptStatic)) {
                    continue;
                }
                fields.add(field);
            }
            cLass = cLass.getSuperclass();
        }
        return fields;
    }

}
