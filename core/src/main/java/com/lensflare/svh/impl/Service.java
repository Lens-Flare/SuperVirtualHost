package com.lensflare.svh.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lensflare.svh.Connection;
import com.lensflare.svh.Server;

public class Service implements com.lensflare.svh.Service {
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * The originating server.
	 */
	private final Server server;
	
	/**
	 * The key.
	 */
	@SuppressWarnings("unused")
	private final String key;
	
	/**
	 * The name.
	 */
	private final String name;
	
	/**
	 * The type.
	 */
	private final String type;
	
	/**
	 * The address.
	 */
	private final InetSocketAddress addr;
	
	/**
	 * Creates a new service
	 * @param server the originating server
	 * @param name the name
	 * @param type the type
	 * @param config the configuration map
	 * @throws Exception
	 */
	public Service(Server server, String key, String type, Map<?, ?> config) throws Exception {
		log.info("Setting up the service for {}", key);
		
		this.server = server;
		this.key = key;
		this.type = type;
		
		log.debug("Finding the name");
		Object obj = config.get("name");
		if (!(obj instanceof String))
			throw new Exception("Problem loading configuration: hosts > host > name");
		this.name = (String) obj;
		
		log.debug("Finding the host name");
		String host;
		obj = config.get("host");
		if (obj == null)
			host = "localhost";
		else if (!(obj instanceof String))
			throw new Exception("Problem loading configuration: hosts > host > host");
		else
			host = (String) obj;
		
		log.debug("Finding the host port");
		obj = config.get("port");
		if (!(obj instanceof Number))
			throw new Exception("Problem loading configuration: hosts > host > port");
		Number port = (Number) obj;

		log.debug("Creating the host address");
		this.addr = new InetSocketAddress(host, port.intValue());
	}

	/**
	 * @return the originating server
	 */
	protected Server getServer() {
		return this.server;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getType() {
		return this.type;
	}
	
	@Override
	public void handleConnection(Connection connection) throws IOException {
		handleConnection(connection, getNewConnection());
	}
	
	/**
	 * Creates a new connection to the service's host.
	 * @return the new connection
	 * @see Socket#Socket()
	 */
	protected Socket getNewConnection() throws IOException {
		return new Socket(addr.getAddress(), addr.getPort());
	}
	
	/**
	 * Handle's a connection using an existing socket.
	 * @param connection the socket
	 * @param socket the connection
	 * @see Connection#forwardToSocket(Socket)
	 */
	protected void handleConnection(Connection connection, Socket socket) throws IOException {
		log.debug("Forwarding connnection to {}", socket);
		connection.forwardToSocket(socket);
	}
}
