import java.io.*;
import java.net.*;
import java.lang.*;

import java.util.Scanner;

public class User {
	public static void main(String[] args) throws Exception {
		Socket s = new Socket("localhost", Integer.parseInt(args[0]));
		Scanner scanner = new Scanner(System.in).useDelimiter("\n");
		InputStream in;
		OutputStream out;
		
		while(true) {
			in = s.getInputStream();
			out = s.getOutputStream();
			
			byte[] inBuffer = new byte[100];
			try {
				while (true) {
					String str = scanner.next();
					out.write(str.getBytes());
					
					int bytesRead = in.read(inBuffer);
					if (bytesRead == -1)
						break; // End of stream is reached --> exit the thread
					System.out.println(new String(inBuffer, 0, bytesRead));
				}
			} catch (IOException e) {
				// Read/write failed --> connection is broken --> exit the thread
			}
		}
	}
}