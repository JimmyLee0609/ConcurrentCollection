package synchronousqueue;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import domain.Domain;

public class SynchronousQueueTest {

	public static void main(String[] args) throws InterruptedException {
		SynchronousQueue<Domain> Queue = new SynchronousQueue<Domain>();
		normalCollectionMethod(Queue);
		for(int i=0;i<30;i++){
			new Thread(()->{
				try {
					Domain dom=getDomain();
					Queue.put(dom);
					System.out.println(Thread.currentThread().getName()+"保存"+dom);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();;
		}
		for(int i=0;i<30;i++){
			new Thread(()->{
				try {
					Domain take = Queue.take();
					System.out.println(Thread.currentThread().getName()+"获取"+take);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();;
		}
		
	}

	private static void normalCollectionMethod(SynchronousQueue<Domain> Queue) throws InterruptedException {
//		boolean add = Queue.add(getDomain()); // offer
		boolean offer = Queue.offer(getDomain());
		boolean offer3 = Queue.offer(getDomain());
		Domain poll = Queue.poll();
		System.out.println(offer+"---"+offer3+"----"+poll);
		Queue.put(getDomain());
		
		// 不好用
		Domain element = Queue.element();// peek 获取null值抛异常
		Domain peek = Queue.peek();// 返回null

		boolean offer2 = Queue.offer(getDomain(), 1, TimeUnit.SECONDS);
		Domain poll2 = Queue.poll(1, TimeUnit.SECONDS);

		Domain take = Queue.take();

		boolean contains = Queue.contains(getDomain());// 返回false
		int size = Queue.size();// 返回0
		int remainingCapacity = Queue.remainingCapacity();// 返回0

		Queue.clear();//空实现

		Domain remove = Queue.remove();// poll 获取null抛出异常
		boolean remove2 = Queue.remove(element);// false

		Object[] array = Queue.toArray();// new Object[0]

		Object[] array2 = Queue.toArray(array); // 设值 a[0] = null;
		boolean addAll = Queue.addAll(Queue);// 逐个添加
		int drainTo = Queue.drainTo(Queue);// e = poll()
		int drainTo2 = Queue.drainTo(Queue, 5);// poll
		boolean retainAll = Queue.retainAll(Queue);// false
	}

	static Random r = new Random(5000);

	private static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
