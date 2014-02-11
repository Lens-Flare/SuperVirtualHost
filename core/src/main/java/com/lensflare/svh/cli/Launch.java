package com.lensflare.svh.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import jline.console.ConsoleReader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.naturalcli.Command;
import org.naturalcli.ExecutionException;
import org.naturalcli.NaturalCLI;

import com.lensflare.svh.Server;

public class Launch {
	public static void main(String[] args) throws IOException {
		Launch l = new Launch(args);
		while (!l.isDone())
			l.execute();
	}
	
	/* ********************************************************************** */
	
	private final Thread mainThread;
	private final DeferredOutputStream dout;
	private final Server server;
	private final Logger log;
	private final NaturalCLI parser;
	private final ConsoleReader reader;
	
	private boolean done = false;
	
	{
		ConsoleReader cr = null;
		Runnable action = new Runnable() { public void run() { mainThread.interrupt(); } };
		PrintStream sysout = System.out;
		
		this.mainThread = Thread.currentThread();
		this.dout = new DeferredOutputStream(System.out, action);
		System.setOut(new PrintStream(dout));
		this.log = LogManager.getLogger();
		
		try {
			cr = new ConsoleReader(new InterruptableInputStream(System.in, 1), sysout);
		} catch (IOException e) {
			log.catching(Level.FATAL, e);
			System.exit(-1);
		}
		
		this.reader = cr;
		
		Runtime.getRuntime().addShutdownHook(new Thread("shutdown") {
			@Override
			public void run() {
				try {
					done = true;
					dout.setEnabled(false);
					log.info("Running final cleanup");
					mainThread.interrupt();
					dout.runQueue();
					if (server.isRunning())
						server.stop();
					reader.shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Launch(String...args) {
		Server server = null;

		if (args.length > 0)
			try {
				server = com.lensflare.svh.impl.Server.load(new File(args[0]));
			} catch (FileNotFoundException e) {
				log.fatal("Could not find configuration file: {}", args[0]);
				System.exit(-1);
			}
		else
			try {
				server = com.lensflare.svh.impl.Server.load(new File("config.yml"));
			} catch (FileNotFoundException e) {
				server = com.lensflare.svh.impl.Server.load(Launch.class.getResourceAsStream("config.yml"));
			}
		
		if (server == null) {
			log.fatal("Could not find configuration file");
			System.exit(-1);
		}
		
		Set<Command> commands = null;
		try {
			commands = server.collectComands();
		} catch (ClassNotFoundException e) {
			log.fatal("Could not build command list", e);
			System.exit(-1);
		}
		
		this.server = server;
		this.parser = new NaturalCLI(commands);
	}
	
	public boolean isDone() {
		return this.done;
	}
	
	public void execute() {
		String line = null;
		
		try { dout.runQueue(); } catch (Exception e) { log.catching(Level.WARN, e); }
		
		Thread.interrupted();
		dout.setEnabled(true);
		try {
			line = reader.readLine("> ", null);
		} catch (IOException e) {
			if (e.getCause() instanceof InterruptedException) {
				try {
					System.out.write(new byte[] {0x08, 0x08});
				} catch (IOException e1) {
					log.catching(Level.WARN, e1);
				}
			} else
				log.catching(Level.WARN, e);
			return;
		} finally {
			dout.setEnabled(false);
		}
		
		if (line.length() == 0)
			return;
		
		try {
			parser.execute(line);
		} catch (ExecutionException e) {
			if (e.getMessage().equals("No command matches."))
				System.err.println("Command '" + line + "' not found");
			else
				log.catching(Level.WARN, e);
		}
	}
}
