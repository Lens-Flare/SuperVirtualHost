package com.lensflare.svh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import jline.console.ConsoleReader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.naturalcli.Command;
import org.naturalcli.ExecutionException;
import org.naturalcli.ICommandExecutor;
import org.naturalcli.InvalidSyntaxException;
import org.naturalcli.NaturalCLI;
import org.naturalcli.ParseResult;

public class Launch {
	public static void main(String[] args) throws IOException {
		Launch l = new Launch(args);
		l.run();
	}
	
	/* ********************************************************************** */
	
	private class InterruptableInputStream extends InputStream {
		private final InputStream original;
		private final long timeout;
		
		public InterruptableInputStream(InputStream original, int timeout) {
			this.original = original;
			this.timeout = timeout;
		}

		@Override
		public int available() throws IOException {
			return original.available();
		}

		@Override
		public void close() throws IOException {
			original.close();
		}

		@Override
		public synchronized void mark(int readlimit) {
			original.mark(readlimit);
		}

		@Override
		public boolean markSupported() {
			return original.markSupported();
		}

		@Override
		public int read() throws IOException {
			while (available() == 0)
				try { Thread.sleep(timeout); } catch (InterruptedException e) { throw new IOException(e); }
			return original.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			while (available() == 0)
				try { Thread.sleep(timeout); } catch (InterruptedException e) { throw new IOException(e); }
			return original.read(b, off, len);
		}

		@Override
		public int read(byte[] b) throws IOException {
			while (available() == 0)
				try { Thread.sleep(timeout); } catch (InterruptedException e) { throw new IOException(e); }
			return original.read(b);
		}

		@Override
		public synchronized void reset() throws IOException {
			original.reset();
		}

		@Override
		public long skip(long n) throws IOException {
			return original.skip(n);
		}
	}
	
	private class DeferredOutputStream extends OutputStream {
		private final OutputStream original;
		private final Runnable action;
		private final Queue<Callable<?>> queued = new ConcurrentLinkedQueue<Callable<?>>();
		
		public DeferredOutputStream(OutputStream original, Runnable action) {
			this.original = original;
			this.action = action;
		}
		
		@Override
		public void write(final int b) throws IOException {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.write(b); return null; } });
			action.run();
		}

		@Override
		public void write(final byte[] b, final int off, final int len) throws IOException {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.write(b, off, len); return null; } });
			action.run();
		}

		@Override
		public void write(final byte[] b) throws IOException {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.write(b); return null; } });
			action.run();
		}
		
		@Override
		public void flush() throws IOException {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.flush(); return null; } });
			action.run();
		}

		@Override
		public void close() throws IOException {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.close(); return null; } });
			action.run();
		}

		public synchronized void runQueue() throws Exception {
			Iterator<Callable<?>> iter = queued.iterator();
			while (iter.hasNext()) {
				iter.next().call();
				iter.remove();
			}
		}
	}

	private final Thread mainThread;
	private final DeferredOutputStream dout;
	private final Logger log;
	private final Server server;
	private final NaturalCLI cli;
	private final ConsoleReader reader;
	
	
	{
		Set<Command> cs = null;
		NaturalCLI ncli = null;
		ConsoleReader cr = null;
		Runnable action = new Runnable() { public void run() { mainThread.interrupt(); } };
		
		this.mainThread = Thread.currentThread();
		this.dout = new DeferredOutputStream(System.out, action);
		
		System.setOut(new PrintStream(dout));
		this.log = LogManager.getLogger();
		
		try {
			cs = getCommands();
			ncli = new NaturalCLI(cs);
			cr = new ConsoleReader(new InterruptableInputStream(System.in, 1), dout.original);
		} catch (InvalidSyntaxException e) {
			log.catching(Level.FATAL, e);
			System.exit(-1);
		} catch (IOException e) {
			log.catching(Level.FATAL, e);
			System.exit(-1);
		}
		
		this.cli = ncli;
		this.reader = cr;
	}
	
	private Launch(String[] args) {
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
		
		try {
			server.start();
		} catch (IOException e) {
			log.catching(Level.FATAL, e);
			System.exit(-1);
		}
		
		this.server = server;
	}
	
	public Set<Command> getCommands() throws InvalidSyntaxException {
		Set<Command> commands = new HashSet<Command>();
		
		commands.add(new Command("start", "Starts the server", new ICommandExecutor() {
			@Override
			public void execute(ParseResult pr) throws ExecutionException {
				try {
					server.start();
				} catch (IOException e) {
					log.catching(Level.WARN, e);
				}
			}
		}));
		
		commands.add(new Command("status", "Prints the server's status", new ICommandExecutor() {
			@Override
			public void execute(ParseResult pr) throws ExecutionException {
				try {
					dout.original.write(server.status().getBytes());
				} catch (IOException e) {
					log.catching(Level.WARN, e);
				}
			}
		}));
		
		commands.add(new Command("stop", "Stops the server", new ICommandExecutor() {
			@Override
			public void execute(ParseResult pr) throws ExecutionException {
				server.stop();
			}
		}));
		
		return commands;
	}

	public void run() {
		String line = null;
		
		while (server.isRunning()) {
			try {
				dout.runQueue();
			} catch (Exception e) {
				log.catching(Level.WARN, e);
			}
			
			try {
				Thread.interrupted();
				line = reader.readLine("> ", null);
			} catch (IOException e) {
				if (e.getCause() instanceof InterruptedException) {
					try {
						dout.original.write(new byte[] {0x08, 0x08});
					} catch (IOException e1) {
						log.catching(Level.WARN, e1);
					}
				} else
					log.catching(Level.WARN, e);
				continue;
			}
			
			if (line.length() == 0)
				continue;
			
			try {
				cli.execute(line);
			} catch (ExecutionException e) {
				if (e.getMessage().equals("No command matches."))
					System.err.println("Command '" + line + "' not found");
				else
					log.catching(Level.WARN, e);
			}
		}
	}
}
