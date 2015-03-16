package app.util;

/**
 * 服务端进程运行模式
 * 
 * @author yiyongpeng
 * 
 */
public class ServerMode {
	public static final int MODE_NORMAL = 0, MODE_TEST = 1, MODE_DEBUG = 2;

	private static int mode = Integer.parseInt(System.getProperty("mode",
			String.valueOf(MODE_DEBUG)));

	public static void setMode(int mode) {
		ServerMode.mode = mode;
	}

	public static boolean isDebug() {
		return mode == MODE_DEBUG;
	}

	public static boolean isNormal() {
		return mode == MODE_NORMAL;
	}

	public static boolean isTest() {
		return mode == MODE_TEST;
	}
}
