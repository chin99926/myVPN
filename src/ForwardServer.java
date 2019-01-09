/**
 * Port forwarding server. Forward data
 * between two TCP ports. Based on Nakov TCP Socket Forward Server 
 * and adapted for IK2206.
 *
 * Original copyright notice below.
 * (c) 2018 Peter Sjodin, KTH
 */

/**
 * Nakov TCP Socket Forward Server - freeware
 * Version 1.0 - March, 2002
 * (c) 2001 by Svetlin Nakov - http://www.nakov.com
 */

import java.lang.Integer;
import java.util.Base64;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import java.security.*;
 
public class ForwardServer
{
    public static final int DEFAULTSERVERPORT = 2206;
    public static final String DEFAULTSERVERHOST = "localhost";
    public static final String PROGRAMNAME = "ForwardServer";
	public static final int KEYLENGTH = 128;
    private static Arguments arguments;

    private static ServerSocket hsServerSocket;
	private static PublicKey clientPubKey;
	private static String sessionKeyString;
	private static String sessionIvString;
	private static Socket sessionSocket;
    
    private static ServerSocket listenSocket;
    private static String targetHost;
    private static int targetPort;
    
    /**
     * Do handshake negotiation with client to authenticate, learn 
     * target host/port, etc.
     */
    private void doHandshake() throws UnknownHostException, IOException, Exception {
        Socket hsSocket = hsServerSocket.accept();
        Logger.log("Incoming handshake connection from " + hsSocket.getInetAddress().getHostAddress() + ":" + hsSocket.getPort());

        /** This is where the handshake should take place */
		clientPubKey = null;
		while(clientPubKey == null) {
			clientPubKey = Handshake.verifyClientHello(hsSocket, VerifyCertificate.createCert(arguments.get("cacert")));
		}
		Handshake.sendServerHello(hsSocket, arguments.get("usercert"));
		
		HandshakeMessage hsMsg = null;
		while(hsMsg == null) {
			hsMsg = Handshake.verifyForward(hsSocket);
			targetHost = hsMsg.getParameter("TargetHost");
			targetPort = Integer.parseInt(hsMsg.getParameter("TargetPort"));
		}
		SessionEncrypter ssnEncrypter = new SessionEncrypter(KEYLENGTH);
		sessionKeyString = ssnEncrypter.encodeKey();
		sessionIvString = ssnEncrypter.encodeIV();
		listenSocket = Handshake.sendSession(hsSocket, Base64.getEncoder().encodeToString(HandshakeCrypto.encrypt(Base64.getDecoder().decode(sessionKeyString), clientPubKey)),
													   Base64.getEncoder().encodeToString(HandshakeCrypto.encrypt(Base64.getDecoder().decode(sessionIvString), clientPubKey)));
		sessionSocket = listenSocket.accept();
		
        hsSocket.close();     
    }

    /*
     * Starts the forward server - binds on a given port and starts serving
     */
    public void startForwardServer() throws IOException {
         // Bind server on given TCP port
        int port = Integer.parseInt(arguments.get("handshakeport"));
        try {
            hsServerSocket = new ServerSocket(port);
        } catch (IOException ioe) {
			ioe.printStackTrace();
			throw new IOException("Unable to bind to port " + port);
        }

        Logger.log("Nakov Forward Server started on TCP port " + port);
		
		ForwardServerClientThread forwardSCThread;
		Socket s2tSocket = new Socket();
		
		try {
			doHandshake();
			
			s2tSocket.connect(new InetSocketAddress(targetHost, targetPort));
			forwardSCThread = new ForwardServerClientThread(s2tSocket, sessionSocket);
			forwardSCThread.setSessionParameters(sessionKeyString, sessionIvString);
			forwardSCThread.start();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
 
    public static void usage() {
        String indent = "";
        System.err.println(indent + "Usage: " + PROGRAMNAME + " options");
        System.err.println(indent + "Where options are:");
        indent += "    ";
        System.err.println(indent + "--handshakehost=<hostname>");
        System.err.println(indent + "--handshakeport=<portnumber>");        
        System.err.println(indent + "--usercert=<filename>");
        System.err.println(indent + "--cacert=<filename>");
        System.err.println(indent + "--key=<filename>");                
    }
    
    /**
     * Program entry point. Reads settings, starts check-alive thread and
     * the forward server
     */
    public static void main(String[] args) throws Exception {
		// 20190105: remove parameter number check...
		arguments = new Arguments();
        arguments.setDefault("handshakeport", Integer.toString(DEFAULTSERVERPORT));
        arguments.setDefault("handshakehost", DEFAULTSERVERHOST);
        arguments.loadArguments(args);
        
        ForwardServer forwardServer = new ForwardServer();
        try {
           forwardServer.startForwardServer();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
 
}