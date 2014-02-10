package com.lensflare.svh.minecraft;

import java.io.IOException;
import java.net.Socket;

import com.lensflare.svh.Server;
import com.lensflare.svh.impl.Connection;

public class ConnectionPost17 extends Connection {
	public ConnectionPost17(Server server, Socket socket) {
		super(server, socket);
	}

	@Override
	public void end() throws IOException {
		if (!isActive())
			return;
		
		String reason = "";
		int rLength = reason.length();
		int packetID = 0x40;
		int pLength = rLength + 3;
		
		byte[] data = new byte[pLength];
		data[0] = (byte)pLength;
		data[1] = (byte)packetID;
		data[2] = (byte)rLength;
		System.arraycopy(reason.getBytes(), 0, data, 3, rLength);
		
		getSocket().getOutputStream().write(data);
		
		super.end();
	}
}
