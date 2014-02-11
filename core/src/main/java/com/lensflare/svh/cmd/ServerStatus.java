package com.lensflare.svh.cmd;

import org.naturalcli.ParseResult;

import com.lensflare.svh.Server;
import com.lensflare.svh.anno.Help;
import com.lensflare.svh.anno.Syntax;

@Syntax("status")
@Help("Prints the server's status")
public class ServerStatus extends ServerCommand {
	public ServerStatus(Server server) { super(server); }

	@Override
	protected void safeExecute(ParseResult pr) throws Exception {
		System.out.println(getServer().status(false));
	}
}
