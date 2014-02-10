package com.lensflare.svh;

import java.io.IOException;


public interface Server {
	public Service getServiceForTypeAndHost(String type, String host);
	public Authenticator getAuthenticator(String name);
	
	public void addCleanupHook(Runnable routine);

	boolean isRunning();
	public void start() throws IOException;
	public void stop();
}
