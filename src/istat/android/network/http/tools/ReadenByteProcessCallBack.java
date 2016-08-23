package istat.android.network.http.tools;

import java.io.InputStream;
import java.net.HttpURLConnection;
import istat.android.network.http.HttpAsyncQuery;
import istat.android.network.http.HttpAsyncQuery.QueryProcessCallBack;
import istat.android.network.util.ToolKits.Stream;

public abstract class ReadenByteProcessCallBack extends
		QueryProcessCallBack<Integer> {
	int buffer = Stream.DEFAULT_BUFFER_SIZE;
	String encoding = Stream.DEFAULT_ENCODING;

	public ReadenByteProcessCallBack() {

	}

	public ReadenByteProcessCallBack(String encoding, int bufferSize) {
		this.encoding = encoding;
		this.buffer = bufferSize;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setBuffer(int buffer) {
		this.buffer = buffer;
	}

	public int getBuffer() {
		return buffer;
	}

//	/**
//	 * @param vars
//	 *            un tableaqu d'entier representant la progression.
//	 *            vars[0]=nombre de byte deja lu, vars[1]=nobre total de byte a
//	 *            lire, vars[2] le pourcentage deja lue
//	 */
//	@Override
//	public abstract void onUpdateQueryProcess(HttpAsyncQuery query,
//			Integer... vars);

	@Override
	public String onBuildResponseBody(HttpURLConnection currentConnexion,
			InputStream inp, HttpAsyncQuery query) {
		// TODO Auto-generated method stub
		String out = "";
		byte[] b = new byte[buffer];
		int read = 0;
		int totalReaden = 0;
		int streamSize = currentConnexion == null ? 0 : currentConnexion
				.getContentLength();
		try {
			streamSize = streamSize == 0 ? inp.available() : streamSize;
			while ((read = inp.read(b)) > -1) {

				if (query.isCancelled()) {
					return null;
				}
				out += (encoding != null ? new String(b, 0, read, encoding)
						: new String(b, 0, read));
				totalReaden += read;
				publishProgression(totalReaden, streamSize,
						streamSize > 0 ? (100 * totalReaden / streamSize) : -1);
			}
			inp.close();
		} catch (Exception e) {
		}

		return out;
	}

}
