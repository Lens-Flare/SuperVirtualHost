package com.lensflare.svh;

public interface Authenticator {
	public String getName();
	public boolean userIsAuthorizedForService(String user, Service service);
}
