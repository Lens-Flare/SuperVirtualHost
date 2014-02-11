package com.lensflare.svh.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lensflare.svh.Connection;
import com.lensflare.svh.Server;

public class Host implements com.lensflare.svh.Host, Runnable {
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * The server that created this object.
	 */
	private final Server server;
	
	/**
	 * The name.
	 */
	private final String name;
	
	/**
	 * The address to connect to.
	 */
	private final InetSocketAddress addr;
	
	/**
	 * The connection constructor.
	 */
	private final Constructor<? extends Connection> connectionConstructor;
	
	/**
	 * The connection handling thread.
	 */
	private final Thread thread = new Thread(this, "lstn");
	
	/**
	 * The listening socket.
	 */
	private ServerSocket socket = null;
	
	/**
	 * True if the host is accepting connections.
	 */
	private boolean running = false;
	
	/**
	 * Creates a new host.
	 * @param server the originating server
	 * @param name the name
	 * @param config the configuration map
	 * @throws Exception
	 */
	public Host(Server server, String name, Map<?, ?> config) throws Exception {
		log.info("Setting up the {} host", name);
		
		this.server = server;
		this.name = name;
		
		log.debug("Finding the host name");
		String host;
		Object obj = config.get("host");
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
		
		log.debug("Finding connection-class");
		String className;
		obj = config.get("connection-class");
		if (obj == null)
			className = com.lensflare.svh.impl.Connection.class.getSimpleName();
		else if (!(obj instanceof String))
			throw new Exception("Problem loading configuration: hosts > host > host");
		else
			className = (String) obj;
		
		log.debug("Finding the actual connection class");
		@SuppressWarnings("unchecked")
		Class<? extends Connection> theClass = (Class<? extends Connection>) Class.forName(className);
		if (theClass == null)
			throw new Exception("Problem loading configuration: Invalid class name " + className);
		
		log.debug("Finding the connection's constructor");
		this.connectionConstructor = theClass.getConstructor(Server.class, Socket.class);
		if (connectionConstructor == null)
			throw new Exception("Problem loading configuration: No appropriate constructor for class " + className);
		
		log.debug("Installing a cleanup hook");
		getServer().addCleanupHook(new Runnable() {
			public void run() {
				try {
					stop();
				} catch (IOException e) {
					log.catching(Level.WARN, e);
				}
			}
		});
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
	public ServerSocket getSocket() {
		return this.socket;
	}
	
	protected synchronized boolean _running() {
		return this.running;
	}

	@Override
	public boolean isRunning() {
		if (!_running())
			return false;
		
		if (socket == null)
			return false;
		
		if (socket.isClosed())
			return false;
		
		return true;
	}
	
	/**
	 * Accepts incoming connections.
	 * @return the new connection
	 * @see ServerSocket#accept()
	 * @see Constructor#newInstance(Object...)
	 */
	protected Connection accept() throws IOException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		if (!isRunning())
			return null;
		
		log.debug("Accepting a connection");
		
		return connectionConstructor.newInstance(getServer(), socket.accept());
	}

	@Override
	public synchronized void start() throws IOException {
		if (isRunning())
			return;
		
		log.info("Starting the {} host on {}", name, addr);
		
		this.running = true;
		this.socket = new ServerSocket(addr.getPort(), 10, addr.getAddress());
		thread.start();
		
		log.info("Host started");
	}
	
	/**
	 * Accepts connections until the socket is closed. Connections are passed to
	 * the result of calling
	 * {@link Server#getServiceForTypeAndHost(String, String)} with the host's
	 * name and a blank service.
	 */
	@Override
	public void run() {
		log.info("Running the {} host on {}", name, addr);
		
		while (isRunning())
			try {
				log.debug("Accepting a connection");
				getServer().getServiceForTypeAndHost(getName(), "").handleConnection(accept());
			} catch (Exception e) {
				log.catching(_running() ? Level.DEBUG : Level.WARN, e);
			}
	}

	@Override
	public synchronized void stop() throws IOException {
		if (!isRunning())
			return;

		log.info("Stopping the {} host on {}", name, addr);
		
		if (!socket.isClosed())
			socket.close();
		this.socket = null;
		this.running = false;
		
		log.info("Host stopped");
	}
}
