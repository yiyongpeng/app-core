package app.core;

/**
 * 运行时访问异常
 * 
 * @author yiyongpeng
 * 
 */
public class AccessException extends RuntimeException {

	public AccessException(String message) {
		super(message);
	}

	public AccessException(Throwable cause) {
		super(cause);
	}

	public AccessException(String message, Throwable cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = 3211887606520343529L;

}
