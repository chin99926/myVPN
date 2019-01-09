public class Logger {
    private static final boolean ENABLE_LOGGING = true;
	private static final boolean ENABLE_WARNING = true;
	
    public static void log(String message) {
        if (ENABLE_LOGGING) {
            System.out.println(message);
        }
    }
	
	public static void warn(String message) {
        if (ENABLE_WARNING) {
            System.out.println(message);
        }
    }
}
