package com.lensflare.svh.cli;

import java.io.IOException;
import java.io.InputStream;

public class InterruptableInputStream extends InputStream {
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