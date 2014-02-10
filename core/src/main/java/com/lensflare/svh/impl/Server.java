package com.lensflare.svh.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.lensflare.svh.Authenticator;
import com.lensflare.svh.Host;
import com.lensflare.svh.Service;

public class Server implements com.lensflare.svh.Server {
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * Loads configuration from a file.
	 * @param configFile the path to the file
	 * @return the {@link Server} object
	 * @throws FileNotFoundException thrown if configFile does not point to a
	 * file
	 * @see Server#load(InputStream)
	 */
	public static Server load(File configFile) throws FileNotFoundException {
		log.debug("Loading configuration from file: {}", configFile);
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(configFile);
			Server server = Server.load(fis);
			return server;
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				log.fatal("Failed to load configuration from file", e);
			}
		}
	}
	
	/**
	 * Loads configuration from a string.
	 * @param configString the string
	 * @return the {@link Server} object
	 * @see Server#load(InputStream)
	 */
	public static Server load(String configString) {
		log.debug("Loading configuration from string: {}", configString);
		
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(configString.getBytes());
			Server server = Server.load(bais);
			return server;
		} finally {
			try {
				if (bais != null);
					bais.close();
			} catch (IOException e) {
				log.fatal("Failed to load configuration from string", e);
			}
		}
	}
	
	/**
	 * Loads configuration from a stream. Does not close the stream.
	 * @param configStream the stream
	 * @return the {@link Server} object
	 */
	public static Server load(InputStream configStream) {
		log.debug("Loading configuration from stream");
		try {
			log.debug("Parsing YAML");
			Object obj = new Yaml().load(configStream);
			if (!(obj instanceof Map))
				throw new Exception("Problem loading configuration from YAML");
			Map<?, ?> map = (Map<?, ?>) obj;
			
			log.debug("Finding server-class");
			obj = map.get("server-class");
			if (obj != null && !(obj instanceof String))
				throw new Exception("Problem loading configuration: server-class");
			String className = (String) obj;

			log.debug("Finding load");
			obj = map.get("load");
			if (obj == null)
				obj = Collections.EMPTY_LIST;
			else if (!(obj instanceof List))
				throw new Exception("Problem loading configuration: load");
			List<?> load = (List<?>) obj;

			log.debug("Finding hosts");
			obj = map.get("hosts");
			if (obj == null)
				obj = Collections.EMPTY_MAP;
			else if (!(obj instanceof Map))
				throw new Exception("Problem loading configuration: hosts");
			Map<?, ?> hosts = (Map<?, ?>) obj;

			log.debug("Finding services");
			obj = map.get("services");
			if (obj == null)
				obj = Collections.EMPTY_MAP;
			else if (!(obj instanceof Map))
				throw new Exception("Problem loading configuration: services");
			Map<?, ?> services = (Map<?, ?>) obj;

			log.debug("Finding authenticators");
			obj = map.get("authenticators");
			if (obj == null)
				obj = Collections.EMPTY_MAP;
			else if (!(obj instanceof Map))
				throw new Exception("Problem loading configuration: authenticators");
			Map<?, ?> authenticators = (Map<?, ?>) obj;

			log.debug("Finding actual class for server-class: {}", className);
			@SuppressWarnings("unchecked")
			Class<? extends Server> theClass = (Class<? extends Server>) Class.forName(className);
			if (theClass == null)
				throw new Exception("Problem loading configuration: Invalid class name " + className);

			log.debug("Finding constructor for server-class");
			Constructor<? extends Server> constructor = theClass.getConstructor(List.class, Map.class, Map.class, Map.class);
			if (constructor == null)
				throw new Exception("Problem loading configuration: No appropriate constructor for class " + className);

			log.debug("Instantiating server");
			return constructor.newInstance(load, hosts, services, authenticators);
		} catch (Exception e) {
			log.fatal("Caught exception, dying",  e);
			System.exit(-1);
			return null;
		}
	}
	
	/**
	 * A stack of hooks that will be run when the server stops.
	 */
	private final List<Runnable> cleanupHooks = new ArrayList<Runnable>();
	
	/**
	 * The map of hosts.
	 */
	private final Map<String, Host> hosts = new HashMap<String, Host>();
	
	/**
	 * The map of services.
	 */
	private final Map<String, Map<String, Service>> services = new HashMap<String, Map<String, Service>>();
	
	/**
	 * The map of authenticators
	 */
	private final Map<String, Authenticator> authenticators = new HashMap<String, Authenticator>();
	
	/**
	 * True if the server is running
	 */
	private boolean running = false;
	
	/**
	 * Creates a new server.
	 * @param load a list of classes to load
	 * @param hostsInfo hosts configuration map
	 * @param servicesInfo services configuration map
	 * @param authenticatorsInfo authenticators configuration map
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Server(List<?> load, Map<?, ?> hostsInfo, Map<?, ?> servicesInfo, Map<?, ?> authenticatorsInfo) throws Exception {
		log.info("Setting up server");
		
		log.info("Loading classes");
		for (Object obj : load) {
			if (!(obj instanceof String))
				throw new Exception("Problem loading configuration: load > class");
			log.debug("Loading ", obj);
			Class.forName((String) obj);
		}
		
		log.info("Loading hosts");
		for (Map.Entry<?, ?> entry : hostsInfo.entrySet()) {
			if (!(entry.getKey() instanceof String))
				throw new Exception("Problem loading configuration: hosts > key");
			
			if (!(entry.getValue() instanceof Map))
				throw new Exception("Problem loading configuration: hosts > value");
			
			log.debug("Finding host class");
			Object className = ((Map<?, ?>) entry.getValue()).get("class");
			if (className == null)
				className = "con.lensflare.svh.impl.Host";
			else if (!(className instanceof String))
				throw new Exception("Problem loading configuration: hosts > host > class");
			
			log.debug("Finding host's actual class: {}", className);
			Class<? extends Host> theClass = (Class<? extends Host>) Class.forName((String) className);
			if (theClass == null)
				throw new Exception("Problem loading configuration: Invalid class name " + className);
			
			log.debug("Finding host's constructor");
			Constructor<? extends Host> constructor = theClass.getConstructor(com.lensflare.svh.Server.class, String.class, Map.class);
			if (constructor == null)
				throw new Exception("Problem loading configuration: No appropriate constructor for class " + className);
			
			log.debug("Instantiating host");
			Host host = constructor.newInstance(this, entry.getKey(), entry.getValue());
			hosts.put(host.getName(), host);
		}
		
		log.info("Loading services");
		for (Map.Entry<?, ?> entry : servicesInfo.entrySet()) {
			if (!(entry.getKey() instanceof String))
				throw new Exception("Problem loading configuration: services > key");
			
			if (!(entry.getValue() instanceof Map))
				throw new Exception("Problem loading configuration: services > value");
			
			Map<String, Service> servicesForType = new HashMap<String, Service>();
			services.put((String) entry.getKey(), servicesForType);
			
			log.info("Loading services of type {}", entry.getKey());
			for (Map.Entry<?, ?> _entry : ((Map<?, ?>) entry.getValue()).entrySet()) {
				if (!(_entry.getKey() instanceof String))
					throw new Exception("Problem loading configuration: services > key");
				
				if (!(_entry.getValue() instanceof Map))
					throw new Exception("Problem loading configuration: services > value");
				
				log.debug("Finding service class");
				Object className = ((Map<?, ?>) _entry.getValue()).get("class");
				if (className == null)
					className = "com.lensflare.svh.impl.Service";
				else if (!(className instanceof String))
					throw new Exception("Problem loading configuration: services > service > class");
				
				log.debug("Finding service's actual class: {}", className);
				Class<? extends Service> theClass = (Class<? extends Service>) Class.forName((String) className);
				if (theClass == null)
					throw new Exception("Problem loading configuration: Invalid class name " + className);
				
				log.debug("Finding service's constructor");
				Constructor<? extends Service> constructor = theClass.getConstructor(com.lensflare.svh.Server.class, String.class, String.class, Map.class);
				if (constructor == null)
					throw new Exception("Problem loading configuration: No appropriate constructor for class " + className);
				
				log.debug("Instantiating service");
				Service service = constructor.newInstance(this, _entry.getKey(), entry.getKey(), _entry.getValue());
				servicesForType.put(service.getKey(), service);
			}
		}
		
		log.info("Loading authenticators");
		for (Map.Entry<?, ?> entry : authenticatorsInfo.entrySet()) {
			if (!(entry.getKey() instanceof String))
				throw new Exception("Problem loading configuration: authenticators > key");
			
			if (!(entry.getValue() instanceof Map))
				throw new Exception("Problem loading configuration: authenticators > value");
			
			log.debug("Finding authenticator class");
			Object className = ((Map<?, ?>) entry.getValue()).get("class");
			if (className == null)
				className = "con.lensflare.svh.impl.Authenticator";
			else if (!(className instanceof String))
				throw new Exception("Problem loading configuration: authenticators > authenticator > class");
			
			log.debug("Finding authenticator's actual class: {}", className);
			Class<? extends Authenticator> theClass = (Class<? extends Authenticator>) Class.forName((String) className);
			if (theClass == null)
				throw new Exception("Problem loading configuration: Invalid class name " + className);
			
			log.debug("Finding authenticator's constructor");
			Constructor<? extends Authenticator> constructor = theClass.getConstructor(com.lensflare.svh.Server.class, String.class, Map.class);
			if (constructor == null)
				throw new Exception("Problem loading configuration: No appropriate constructor for class " + className);
			
			log.debug("Instantiating constructor");
			Authenticator auth = constructor.newInstance(this, entry.getKey(), entry.getValue());
			authenticators.put(auth.getName(), auth);
		}
	}

	@Override
	public Service getServiceForTypeAndHost(String type, String host) {
		Map<String, Service> services = this.services.get(type);
		if (services == null)
			return null;
		
		Service service = services.get(host);
		if (service == null)
			service = services.get("");
		
		return service;
	}

	@Override
	public Authenticator getAuthenticator(String name) {
		return authenticators.get(name);
	}

	@Override
	public void addCleanupHook(Runnable routine) {
		cleanupHooks.add(routine);
	}
	
	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void start() throws IOException {
		this.running = true;
		
		log.info("Starting server");
		
		for (Host host : hosts.values())
			host.start();
	}
	
	@Override
	public void stop() {
		this.running = false;
		
		log.info("Stopping server");
		
		for (Runnable routine : cleanupHooks)
			try {
				routine.run();
			} catch (Exception e) {
				log.catching(Level.WARN, e);
			}
		
		log.info("Server stopped");
	}
}
