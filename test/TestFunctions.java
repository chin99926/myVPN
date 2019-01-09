import java.io.*;
import java.security.*;
import java.security.cert.*;

import java.util.Base64;
import java.net.Socket;
import java.net.InetSocketAddress;

public class TestFunctions {
	public static void test(SessionEncrypter se) throws Exception {
		se = new SessionEncrypter(128);
	}
	
	public static void main(String[] args) throws Exception {
		SessionEncrypter se = null;
		test(se);
		System.out.println(se.encodeIV());
	}
}