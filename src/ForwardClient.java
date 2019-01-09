/**
 * Port forwarding client. Forward data
 * between two TCP ports. Based on Nakov TCP Socket Forward Server 
 * and adapted for IK2206.
 *
 * See original copyright notice below.
 * (c) 2018 Peter Sjodin, KTH
 */

/**
 * Nakov TCP Socket Forward Server - freeware
 * Version 1.0 - March, 2002
 * (c) 2001 by Svetlin Nakov - http://www.nakov.com
 */

import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.util.Base64;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.security.PublicKey;
import java.security.PrivateKey;
 
public class ForwardClient {
    public static final int DEFAULTSERVERPORT = 2206;
    public static final String DEFAULTSERVERHOST = "localhost";
    public static final String PROGRAMNAME = "ForwardClient";
    private static Arguments arguments;

	private static PublicKey serverPubKey;
	private static PrivateKey clientPriKey;
	private static String sessionKeyString;
	private static String sessionIVString;
	private static Socket sessionSocket;
	
    private static String serverHost;
    private static int serverPort;

    private void doHandshake() throws Exception {

        /* Connect to forward server */
        System.out.println("Connect to " +  arguments.get("handshakehost") + ":" + Integer.parseInt(arguments.get("handshakeport")));
        Socket hsSocket = new Socket(arguments.get("handshakehost"), Integer.parseInt(arguments.get("handshakeport")));

        /* This is where the handshake should take place */
		Handshake.sendClientHello(hsSocket, arguments.get("usercert"));
		serverPubKey = null;
		while(serverPubKey == null) {
			serverPubKey = Handshake.verifyServerHello(hsSocket, VerifyCertificate.createCert(arguments.get("cacert")));
		}
		
		Handshake.sendForward(hsSocket, arguments.get("targethost"), arguments.get("targetport"));
		HandshakeMessage hsMsg = null;
		while(hsMsg == null) {
			hsMsg = Handshake.verifySession(hsSocket);
			sessionKeyString = Base64.getEncoder().encodeToString(HandshakeCrypto.decrypt(Base64.getDecoder().decode(hsMsg.getParameter("SessionKey")), clientPriKey));
			sessionIVString = Base64.getEncoder().encodeToString(HandshakeCrypto.decrypt(Base64.getDecoder().decode(hsMsg.getParameter("SessionIV")), clientPriKey));
			serverHost = hsMsg.getParameter("ServerHost");
			serverPort = Integer.parseInt(hsMsg.getParameter("ServerPort"));
			sessionSocket = new Socket(serverHost, serverPort);
		}
		
        hsSocket.close();
    }

    /*
     * Let user know that we are waiting
     */
    private static void tellUser(ServerSocket listensocket) throws UnknownHostException {
        System.out.println("Client forwarder to target " + arguments.get("targethost") + ":" + arguments.get("targetport"));
        System.out.println("Waiting for incoming connections at " +
                           listensocket.getInetAddress().getHostAddress() + ":" + listensocket.getLocalPort());
    }
        
    /*
     * Set up client forwarder.
     * Run handshake negotiation, then set up a listening socket and wait for user.
     * When user has connected, start port forwarder thread.
     */
    public void startForwardClient() throws Exception {

        doHandshake();

        // Wait for client. Accept one connection.

        ForwardServerClientThread forwardSCThread;
        ServerSocket c2uListenSocket;
        
        try {
            /* Create a new socket. This is to where the user should connect.
             * ForwardClient sets up port forwarding between this socket
             * and the ServerHost/ServerPort learned from the handshake */
            c2uListenSocket = new ServerSocket();
            /* Let the system pick a port number */
            c2uListenSocket.bind(null); 
            /* Tell the user, so the user knows where to connect */ 
            tellUser(c2uListenSocket);

            Socket c2uSocket = c2uListenSocket.accept();
            Logger.log("Accepted user from " + c2uSocket.getInetAddress().getHostAddress() + ":" + c2uSocket.getPort());
			
			if(c2uSocket == null || sessionSocket == null) {
				System.out.println("Err: can't start from either side");
				return;
			}
            
            forwardSCThread = new ForwardServerClientThread(c2uSocket, sessionSocket);
			forwardSCThread.setSessionParameters(sessionKeyString, sessionIVString);
            forwardSCThread.start();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        } catch(Exception e) {
			e.printStackTrace();
		}
    }
	
    public static void usage() {
        String indent = "";
        System.err.println(indent + "Usage: " + PROGRAMNAME + " options");
        System.err.println(indent + "Where options are:");
        indent += "    ";
        System.err.println(indent + "--targethost=<hostname>");
        System.err.println(indent + "--targetport=<portnumber>");        
        System.err.println(indent + "--handshakehost=<hostname>");
        System.err.println(indent + "--handshakeport=<portnumber>");        
        System.err.println(indent + "--usercert=<filename>");
        System.err.println(indent + "--cacert=<filename>");
        System.err.println(indent + "--key=<filename>");                
    }
    
    /**
     * Program entry point. Reads arguments and run
     * the forward server
     */
    public static void main(String[] args)
    {
		try {
            arguments = new Arguments();
            arguments.setDefault("handshakeport", Integer.toString(DEFAULTSERVERPORT));
            arguments.setDefault("handshakehost", DEFAULTSERVERHOST);
            arguments.loadArguments(args);
            if(arguments.get("targetport") == null || arguments.get("targethost") == null) {
                throw new IllegalArgumentException("Target not specified");
            }
			clientPriKey = HandshakeCrypto.getPrivateKeyFromKeyFile(arguments.get("key"));
        } catch(IllegalArgumentException ex) {
            System.out.println(ex);
            usage();
            System.exit(1);
        }
		
		ForwardClient forwardClient = new ForwardClient();
        try {
            forwardClient.startForwardClient();
        } catch(Exception e) {
			e.printStackTrace();
        }
    }
}
