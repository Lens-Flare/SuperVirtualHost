package com.lensflare.svh.minecraft;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lensflare.svh.Connection;
import com.lensflare.svh.Server;

public class ServicePost17 extends com.lensflare.svh.impl.AuthenticatedService {
	private static final Logger log = LogManager.getLogger();
	
	public ServicePost17(Server server, String name, String type, Map<?, ?> config) throws Exception {
		super(server, name, type, config);
	}
	
	@Override
	public void handleConnection(Connection connection) throws IOException {
		byte[] data = connection.getUnsentData(0);
		int length = data[0];
		int nextState = data[length];
		
		if (nextState != 2) {
			log.debug("Forwarding ping packet");
			super.handleConnection(connection);
			return;
		}
		
		if (data.length == length + 1)
			data = connection.getData();
		else
			data = Arrays.copyOfRange(data, length + 1, data.length);
		
		int offset = 2;
		int strlen = data[offset++];
		String user = new String(data, offset, strlen, "UTF-8");
		if (!userIsAuthorizedForService(user, this)) {
			log.info("User {} is not authorized", user);
			connection.end();
			return;
		}

		log.debug("Forwarding connection for user {}", user);
		super.handleConnection(connection);
	}
}