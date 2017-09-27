package linkedtransferqueue;

import java.util.concurrent.LinkedTransferQueue;

import domain.Domain;

public class Task implements Runnable {
	LinkedTransferQueue<Domain> queue;
	public Task(LinkedTransferQueue<Domain> queue) {
		this.queue=queue;
	}

	@Override
	public void run() {
		queue.put(LinkedTransferQueueTest.getDomain());
		System.out.println(Thread.currentThread().getName()+"put½ø³Ì");
	}

}
