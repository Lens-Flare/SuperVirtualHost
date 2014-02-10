package com.lensflare.svh;

import java.io.IOException;
import java.net.Socket;

public interface Connection {
	public Socket getSocket();
	public Socket getTarget();
	
	public byte[] getData() throws IOException;
	public byte[] getUnsentData(int index);
	
	public void forwardUnsentData(Socket socket) throws IOException;
	public void forwardToSocket(Socket socket) throws IOException;

	public boolean isActive();
	public boolean isForwarding();
	public void end() throws IOException;
}
