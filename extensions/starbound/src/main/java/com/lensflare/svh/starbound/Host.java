package com.lensflare.svh.starbound;

import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lensflare.svh.Connection;
import com.lensflare.svh.Server;
import com.lensflare.svh.Service;

public class Host extends com.lensflare.svh.impl.Host {
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * @see com.lensflare.svh.impl.Host#Host(Server, String, Map)
	 */
	public Host(Server server, String name, Map<?, ?> config) throws Exception {
		super(server, name, config);
	}
	
	@Override
	public void run() {
		log.info("Running the {} host", getName());
		
		while (isRunning())
			try {
				log.debug("Accepting a connection");
				Connection connection = accept();
				
				log.debug("Capturing the first packet");
//				byte[] data = connection.getData();
				
				log.debug("Determining client type");
				Service service = getServer().getServiceForTypeAndHost("Starbound", "");
				
				if (service == null) {
					log.warn("No service found");
					continue;
				}
				
				log.debug("Passing off connection");
				service.handleConnection(connection);
			} catch (Exception e) {
				log.catching(Level.WARN, e);
			}
	}
}
