package app.core.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import app.core.MessageQueue;

public class DefaultMessageQueue implements MessageQueue {
	private BlockingQueue<Object> queue;
	private int capacity;
	private int bytesSize;

	public DefaultMessageQueue(int capacity) {
		this.capacity = capacity;
		queue = new ArrayBlockingQueue<Object>(capacity);
	}

	@Override
	public Object removeFirst() {
		try {
			Object message = queue.take();
			if (message instanceof ByteBuffer) {
				bytesSize -= ((ByteBuffer) message).limit();
			}
			return message;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void putLast(Object message) {
		try {
			queue.put(message);
			if (message instanceof ByteBuffer) {
				bytesSize += ((ByteBuffer) message).limit();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public Object getFirst() {
		return queue.element();
	}

	@Override
	public boolean isFulled() {
		return queue.size() >= capacity;
	}

	@Override
	public void putLastAll(Collection<Object> list) {
		for (Iterator<Object> iterator = list.iterator(); iterator.hasNext();) {
			putLast(iterator.next());
		}
	}

	@Override
	public Collection<Object> removeAll() {
		Collection<Object> list = new ArrayList<Object>();
		queue.drainTo(list);
		bytesSize = 0;
		return list;
	}

	@Override
	public int getBytesSize() {
		return bytesSize;
	}
}
