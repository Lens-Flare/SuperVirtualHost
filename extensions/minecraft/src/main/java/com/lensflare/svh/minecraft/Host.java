package com.lensflare.svh.minecraft;

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
	 * The service name to send pre-1.7 pings to.
	 */
	private final String pre17ping;
	
	/**
	 * @see com.lensflare.svh.impl.Host#Host(Server, String, Map)
	 */
	public Host(Server server, String name, Map<?, ?> config) throws Exception {
		super(server, name, config);
		
		log.debug("Finding pre 1.7 ping service");
		Object obj = config.get("pre-17-ping");
		if (!(obj instanceof String))
			throw new Exception("Problem loading configuration: hosts > host > pre-17-ping");
		this.pre17ping = (String) obj;
	}
	
	/**
	 * Accepts connections until the socket is closed. Attempts to determine
	 * the host name the user requested a connection to. Pre 1.7 clients sent no
	 * host name information with the server ping list request, so those must
	 * all be routed to one server.
	 */
	@Override
	public void run() {
		log.info("Running the {} host", getName());
		
		while (isRunning())
			try {
				log.debug("Accepting a connection");
				Connection connection = accept();
				
				log.debug("Capturing the first packet");
				byte[] data = connection.getData();
				
				log.debug("Determining client type");
				Service service = null;
				if (data[0] == (byte)0xFE)
				{
					log.debug("Pre-1.7 ping packet received");
					service = getServer().getServiceForTypeAndHost("Minecraft", pre17ping);
				}
				// is it a pre-1.7 server connection
				else if (data[0] == 2)
				{
					log.debug("Pre-1.7 connection packet received");
					// user name
					int offset = 2; // string length offset
					int strlen = data[offset++] << 8 | data[offset++]; // string length
					
					// host name
					offset += 2 * strlen; // string length offset
					strlen = data[offset++] << 8 | data[offset++]; // string length
					
					// getting the service
					service = getServer().getServiceForTypeAndHost("Minecraft", new String(data, offset, strlen * 2, "UTF-16"));
				}
				// is it a 1.7+ connection (ping or otherwise)?
				else if (data[1] == 0)
				{
					log.debug("Post-1.7 packet received");
					// host name
					int offset = 3; // string length offset
					int strlen = data[offset++]; // string length
					
					// getting the service
					service = getServer().getServiceForTypeAndHost("Minecraft", new String(data, offset, strlen, "UTF-8"));
				}
				// no idea what it might be, print it out so someone can submit an issue
				else {
					log.error("Unknown packet received:");
					
					// print out the packet as characters (non-printing chars become '.')
					System.out.print('\t');
					for (int i = 0; i < data.length; i++)
						if (20 <= data[i] && data[i] < 127)
							System.out.print(String.format("%c", data[i]));
						else
							System.out.print('.');
					System.out.println();
					
					// print out the packet as hex
					System.out.print('\t');
					for (int i = 0; i < data.length; i++) {
						System.out.print(String.format("%02x", data[i]));
						if (i % 4 == 3)
							System.out.print(' ');
					}
					System.out.println();
				}
				
				if (service == null) {
					log.warn("No service found");
					continue;
				}
				
				log.debug("Passing off connection");
				service.handleConnection(connection);
			} catch (Exception e) {
				log.catching(_running() ? Level.DEBUG : Level.WARN, e);
			}
	}
}
