package com.lensflare.svh.impl;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lensflare.svh.Authenticator;
import com.lensflare.svh.Server;
import com.lensflare.svh.Service;

public abstract class AuthenticatedService extends com.lensflare.svh.impl.Service {
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * The name of the authenticator.
	 */
	private final String auth;
	
	public AuthenticatedService(Server server, String name, String type, Map<?, ?> config) throws Exception {
		super(server, name, type, config);
		
		log.debug("Finding the authenticator");
		Object obj = config.get("authenticator");
		if (obj != null && !(obj instanceof String))
			throw new Exception("Problem loading configuration: services > service > authenticator");
		this.auth = (String) obj;
	}
	
	/**
	 * Check's the user's authorization using the configured authenticator.
	 * @param user the user
	 * @param service the service
	 * @return true if the user is authorized
	 */
	public boolean userIsAuthorizedForService(String user, Service service) {
		if (this.auth == null)
			return true;
		
		Authenticator auth = getServer().getAuthenticator(this.auth);
		if (auth == null)
			return true;
		
		return auth.userIsAuthorizedForService(user, service);
	}
}