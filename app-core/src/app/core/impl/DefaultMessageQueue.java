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
		if(queue==null)return null;
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
		if(queue==null)return;
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
		return (queue==null)||queue.isEmpty();
	}

	@Override
	public int size() {
		if(queue==null)return 0;
		return queue.size();
	}

	@Override
	public Object getFirst() {
		if(queue==null)return null;
		return queue.element();
	}

	@Override
	public boolean isFulled() {
		if(queue==null)return false;
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
		if(queue==null)return list;
		queue.drainTo(list);
		bytesSize = 0;
		return list;
	}

	@Override
	public int getBytesSize() {
		return bytesSize;
	}
	
	@Override
	public void destory() {
		BlockingQueue<Object> tmp = queue;
		queue = null;
		tmp.clear();
	}
}
