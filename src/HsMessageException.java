import java.lang.Exception;

public class HsMessageException extends Exception {
	HsMessageException(String msg) {
		System.out.println("Message \"" + msg + "\" undefined or should be received at other phase of Handshake");
		System.out.println("Aborting...");
	}
}