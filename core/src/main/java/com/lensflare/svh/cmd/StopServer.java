package com.lensflare.svh.cmd;

import org.naturalcli.ParseResult;

import com.lensflare.svh.Server;
import com.lensflare.svh.anno.Help;
import com.lensflare.svh.anno.Syntax;

@Syntax("stop")
@Help("Stops the server")
public class StopServer extends ServerCommand {
	public StopServer(Server server) { super(server); }

	@Override
	protected void safeExecute(ParseResult pr) throws Exception {
		getServer().stop();
	}
}
