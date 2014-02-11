package com.lensflare.svh.cli;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeferredOutputStream extends OutputStream {
	private final OutputStream original;
	private final Runnable action;
	private final Queue<Callable<?>> queued = new ConcurrentLinkedQueue<Callable<?>>();
	
	private boolean enabled = false;
	
	public DeferredOutputStream(OutputStream original, Runnable action) {
		this.original = original;
		this.action = action;
	}
	
	public synchronized boolean getEnabled() {
		return enabled;
	}
	
	public synchronized void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public void write(final int b) throws IOException {
		if (getEnabled()) {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.write(b); return null; } });
			action.run();
		} else
			original.write(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (getEnabled()) {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.write(b, off, len); return null; } });
			action.run();
		} else
			original.write(b);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		if (getEnabled()) {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.write(b); return null; } });
			action.run();
		} else
			original.write(b);
	}
	
	@Override
	public void flush() throws IOException {
		if (getEnabled()) {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.flush(); return null; } });
			action.run();
		} else
			original.flush();
	}

	@Override
	public void close() throws IOException {
		if (getEnabled()) {
			queued.add(new Callable<Object>() { public Object call() throws Exception { original.close(); return null; } });
			action.run();
		} else
			original.close();
	}

	public synchronized void runQueue() throws Exception {
		Iterator<Callable<?>> iter = queued.iterator();
		while (iter.hasNext()) {
			iter.next().call();
			iter.remove();
		}
	}
}