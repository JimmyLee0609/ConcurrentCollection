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
		System.out.println("==================���=========================");
		boolean add = queue.add(getDomain()); // xfer(e, true, ASYNC, 0);
		boolean add2 = queue.add(getDomain());
		boolean offer = queue.offer(getDomain()); // xfer(e, true, ASYNC, 0);
		boolean offer2 = queue.offer(getDomain(), 1, TimeUnit.SECONDS); // xfer(e, true, ASYNC, 0);
		queue.put(getDomain()); // xfer(e, true, ASYNC, 0);

		System.out.println("===============��ȡ�ȴ����Ѷ��д�С==========================");
		int waitingConsumerCount = queue.getWaitingConsumerCount(); // countOfMode(false);
		System.out.println("getWaitingConsumerCount--" + waitingConsumerCount);
		System.out.println("===============��ȡ==================");
		Domain peek = queue.peek(); // firstDataItem();
		Domain poll = queue.poll(); // xfer(null, false, NOW, 0)
		Domain poll2 = queue.poll(1, TimeUnit.SECONDS);// E e = xfer(null, false, TIMED, unit.toNanos(timeout));
		Domain take = queue.take(); // E e = xfer(null, false, SYNC, 0);
		Domain element = queue.element(); // peek();
		System.out.println("================����========================");
		boolean contains = queue.contains(peek);// ��ͷ��ʼ��
		System.out.println("==================Transfer=��=======================");
		// ����������еȴ���ֱ�Ӹ��ȴ��ߡ�
		queue.transfer(getDomain()); // xfer(e, true, SYNC, 0)
		boolean tryTransfer = queue.tryTransfer(getDomain(), 1, TimeUnit.SECONDS);// xfer(e,  true, TIMED, unit.toNanos(timeout)
		boolean tryTransfer2 = queue.tryTransfer(getDomain(), 1, TimeUnit.SECONDS);
		System.out.println("=========================��С==========================");
		int remainingCapacity = queue.remainingCapacity(); // Integer.MAX_VALUE;
		int size = queue.size(); // countOfMode(true);
		System.out.println("=================�Ƴ�=========================");
		Domain remove = queue.remove(); // E x = poll();
		boolean remove2 = queue.remove(remove); // �Ӷ���ͷ��ʼ��
		System.out.println("==================ת��===========================");
		Object[] array = queue.toArray(); // �½����飬����Ԫ�� ������
		Object[] array2 = queue.toArray(array); // �½��ض��������飬����Ԫ��
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
