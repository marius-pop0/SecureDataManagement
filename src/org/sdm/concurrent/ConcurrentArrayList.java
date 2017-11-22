package org.sdm.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Mathew : 22/11/2017.
 */
public class ConcurrentArrayList<T> {

	private final Lock readLock;
	private final Lock writeLock;
	private final List<T> list;

	public ConcurrentArrayList() {
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
		this.list = new ArrayList<>();
	}

	public void add(T e) {
		writeLock.lock();
		try {
			list.add(e);
		} finally {
			writeLock.unlock();
		}
	}

	public T get(int index) {
		readLock.lock();
		T res;
		try {
			res = list.get(index);
		} finally {
			readLock.unlock();
		}
		return res;
	}

	public T remove(int index) {
		writeLock.lock();
		T res;
		try {
			res = list.remove(index);
		} finally {
			writeLock.unlock();
		}
		return res;
	}

	public boolean remove(T t) {
		writeLock.lock();
		boolean res;
		try {
			res = list.remove(t);
		} finally {
			writeLock.unlock();
		}
		return res;
	}


	public Iterator<T> iterator() {
		readLock.lock();
		try {
			return new ArrayList<>(list).iterator();
			//^ we iterate over an snapshot of our list
		} finally {
			readLock.unlock();
		}
	}
}
