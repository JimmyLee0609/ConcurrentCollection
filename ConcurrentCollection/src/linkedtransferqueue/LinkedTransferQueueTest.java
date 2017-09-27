package linkedtransferqueue;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import domain.Domain;

public class LinkedTransferQueueTest {

	public static void main(String[] args) throws InterruptedException {
		LinkedTransferQueue<Domain> queue = new LinkedTransferQueue<Domain>();
		// method(queue);

		Thread t1 = new Thread(() -> {
			for (int i = 0; i < 500; i++) {
//				new Thread(new Task(queue)).start();
					queue.put(getDomain());
					System.out.println("get"+queue.size());
			}
		});
		Thread t2 = new Thread(() -> {
			for (int i = 0; i < 500; i++) {
//				new Thread(new TaskTake(queue)).start();
				try {
					Domain take = queue.take();
					System.out.println(take);
					System.out.println(queue.size());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
		t1.start();
		t2.start();
		
		System.out.println();
	}

	private static void method(LinkedTransferQueue<Domain> queue) throws InterruptedException {
		System.out.println("==================添加=========================");
		boolean add = queue.add(getDomain()); // xfer(e, true, ASYNC, 0);
		boolean add2 = queue.add(getDomain());
		boolean offer = queue.offer(getDomain()); // xfer(e, true, ASYNC, 0);
		boolean offer2 = queue.offer(getDomain(), 1, TimeUnit.SECONDS); // xfer(e, true, ASYNC, 0);
		queue.put(getDomain()); // xfer(e, true, ASYNC, 0);

		System.out.println("===============获取等待消费队列大小==========================");
		int waitingConsumerCount = queue.getWaitingConsumerCount(); // countOfMode(false);
		System.out.println("getWaitingConsumerCount--" + waitingConsumerCount);
		System.out.println("===============获取==================");
		Domain peek = queue.peek(); // firstDataItem();
		Domain poll = queue.poll(); // xfer(null, false, NOW, 0)
		Domain poll2 = queue.poll(1, TimeUnit.SECONDS);// E e = xfer(null, false, TIMED, unit.toNanos(timeout));
		Domain take = queue.take(); // E e = xfer(null, false, SYNC, 0);
		Domain element = queue.element(); // peek();
		System.out.println("================包含========================");
		boolean contains = queue.contains(peek);// 从头开始查
		System.out.println("==================Transfer=存=======================");
		// 如果队列中有等待的直接给等待者。
		queue.transfer(getDomain()); // xfer(e, true, SYNC, 0)
		boolean tryTransfer = queue.tryTransfer(getDomain(), 1, TimeUnit.SECONDS);// xfer(e,  true, TIMED, unit.toNanos(timeout)
		boolean tryTransfer2 = queue.tryTransfer(getDomain(), 1, TimeUnit.SECONDS);
		System.out.println("=========================大小==========================");
		int remainingCapacity = queue.remainingCapacity(); // Integer.MAX_VALUE;
		int size = queue.size(); // countOfMode(true);
		System.out.println("=================移除=========================");
		Domain remove = queue.remove(); // E x = poll();
		boolean remove2 = queue.remove(remove); // 从队列头开始找
		System.out.println("==================转换===========================");
		Object[] array = queue.toArray(); // 新建数组，复制元素 迭代器
		Object[] array2 = queue.toArray(array); // 新建特定类型数组，复制元素
		ArrayList<Domain> list = new ArrayList<Domain>();
		int drainTo = queue.drainTo(list); // e = poll()) c.add(e)
		int drainTo2 = queue.drainTo(list, 5); // e = poll()) c.add(e)

		queue.clear(); // poll()
	}

	static Random r = new Random(5000);

	static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
