/**
 * ForwardServerClientThread handles the clients of Nakov Forward Server. It
 * connects two sockets and starts the TCP forwarding between given client
 * and its assigned server. After the forwarding is failed and the two threads
 * are stopped, closes the sockets.
 *
 */

/**
 * Modifications for IK2206:
 * - Server pool removed
 * - Two variants - client connects to listening socket or client is already connected
 *
 * Peter Sjodin, KTH
 */

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;

public class ForwardServerClientThread extends Thread
{
    // private ForwardClient mForwardClient = null;
    private Socket plainSocket = null;
    private Socket cipherSocket = null;
    private boolean mBothConnectionsAreAlive = false;
	
	private SessionEncrypter sessionEncrypter;
	private SessionDecrypter sessionDecrypter;
    
    /**
     * Creates a client thread for handling clients of NakovForwardServer.
     * A client socket should be connected and passed to this constructor.
     * A server socket is created later by run() method.
     */
    public ForwardServerClientThread(Socket pSocket, Socket cSocket) {
        plainSocket = pSocket;
		cipherSocket = cSocket;
    }
	
	public void setSessionParameters(String sssKeyString, String sssIVString) throws Exception {
		sessionEncrypter = new SessionEncrypter(sssKeyString, sssIVString);
		sessionDecrypter = new SessionDecrypter(sssKeyString, sssIVString);
	}

    /**
     * Obtains a destination server socket to some of the servers in the list.
     * Starts two threads for forwarding : "client in <--> dest server out" and
     * "dest server in <--> client out", waits until one of these threads stop
     * due to read/write failure or connection closure. Closes opened connections.
     * 
     * If there is a listen socket, first wait for incoming connection
     * on the listen socket.
     */
    public void run()
    {
        try {
           // Obtain input and output streams of server and client
           InputStream plainIn = plainSocket.getInputStream();
           OutputStream plainOut = plainSocket.getOutputStream();
           CipherInputStream cipherIn = sessionDecrypter.openCipherInputStream(cipherSocket.getInputStream());
           CipherOutputStream cipherOut = sessionEncrypter.openCipherOutputStream(cipherSocket.getOutputStream());

		   /*
            * serverHostPort = mServerHost + ":" + mServerPort;
            * Logger.log("TCP Forwarding " + clientHostPort + " <--> " + serverHostPort + " started.");
			*/
			
		   Logger.log("TCP Forwarding  " + plainSocket.getInetAddress().getHostAddress() + ":" + plainSocket.getPort() +
					  " <--> " + plainSocket.getLocalAddress().getHostAddress() + ":" + plainSocket.getLocalPort() + "  started.");
		   
           // Start forwarding of socket data between server and client
           ForwardThread forward2Tunnel = new ForwardThread(this, plainIn, cipherOut);
           ForwardThread forward2End = new ForwardThread(this, cipherIn, plainOut);
           mBothConnectionsAreAlive = true;
           forward2Tunnel.start();
           forward2End.start();
        } catch(IOException ioe) {
			ioe.printStackTrace();
        } catch(Exception e) {
			e.printStackTrace();
		}
    }
 
    /**
     * connectionBroken() method is called by forwarding child threads to notify
     * this thread (their parent thread) that one of the connections (server or client)
     * is broken (a read/write failure occured). This method disconnects both server
     * and client sockets causing both threads to stop forwarding.
     */
    public synchronized void connectionBroken()
    {
        if (mBothConnectionsAreAlive) {
           // One of the connections is broken. Close the other connection and stop forwarding
           // Closing these socket connections will close their input/output streams
           // and that way will stop the threads that read from these streams
           try { plainSocket.close(); } catch (IOException e) {}
           try { cipherSocket.close(); } catch (IOException e) {}
 
           mBothConnectionsAreAlive = false;
 
           Logger.log("TCP Forwarding  " + plainSocket.getInetAddress().getHostAddress() + ":" + plainSocket.getPort() +
					  " <--> " + plainSocket.getLocalAddress().getHostAddress() + ":" + plainSocket.getLocalPort() + "  stopped.");
        }
    }
}
