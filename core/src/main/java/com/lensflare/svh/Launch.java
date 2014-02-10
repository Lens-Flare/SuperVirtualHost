package com.lensflare.svh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.naturalcli.Command;
import org.naturalcli.ExecutionException;
import org.naturalcli.ICommandExecutor;
import org.naturalcli.InvalidSyntaxException;
import org.naturalcli.NaturalCLI;
import org.naturalcli.ParseResult;

import jline.console.ConsoleReader;

public class Launch {
	private final Server server;
	
	private Launch(String[] args) throws FileNotFoundException {
		String configPath = "config.yml";
		if (args.length > 0)
			configPath = args[0];
		this.server = com.lensflare.svh.impl.Server.load(new File(configPath));
	}
	
	public static void main(String[] args) throws IOException, InvalidSyntaxException, ExecutionException {
		Launch l = new Launch(args);
		NaturalCLI ncli = new NaturalCLI(l.getCommands());
		ConsoleReader cr = new ConsoleReader();
		
		if (l.server == null)
			return;
		
		l.server.start();
		cr.setPrompt("> ");
		while (l.server.isRunning())
			ncli.execute(cr.readLine());
	}
	
	public Set<Command> getCommands() throws InvalidSyntaxException {
		Set<Command> commands = new HashSet<Command>();
		
		commands.add(new Command("start", "Starts the server", new ICommandExecutor() {
			@Override
			public void execute(ParseResult pr) throws ExecutionException {
				try {
					server.start();
				} catch (IOException e) {
					e.printStackTrace();
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
}
