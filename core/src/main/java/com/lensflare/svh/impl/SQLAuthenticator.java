package com.lensflare.svh.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lensflare.svh.Server;
import com.lensflare.svh.Service;

public class SQLAuthenticator extends Authenticator {
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * The prepared statement used to check users' authorization.
	 */
	private final PreparedStatement statement;
	
	/**
	 * The list and order of parameters of the statment.
	 */
	private final List<String> params = new ArrayList<String>(3);
	
	/**
	 * Creates a new authenticator that uses a SQL database
	 * @param server the originating server
	 * @param name the name
	 * @param config the configuration map
	 * @throws Exception
	 */
	public SQLAuthenticator(Server server, String name, Map<?, ?> config) throws Exception {
		super(server, name, config);
		
		log.debug("Finding the database url");
		Object obj = config.get("url");
		if (!(obj instanceof String))
			throw new Exception("Problem loading configuration: authenticators > authenticator > url");
		String url = (String) obj;
		
		log.debug("Finding the authentication query");
		obj = config.get("query");
		if (!(obj instanceof String))
			throw new Exception("Problem loading configuration: authenticators > authenticator > query");
		String query = (String) obj;
		
		log.debug("Finding the parameter order");
		obj = config.get("parameter-order");
		if (!(obj instanceof List))
			throw new Exception("Problem loading configuration: authenticators > authenticator > parameter-order");
		
		log.debug("Processing the parameter order list");
		for (Object _obj : (List<?>)obj) {
			if (!(_obj instanceof String))
				throw new Exception("Problem loading configuration: authenticators > authenticator > parameter-order > entry");
			params.add((String)_obj);
		}
		
		
		log.debug("Connecting to {}", url);
		final Connection connection = DriverManager.getConnection(url);
		
		log.debug("Preparing the statement: {}", query);
		this.statement = connection.prepareStatement(query);
		
		log.debug("Installing a cleanup hook");
		getServer().addCleanupHook(new Runnable() {
			public void run() {
				try {
					log.debug("Closing the database connection");
					if (!connection.isClosed())
						connection.close();
				} catch (SQLException e) {
					log.catching(Level.WARN, e);
				}
			}
		});
	}
	
	@Override
	public boolean userIsAuthorizedForService(String user, Service service) {
		log.info("Checking user {}'s authorization for service {}", user, service);
		
		try {
			log.debug("Setting the statement parameters");
			
			int index = params.indexOf("user");
			if (index > -1)
				statement.setString(index + 1, user);

			index = params.indexOf("service-name");
			if (index > -1)
				statement.setString(index + 1, service.getName());

			index = params.indexOf("service-type");
			if (index > -1)
				statement.setString(index + 1, service.getType());
			
			log.debug("Executing the statement");
			ResultSet rs = statement.executeQuery();
			if (!rs.next())
				return false;
			return rs.getInt(1) > 0;
		} catch (SQLException e) {
			log.catching(Level.ERROR, e);
			return false;
		}
	}
}
