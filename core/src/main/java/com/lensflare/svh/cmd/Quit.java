package com.lensflare.svh.cmd;

import org.naturalcli.ParseResult;

import com.lensflare.svh.Server;
import com.lensflare.svh.anno.Help;
import com.lensflare.svh.anno.Syntax;

@Syntax("quit")
@Help("Exits the system")
public class Quit extends ServerCommand {
	public Quit(Server server) { super(server); }

	@Override
	protected void safeExecute(ParseResult pr) throws Exception {
		getServer().exit(0);
	}
}
