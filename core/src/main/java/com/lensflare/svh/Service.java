package com.lensflare.svh;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

/**
 * The {@code Service} interface represents services users can be routed to.
 * Classes that implement this interface are expected to implement a public
 * constructor with the same signature as
 * {@link com.lensflare.svh.impl.Service#Service(Server, String, Map)}.
 * <hr /><span style="font-weight:bold">YAML</span><br />
 * <ul>
 *   <li>{@code class}, optional, the class to instantiate the service as,
 *   defaults to {@link com.lensflare.svh.impl.Service}.</li>
 *   <li>{@code name}, optional, the service's name.</li>
 *   <li>{@code host}, optional, the host to connect clients to, defaults to
 *   {@code localhost}.</li>
 *   <li>{@code port}, required, the port to connect to.</li>
 *   <li>{@code authenticator}, option, the name of the authenticator to use to
 *   check users.</li>
 * </ul>
 * <hr />
 * @author firelizzard
 *
 */
public interface Service {
	/**
	 * @return the external host name
	 */
	public String getKey();
	
	/**
	 * @return the name
	 */
	public String getName();
	
	/**
	 * 
	 * @return the type
	 */
	public String getType();
	
	/**
	 * @return the socket address
	 */
	public InetSocketAddress getAddress();
	
	/**
	 * Handles a connection by passing it to the service, possibly
	 * authenticating.
	 * @param connection the connection
	 * @throws IOException {@link Socket#Socket(String, int)}
	 */
	public void handleConnection(Connection connection) throws IOException;
}
