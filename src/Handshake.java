import java.io.*;
import java.lang.*;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.util.Base64;

import java.security.*;
import java.security.cert.*;

public class Handshake {
    /* Static data - default setup */

    /* Where the client forwarder forwards data from - default as specified */
    private static String serverHost = "localhost";
    private static int serverPort = 4412;    
	
    /* The final destination - default as specified
     * private static String targetHost = "localhost";
     * private static int targetPort = 6789;
	 */
	private String targetHost;
	private int targetPort;
	
	public static String getCertificateAsString(String filename) throws Exception {
		CertificateFactory cfactory = CertificateFactory.getInstance("X.509");
		
		try {
			FileInputStream fis = new FileInputStream(filename);
			X509Certificate x509cert = (X509Certificate)cfactory.generateCertificate(fis);
			return Base64.getEncoder().encodeToString(x509cert.getEncoded());
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch(CertificateException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void sendClientHello(Socket clientSocket, String clientCert) throws Exception {
		HandshakeMessage hsMsg = new HandshakeMessage();
		hsMsg.putParameter("MessageType", "ClientHello");
		hsMsg.putParameter("Certificate", getCertificateAsString(clientCert));
		
		try {
			hsMsg.send(clientSocket);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static PublicKey verifyClientHello(Socket serverSocket, X509Certificate caCert) throws Exception {
		HandshakeMessage hsMsg = new HandshakeMessage();
		hsMsg.recv(serverSocket);
		
		if(hsMsg.getParameter("MessageType").equals("ClientHello")) {
			byte[] certBytes = Base64.getDecoder().decode(hsMsg.getParameter("Certificate"));
			
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(certBytes);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate clientCert = (X509Certificate)cf.generateCertificate(bis);
				
				clientCert.verify(caCert.getPublicKey());
				return clientCert.getPublicKey();
			} catch(CertificateException e) {
				e.printStackTrace();
				return null;
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		} else return null;
	}
	
	public static void sendServerHello(Socket serverSocket, String serverCert) throws Exception {
		HandshakeMessage hsMsg = new HandshakeMessage();
		hsMsg.putParameter("MessageType", "ServerHello");
		hsMsg.putParameter("Certificate", getCertificateAsString(serverCert));
		
		try {
			hsMsg.send(serverSocket);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static PublicKey verifyServerHello(Socket clientSocket, X509Certificate caCert) throws Exception {
		HandshakeMessage hsMsg = new HandshakeMessage();
		hsMsg.recv(clientSocket);
		
		if(hsMsg.getParameter("MessageType").equals("ServerHello")) {
			byte[] certBytes = Base64.getDecoder().decode(hsMsg.getParameter("Certificate"));
			
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(certBytes);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate serverCert = (X509Certificate)cf.generateCertificate(bis);
				
				serverCert.verify(caCert.getPublicKey());
				return serverCert.getPublicKey();
			} catch(CertificateException e) {
				e.printStackTrace();
				return null;
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		} else return null;
	}
	
	public static void sendForward(Socket clientSocket, String targetHost, String targetPort) throws Exception {
		HandshakeMessage hsMsg = new HandshakeMessage();
		hsMsg.putParameter("MessageType", "Forward");
		hsMsg.putParameter("TargetHost", targetHost);
		hsMsg.putParameter("TargetPort", targetPort);
		
		try {
			hsMsg.send(clientSocket);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static HandshakeMessage verifyForward(Socket serverSocket) throws Exception {
		HandshakeMessage hsMsg = new HandshakeMessage();
		hsMsg.recv(serverSocket);
		
		if(hsMsg.getParameter("MessageType").equals("Forward")) {
			return hsMsg;
		} else return null;
	}
	
	public static ServerSocket sendSession(Socket serverSocket, String encSessionKey, String encSessionIV) throws Exception {
		HandshakeMessage hsMsg = new HandshakeMessage();
		hsMsg.putParameter("MessageType", "Session");
		hsMsg.putParameter("SessionKey", encSessionKey);
		hsMsg.putParameter("SessionIV", encSessionIV);
		
		ServerSocket listenSocket = new ServerSocket();
		listenSocket.bind(new InetSocketAddress(serverHost, serverPort));
		
		hsMsg.putParameter("ServerHost", serverHost);
		hsMsg.putParameter("ServerPort", Integer.toString(serverPort));
		
		try {
			hsMsg.send(serverSocket);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return listenSocket;
	}
	
	public static HandshakeMessage verifySession(Socket clientSocket) throws Exception {
		HandshakeMessage hsMsg = new HandshakeMessage();
		hsMsg.recv(clientSocket);
		
		if(hsMsg.getParameter("MessageType").equals("Session")) {
			return hsMsg;
		} else return null;
	}
}
