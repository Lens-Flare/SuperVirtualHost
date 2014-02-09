package com.lensflare.svh.impl;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lensflare.svh.Server;

public abstract class Authenticator implements com.lensflare.svh.Authenticator {
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * The originating server.
	 */
	private final Server server;
	
	/**
	 * The name.
	 */
	private final String name;
	
	/**
	 * Creates a new authenticator
	 * @param server the originating server
	 * @param name the name
	 * @param config the configuration map
	 * @throws Exception
	 */
	public Authenticator(Server server, String name, Map<?, ?> config) throws Exception {
		log.info("Setting up the {} authenticator", name);
		
		this.server = server;
		this.name = name;
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
}
