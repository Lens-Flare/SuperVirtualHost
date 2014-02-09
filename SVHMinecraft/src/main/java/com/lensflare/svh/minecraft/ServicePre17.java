package com.lensflare.svh.minecraft;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lensflare.svh.Connection;
import com.lensflare.svh.Server;

public class ServicePre17 extends com.lensflare.svh.impl.AuthenticatedService {
	private static final Logger log = LogManager.getLogger();
	
	public ServicePre17(Server server, String name, String type, Map<?, ?> config) throws Exception {
		super(server, name, type, config);
	}

	/**
	 * Looks at the first packet, finds the user name, and checks if that user
	 * is authorized for this service.
	 * @param connection the connection
	 * @throws IOException
	 */
	@Override
	public void handleConnection(Connection connection) throws IOException {
		byte[] data = connection.getUnsentData(0);
		
		if (data[0] == (byte)0xFE) {
			log.debug("Forwarding ping connection");
			super.handleConnection(connection);
			return;
		}
		
		// user name
		int offset = 2; // string length offset
		int strlen = data[offset++] << 8 | data[offset++]; // string length
		
		String user = new String(data, offset, strlen * 2, "UTF-16");
		if (!userIsAuthorizedForService(user, this)) {
			log.info("User {} is not authorized", user);
			connection.end();
			return;
		}

		log.debug("Forwarding connection for user {}", user);
		super.handleConnection(connection);
	}
}
