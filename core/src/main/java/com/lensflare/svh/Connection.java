package com.lensflare.svh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * The {@code Connection} interface represents an active connection by a user.
 * Classes that implement this interface are expected to implement a public
 * constructor with the same signature as
 * {@link com.lensflare.svh.impl.Connection#Connection(Server, Socket)}.
 * <hr /><span style="font-weight:bold">YAML</span><br />
 * This interface currently has no corresponding YAML section.
 * <hr />
 * @author firelizzard
 *
 */
public interface Connection {
	/**
	 * @return the incoming socket
	 */
	Socket getSocket();
	
	/**
	 * @return the outgoing socket (can be null)
	 */
	Socket getTarget();
	
	/**
	 * Retrieves incoming data from the connection.
	 * @return the data
	 * @throw IOException {@link InputStream#read(byte[])}
	 */
	byte[] getData() throws IOException;
	
	/**
	 * Returns data that has been retrieved but not sent.
	 * @param index the index on the stack
	 * @return the data
	 */
	byte[] getUnsentData(int index);
	
	/**
	 * Forwards any unsent data to the specified socket
	 * @param socket the socket
	 * @throw IOException {@link OutputStream#write(byte[])}
	 */
	void forwardUnsentData(Socket socket) throws IOException;
	
	/**
	 * Sets the specified socket to the target socket and starts forwarding data
	 * both ways. Forwards any unsent data first.
	 * @param socket the target socket
	 * @throw IOException {@link Connection#forwardUnsentData(Socket)}
	 */
	void forwardToSocket(Socket socket) throws IOException;

	/**
	 * @return true if the connection is active
	 */
	boolean isActive();
	
	/**
	 * @return true if the connection is forwarding
	 */
	boolean isForwarding();
	
	/**
	 * Ends the connection.
	 * @throw IOException {@link Socket#close()}
	 */
	void end() throws IOException;
}
