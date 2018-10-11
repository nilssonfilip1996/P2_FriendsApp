package com.example.nilss.friendsintheworld;

import android.util.Log;

public class RunOnThread {
	private static final String TAG = "RunOnThread";
	private Buffer<Runnable> buffer = new Buffer<Runnable>();
	private Worker worker;
	
	public void start() {
		if(worker==null) {
		    worker = new Worker();
		    worker.start();
			Log.d(TAG, "start: WORKER STARTED!");
		}
	}
	
	public void stop() {
		if(worker!=null) {
		    worker.interrupt();
		    worker=null;
		}
	}

	
	public void execute(Runnable runnable) {
		buffer.put(runnable);
	}
	
	private class Worker extends Thread {
		public void run() {
			Runnable runnable;
			while(worker!=null) {
				try {
					runnable = buffer.get();
					runnable.run();
				} catch (InterruptedException e) {
					worker=null;
				}
			}
		}
	}
}
