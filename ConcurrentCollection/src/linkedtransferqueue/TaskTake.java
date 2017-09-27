package linkedtransferqueue;

import java.util.concurrent.LinkedTransferQueue;

import domain.Domain;

public class TaskTake implements Runnable {
	LinkedTransferQueue<Domain> queue;

	public TaskTake(LinkedTransferQueue<Domain> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		try {
			Domain take = queue.take();
			System.out.println(Thread.currentThread().getName() +"--get--"+ take);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
