package istat.android.network.utils;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
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
public class SocketQuery {
	// ---------------------------------------------------------------------------------------------------
	public static String getResponsToString(String serveur, int port,
			String Query) throws IOException {
		Socket socketClient = new Socket(serveur, port);
		BufferedWriter bufferedWriter = new BufferedWriter(
				new OutputStreamWriter(socketClient.getOutputStream()));
		InputStream inputStream = socketClient.getInputStream();
		bufferedWriter.write(Query + "\n");
		Log.i("RestConnect getRestConnectFromQuery", "send Query");
		bufferedWriter.flush();
		Log.i("RestConnect getRestConnectFromQuery", "wait for respons");
		socketClient.shutdownOutput();
		return ToolKits.Stream.streamToString(inputStream);
	}

	public static InputStream getResponsToStream(String serveur, int port,
			String Query) throws IOException {
		Socket socketClient = new Socket(serveur, port);
		BufferedWriter bufferedWriter = new BufferedWriter(
				new OutputStreamWriter(socketClient.getOutputStream()));
		InputStream inputStream = socketClient.getInputStream();
		bufferedWriter.write(Query + "\n");
		Log.i("RestConnect getRestConnectFromQuery", "send Query");
		bufferedWriter.flush();
		Log.i("RestConnect getRestConnectFromQuery", "wait for respons");
		socketClient.shutdownOutput();
		return inputStream;
	}

	// -----------------------------------------------------------------------------------------------
	public static String SendStream(String serveur, int port, InputStream in)
			throws IOException {
		Socket socketClient = new Socket(serveur, port);
		OutputStream Bout = (socketClient.getOutputStream());
		// BufferedWriter Bout = new BufferedWriter(new
		// OutputStreamWriter(socketClient.getOutputStream()));
		try {
			ToolKits.Stream.copyStream(in, Bout);
		} catch (Exception e) {
			Log.i("RestConnect getRestConnectFromQuery", "Fatal error: " + e);
		}
		try {
			in.close();
		} catch (Exception e) {
			Log.i("RestConnect getRestConnectFromQuery", "Input close error: "
					+ e);
		}
		Log.i("RestConnect getRestConnectFromQuery", "send Query");
		Bout.flush();
		Log.i("RestConnect getRestConnectFromQuery", "wait for respons");
		InputStream inputStream = socketClient.getInputStream();
		socketClient.shutdownOutput();
		return ToolKits.Stream.streamToString(inputStream);
	}

	public static String SendStream(String serveur, int port, String post,
			InputStream in) throws IOException {
		Socket socketClient = new Socket(serveur, port);
		OutputStream Bout = (socketClient.getOutputStream());
		// BufferedWriter Bout = new BufferedWriter(new
		// OutputStreamWriter(socketClient.getOutputStream()));
		try {
			ToolKits.Stream.copyStream(post.getBytes(),null, in, Bout);
		} catch (Exception e) {
			Log.i("RestConnect getRestConnectFromQuery", "Fatal error: " + e);
		}
		try {
			in.close();
		} catch (Exception e) {
			Log.i("RestConnect getRestConnectFromQuery", "Input close error: "
					+ e);
		}
		Log.i("RestConnect getRestConnectFromQuery", "send Query");
		Bout.flush();
		Log.i("RestConnect getRestConnectFromQuery", "wait for respons");
		InputStream inputStream = socketClient.getInputStream();
		socketClient.shutdownOutput();
		return ToolKits.Stream.streamToString(inputStream);
	}

	public static String SendStream(String url, InputStream in)
			throws IOException {
		URL Url = new URL(url);
		Socket socketClient = new Socket(Url.getHost(), Url.getPort());
		OutputStream Bout = (socketClient.getOutputStream());
		try {
			ToolKits.Stream.copyStream(("POST " + Url.getPath()
					+ " /HTTP1.1\n").getBytes(),null, in, Bout);
		} catch (Exception e) {
			Log.i("RestConnect getRestConnectFromQuery", "Fatal error: " + e);
		}
		try {
			in.close();
		} catch (Exception e) {
			Log.i("RestConnect getRestConnectFromQuery", "Input close error: "
					+ e);
		}
		Log.i("RestConnect getRestConnectFromQuery", "send Query");
		Bout.flush();
		Log.i("RestConnect getRestConnectFromQuery", "wait for respons");
		InputStream inputStream = socketClient.getInputStream();
		socketClient.shutdownOutput();
		return ToolKits.Stream.streamToString(inputStream);
		// return "ok";
	}

}
