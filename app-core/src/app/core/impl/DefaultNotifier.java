package app.core.impl;

import java.nio.channels.SelectableChannel;
import java.util.LinkedList;
import java.util.List;

import app.core.Connection;
import app.core.Connector;
import app.core.Notifier;
import app.core.ServerHandler;

public class DefaultNotifier<R, S> implements Notifier<R, S> {
	private List<ServerHandler<R, S>> listeners;

	public DefaultNotifier() {
		this.listeners = new LinkedList<ServerHandler<R, S>>();
	}

	@Override
	public boolean isEmpty() {
		return listeners.isEmpty();
	}

	@Override
	public void fireOnAccept() {
		int length = listeners.size();
		try {
			for (int i = 0; i < length; i++)
				listeners.get(i).onAccept();
		} catch (Throwable e) {
			fireOnError(null, e);
		}
	}

	@Override
	public R fireOnAccepted(SelectableChannel sc, R prev) throws Exception {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			prev = listeners.get(i).onAccepted(sc, prev);
		return prev;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void fireOnClosed(R request) {
		int length = listeners.size();
		try {
			for (int i = 0; i < length; i++)
				listeners.get(i).onClosed(request);
		} catch (Throwable e) {
			fireOnError((S) ((Connection) request).getSession(), e);
		}
	}

	@Override
	public void fireOnError(S request, Throwable e) {
		int length = listeners.size();
		try {
			for (int i = 0; i < length; i++)
				listeners.get(i).onError(request, e);
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean fireOnRead(R request) {
		boolean suc = false;
		int length = listeners.size();
		try {
			for (int i = 0; i < length; i++)
				suc = listeners.get(i).onRead(request, suc);
		} catch (Throwable e) {
			fireOnError((S) ((Connection) request).getSession(), e);
		}
		return suc;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean fireOnWrite(R request) {
		boolean bool = false;
		int length = listeners.size();
		try {
			for (int i = 0; i < length; i++)
				bool = listeners.get(i).onWrite(request, bool);
		} catch (Throwable e) {
			fireOnError((S) ((Connection) request).getSession(), e);
		}
		return bool;
	}

	@Override
	public void addHandler(ServerHandler<R, S> listener) {
		if (this.listeners.contains(listener) == false) {
			this.listeners.add(listener);
		}
	}

	@Override
	public void removeHandler(ServerHandler<R, S> listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void init(Connector<R, S> connector) {
		int length = listeners.size();
		for (int i = 0; i < length; i++)
			listeners.get(i).init(connector);
	}

	@Override
	public void destory() {
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).destory();
	}
}
