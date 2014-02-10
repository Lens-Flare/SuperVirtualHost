package com.lensflare.svh;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface Service {
	public String getKey();
	public String getName();
	public String getType();
	public InetSocketAddress getAddress();
	public void handleConnection(Connection connection) throws IOException;
}
