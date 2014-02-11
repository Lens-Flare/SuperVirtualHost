package com.lensflare.svh.cmd;

import org.naturalcli.ParseResult;

import com.lensflare.svh.Server;
import com.lensflare.svh.anno.Help;
import com.lensflare.svh.anno.Syntax;

@Syntax("start")
@Help("Starts the server")
public class StartServer extends ServerCommand {
	public StartServer(Server server) { super(server); }

	@Override
	protected void safeExecute(ParseResult pr) throws Exception {
		getServer().start();
	}
}
