import java.io.*;
import java.net.*;
import java.lang.*;

public class Target {
	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(6789);
		InputStream in;
		OutputStream out;
		
		while(true) {
			Socket s = ss.accept();
			in = s.getInputStream();
			out = s.getOutputStream();
			
			byte[] inBuffer = new byte[8192];
			byte[] outBuffer = "Hi".getBytes();
			try {
				while (true) {
					int bytesRead = in.read(inBuffer);
					if (bytesRead == -1)
						break; // End of stream is reached --> exit the thread
					System.out.println(new String(inBuffer, 0, bytesRead));
					
					out.write(outBuffer);
				}
			} catch (IOException e) {
				// Read/write failed --> connection is broken --> exit the thread
			}
		}
	}
}