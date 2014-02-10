package com.lensflare.svh;

import java.io.IOException;

public interface Service {
	public String getName();
	public String getType();
	public void handleConnection(Connection connection) throws IOException;
}
