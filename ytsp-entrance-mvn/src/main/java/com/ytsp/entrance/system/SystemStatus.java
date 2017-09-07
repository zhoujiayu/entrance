/*
 * $Id: SystemStatus.java 240 2011-08-06 11:03:29Z louis $
 * All rights reserved
 */
package com.ytsp.entrance.system; 

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 系统状态记录，包括：是否正在同步资源、当前负载播放量等。
 * 该记录模型为多线程访问设计，使用重入读写锁。
 * 
 * @author Louis
 */
public class SystemStatus {

	/** 同步资源锁 */
	private final ReentrantRWL srLock = new ReentrantRWL();
	/** 输出流锁 */
	private final ReentrantRWL sosLock = new ReentrantRWL();
	
	/** 是否正在同步资源 */
	private volatile boolean isSyncResource;
	/** 当前负载播放输出流数量 */
	private volatile int streamOutputSize;
	
	public boolean isSyncResource() {
		try {
			srLock.readLock.lock();
			return isSyncResource;
		} finally {
			srLock.readLock.unlock();
		}
	}

	public void setSyncResource(boolean isSyncResource) {
		try {
			srLock.writeLock.lock();
			this.isSyncResource = isSyncResource;
		} finally {
			srLock.writeLock.unlock();
		}
	}

	public int getStreamOutputSize() {
		try {
			sosLock.readLock.lock();
			return streamOutputSize;
		} finally {
			sosLock.readLock.unlock();
		}
	}

	public void setStreamOutputSize(int streamOutputSize) {
		try {
			sosLock.writeLock.lock();
			this.streamOutputSize = streamOutputSize;
		} finally {
			sosLock.writeLock.unlock();
		}
	}
	
	public void flyStreamOutputSize(int flySize) {
		setStreamOutputSize(streamOutputSize + flySize);
	}

	class ReentrantRWL {
		
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		Lock readLock = lock.readLock();
		Lock writeLock = lock.writeLock();
		
	}
	
}
