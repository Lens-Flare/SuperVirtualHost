package com.lensflare.svh.minecraft;

import java.io.IOException;
import java.util.Map;

import com.lensflare.svh.Connection;
import com.lensflare.svh.Server;

public class ServicePost17 extends com.lensflare.svh.impl.AuthenticatedService {
	public ServicePost17(Server server, String name, String type, Map<?, ?> config) throws Exception {
		super(server, name, type, config);
	}
	
	@Override
	public void handleConnection(Connection connection) throws IOException {
		super.handleConnection(connection);
	}
}