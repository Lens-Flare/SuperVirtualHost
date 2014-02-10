package com.lensflare.svh;

import java.io.IOException;


public interface Server {
	Host getHost(String name);
	Service getServiceForTypeAndHost(String type, String host);
	Authenticator getAuthenticator(String name);
	
	boolean registerConnection(Connection connection);
	boolean unregisterHost(Host host);
	boolean unregisterService(Service service);
	boolean unregisterConnection(Connection connection);
	
	void addCleanupHook(Runnable routine);

	boolean isRunning();
	void start() throws IOException;
	void stop();
	
	String status();
}
