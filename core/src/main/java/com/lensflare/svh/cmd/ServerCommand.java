package com.lensflare.svh.cmd;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.naturalcli.ExecutionException;
import org.naturalcli.ICommandExecutor;
import org.naturalcli.ParseResult;

import com.lensflare.svh.Server;
import com.lensflare.svh.anno.SeeAlso;


@SeeAlso({StartServer.class, ServerStatus.class, StopServer.class, Quit.class})
public abstract class ServerCommand implements ICommandExecutor {
	private static final Logger defaultLogger = LogManager.getLogger();
	private static       Logger staticLogger = null;
	private              Logger instanceLogger = null;
	
	private final Server server;
	
	public ServerCommand(Server server) {
		this.server = server;
	}
	
	public Logger log() {
		if (instanceLogger != null)
			return instanceLogger;
		else if (staticLogger != null)
			return staticLogger;
		else
			return defaultLogger;
	}
	
	protected static void setStaticLogger(Logger logger) {
		staticLogger = logger;
	}
	
	protected void setInstanceLogger(Logger logger) {
		instanceLogger = logger;
	}
	
	protected Server getServer() {
		return server;
	}

	@Override
	public final void execute(ParseResult pr) throws ExecutionException {
		try {
			safeExecute(pr);
		} catch (ExecutionException e) {
			throw e;
		} catch (Exception e) {
			log().catching(Level.WARN, e);
		}
	}
	
	protected abstract void safeExecute(ParseResult pr) throws Exception;
}
