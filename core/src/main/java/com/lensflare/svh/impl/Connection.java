package com.lensflare.svh.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Stack;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lensflare.svh.Server;

public class Connection implements com.lensflare.svh.Connection {
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * The originating server.
	 */
	private final Server server;
	
	/**
	 * The socket.
	 */
	private final Socket socket;
	
	/**
	 * The stack of data that has been received from the socket but not yet sent
	 * to another socket.
	 */
	private final Stack<byte[]> unsentData = new Stack<byte[]>();
	
	/**
	 * A tx/rx thread.
	 */
	private Thread a, b;
	
	/**
	 * Creates a new connection.
	 * @param server the originating server
	 * @param socket the socket
	 */
	public Connection(Server server, Socket socket) {
		log.info("New connection from {}", socket.getInetAddress());
		
		this.server = server;
		this.socket = socket;

		log.debug("Installing a cleanup hook");
		getServer().addCleanupHook(new Runnable() {
			public void run() {
				try {
					end();
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
	public Socket getSocket() {
		return this.socket;
	}
	
	@Override
	public byte[] getData() throws IOException {
		if (this.isForwarding())
			return null;
		
		log.debug("Receiving data with no outbound socket");
		
		byte[] data = new byte[1024];
		int len = socket.getInputStream().read(data);
		if (len < 0)
			return null;
		
		data = Arrays.copyOf(data, len);
		
		this.unsentData.push(data);
		return data;
	}

	@Override
	public byte[] getUnsentData(int index) {
		if (index >= unsentData.size())
			return null;
		return unsentData.get(index);
	}

	@Override
	public void forwardUnsentData(Socket socket) throws IOException {
		log.debug("Forwarding all unsent data");
		
		while (unsentData.size() > 0)
			socket.getOutputStream().write(unsentData.pop());
	}
	
	@Override
	public void forwardToSocket(final Socket other) throws IOException {
		forwardUnsentData(other);

		log.info("Starting connection forwarding from {} to {}", socket, other);
		
		this.a = new Thread() {
			@Override
			public void run() {
				try {
					byte[] data = new byte[1024];
					InputStream in = socket.getInputStream();
					OutputStream out = other.getOutputStream();
					
					int len;
					while ((len = in.read(data)) > -1)
						out.write(data, 0, len);
				} catch (Exception e) {
					log.catching(Level.WARN, e);
				} finally {
					try {
						if (!other.isClosed())
							other.close();
					} catch (IOException e) {
						log.catching(Level.WARN, e);
					}
					try {
						end();
					} catch (IOException e) {
						log.catching(Level.WARN, e);
					}
				}
			}
		};
		
		this.b = new Thread() {
			@Override
			public void run() {
				try {
					byte[] data = new byte[1024];
					InputStream in = other.getInputStream();
					OutputStream out = socket.getOutputStream();
					
					int len;
					while ((len = in.read(data)) > -1)
						out.write(data, 0, len);
				} catch (Exception e) {
					log.catching(Level.WARN, e);
				} finally {
					try {
						if (!other.isClosed())
							other.close();
					} catch (IOException e) {
						log.catching(Level.WARN, e);
					}
					try {
						end();
					} catch (IOException e) {
						log.catching(Level.WARN, e);
					}
				}
			}
		};
		
		log.debug("Starting tx/rx threads");
		a.start(); b.start();
	}

	@Override
	public boolean isActive() {
		if (socket == null || socket.isClosed())
			return false;
		
		return socket.isConnected();
	}

	@Override
	public boolean isForwarding() {
		if (a == null || !a.isAlive())
			return false;
		
		if (b == null || b.isAlive())
			return false;
		
		return isActive();
	}

	@Override
	public void end() throws IOException {
		if (!isActive())
			return;
		
		log.info("Closing connection from {}", socket);
		
		socket.close();
	}
}
