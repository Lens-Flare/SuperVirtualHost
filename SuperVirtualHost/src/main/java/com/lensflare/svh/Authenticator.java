package com.lensflare.svh;

import java.util.Map;

/**
 * The {@code Authenticator} class is used to authenticate users for services.
 * Classes that implement this interface are expected to implement a public
 * constructor with the same signature as
 * {@link com.lensflare.svh.impl.Authenticator#Authenticator(Server, String, Map)}.
 * <hr /><span style="font-weight:bold">YAML</span><br />
 * The only required parameter is {@code class}. The authenticator will be
 * instantiated as the specified class. Implementations may require more
 * parameters.
 * <hr />
 * @author firelizzard
 *
 */
public interface Authenticator {
	/**
	 * @return the name of this authenticator
	 */
	public String getName();
	
	/**
	 * Determines whether or not a user is authorized to connect to a service.
	 * @param user the user
	 * @param service the service
	 * @return true if the user should be allowed to connect
	 */
	public boolean userIsAuthorizedForService(String user, Service service);
}
