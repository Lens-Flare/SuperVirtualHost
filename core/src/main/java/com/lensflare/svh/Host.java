package com.lensflare.svh;

import java.io.IOException;
import java.net.ServerSocket;

public interface Host {
	String getName();
	ServerSocket getSocket();
	boolean isRunning();
	void start() throws IOException;
	void stop() throws IOException;
}
