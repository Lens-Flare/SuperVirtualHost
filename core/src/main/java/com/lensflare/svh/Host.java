package com.lensflare.svh;

import java.io.IOException;

public interface Host {
	public String getName();
	public boolean isRunning();
	public void start() throws IOException;
	public void stop() throws IOException;
}
