package istat.android.network.http;

public class HttpQueryException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int code = 0;

	public int getCode() {
		return code;
	}

	public HttpQueryException(int code, String message) {
		super(message);
		this.code = code;
	}

	public HttpQueryException(Exception e) {
		super(e);
		if (e instanceof HttpQueryException) {
			this.code = ((HttpQueryException) e).getCode();
		}
	}

	@Override
	public String getMessage() {
		return code + " : " + super.getMessage();
	}
}