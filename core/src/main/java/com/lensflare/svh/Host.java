package com.lensflare.svh;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import com.lensflare.svh.impl.Connection;

/**
 * The {@code Host} interface accepts incoming connections. Classes that
 * implement this interface are expected to implement a public constructor with
 * the same signature as
 * {@link com.lensflare.svh.impl.Host#Host(Server, String, Map)}.
 * <hr /><span style="font-weight:bold">YAML</span><br />
 * <ul>
 *   <li>{@code class}, optional, the class to instantiate the host as, defaults
 *   to {@link com.lensflare.svh.impl.Host}.</li>
 *   <li>{@code host}, optional, the host to listen on, defaults to
 *   {@code localhost}.</li>
 *   <li>{@code port}, required, the port to listen on.</li>
 *   <li>{@code connection-class}, optional, the class to instantiate new
 *   connections as, defaults to {@link Connection}.</li>
 * </ul>
 * <hr />
 * @author firelizzard
 *
 */
public interface Host {
	/**
	 * @return the name
	 */
	String getName();
	
	/**
	 * @return the listening socket
	 */
	ServerSocket getSocket();
	
	/**
	 * @return true if the host is listening
	 */
	boolean isRunning();
	
	/**
	 * Starts listening.
	 * @throw IOException {@link ServerSocket#ServerSocket(int, int, java.net.InetAddress)}
	 */
	void start() throws IOException;
	
	/**
	 * Stops listening.
	 * @throw IOException {@link ServerSocket#close()}
	 */
	void stop() throws IOException;
}
