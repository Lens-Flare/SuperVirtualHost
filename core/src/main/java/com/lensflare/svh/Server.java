package com.lensflare.svh;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.naturalcli.Command;

import com.lensflare.svh.cmd.ServerCommand;

/**
 * The {@code Server} interface sets up and controls the entire system. Classes
 * that implement this interface are expected to implement a public constructor
 * with the same signature as
 * {@link com.lensflare.svh.impl.Server#Server(List, List, Map, Map, Map)}.
 * <hr /><span style="font-weight:bold">YAML</span><br />
 * <ul>
 *   <li>{@code server-class}, optional, the class to instantiate the server as,
 *   defaults to {@link com.lensflare.svh.impl.Server}.</li>
 *   <li>{@code load}, optional, a list of classes to initialize using
 *   {@link Class#forName(String)}</li>
 *   <li>{@code hosts}, required, a map of names to host entries</li>
 *   <li>{@code services}, required, a map of types to maps of external host
 *   names to service entries</li>
 *   <li>{@code authenticator}, optional, a map of names to authenticator
 *   entries.</li>
 * </ul>
 * <hr />
 * @author firelizzard
 *
 */
public interface Server {
	/**
	 * Returns the host associated with the specified name or null.
	 * @param name the name
	 * @return the host
	 */
	Host getHost(String name);
	
	/**
	 * Returns the service associated with the specified type and external host
	 * name or null.
	 * @param type the type
	 * @param host the host name
	 * @return the service
	 */
	Service getServiceForTypeAndHost(String type, String host);
	
	/**
	 * Returns the authenticator associated with the specified name or null.
	 * @param name the name
	 * @return the authenticator
	 */
	Authenticator getAuthenticator(String name);
	
	/**
	 * Searches the packages specified in the YAML for {@link ServerCommand}s.
	 * @return the commands
	 * @throws ClassNotFoundException {@link Class#forName(String)}
	 */
	Set<Command> collectComands() throws ClassNotFoundException;
	
	/**
	 * Registers a connection.
	 * @param connection the connection
	 * @return true if the connection was registered
	 */
	boolean registerConnection(Connection connection);
	
	/**
	 * Unregisters a connection.
	 * @param connection the connection
	 * @return true if the connection was unregistered
	 */
	boolean unregisterConnection(Connection connection);
	
	/**
	 * Adds a cleanup routine that will be run when the server shuts down.
	 * @param routine the routine
	 */
	void addCleanupHook(Runnable routine);

	/**
	 * @return true if the server is running
	 */
	boolean isRunning();
	
	/**
	 * Prints the status of the server (to a string).
	 * @param detail if true, prints more detail
	 * @return the server's status info
	 */
	String status(boolean detail);
	
	/**
	 * Starts the server.
	 * @throws {@link Host#start()}
	 */
	void start() throws IOException;
	
	/**
	 * Stops the server.
	 */
	void stop();

	/**
	 * Stops the server and exits the JVM
	 * @param status the status
	 * @see System#exit(int)
	 */
	void exit(int status);
}
