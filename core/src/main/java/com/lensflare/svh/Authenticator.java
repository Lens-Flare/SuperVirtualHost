package com.lensflare.svh;

import java.util.Map;

/**
 * The {@code Authenticator} interface is used to authenticate users for
 * services. Classes that implement this interface are expected to implement a
 * public constructor with the same signature as
 * {@link com.lensflare.svh.impl.Authenticator#Authenticator(Server, String, Map)}.
 * <hr /><span style="font-weight:bold">YAML</span><br />
 * <ul>
 *   <li>{@code class}, required, the class to instantiate the authenticator
 *   as.</li>
 * </ul>
 * <hr />
 * @author firelizzard
 *
 */
public interface Authenticator {
	/**
	 * @return the name of this authenticator
	 */
	String getName();
	
	/**
	 * Determines whether or not a user is authorized to connect to a service.
	 * @param user the user
	 * @param service the service
	 * @return true if the user should be allowed to connect
	 */
	boolean userIsAuthorizedForService(String user, Service service);
}
