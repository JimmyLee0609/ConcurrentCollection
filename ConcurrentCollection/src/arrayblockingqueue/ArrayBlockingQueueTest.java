package arrayblockingqueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import domain.Domain;

public class ArrayBlockingQueueTest {

	public static void main(String[] args) throws InterruptedException {
		ArrayBlockingQueue<Domain> queue = new ArrayBlockingQueue<Domain>(10);
		// 新建10容量的有界队列
		System.out.println("==================添加元素====================");
		// ==================添加元素===================
		// 直接添加，添加成功返回true,不成功返回false
		boolean add = queue.add(getDomain());
		boolean offer = queue.offer(getDomain());
		System.out.println("first-" + add + " ----second-" + offer);

		for (int i = 0; i < 50; i++) {
			new Thread(() -> {
				// 添加元素到队列，队列满了就会添加到等待队列，阻塞当前线程。
				try {
					queue.put(getDomain());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("第" +Thread.currentThread().getName() + "个put");
			}
			).start();
		}
		// 添加元素到队列，如果队列满了就，等待传入的时间再添加，成功返回true,失败返回false，不会阻塞当前线程，
		boolean offer2 = queue.offer(getDomain(), 3, TimeUnit.SECONDS);
		System.out.println("offer第二次是否成功" + offer2);
		int remainingCapacity = queue.remainingCapacity();
		int size = queue.size();
		System.out.println("打印当前队列的容量" + remainingCapacity);
		System.out.println("打印当前队列的大小" + size);
		System.out.println();
		System.out.println();
		System.out.println("======================获取元素=================");
		// ===================获取元素=====================================
		// 获取但不删除首元素，首元素由takeIndex来维持
		Domain element = queue.element();// peek（），元素为空抛异常
		Domain peek = queue.peek();
		System.out.println("element" + element + "------------peek" + peek);
		// 获取并删除首元素,如果队列有元素就返回元素，没有就返回空
		Domain poll = queue.poll();
		System.out.println("poll" + poll);
		// 获取并删除首元素,如果队列有元素就返回元素，没有就等待传入时间再获取，没有就返回空
		Domain poll2 = queue.poll(1, TimeUnit.SECONDS);
		System.out.println("poll(1, TimeUnit.SECONDS)" + poll2);
		// 获取首元素，队列没有就等待，直到有元素返回，不会阻塞该线程。
		Domain take = queue.take();
		System.out.println("take()" + take);

		// 转换保存方式
		System.out.println();
		System.out.println();
		System.out.println("================转换==================");

		Object[] array = queue.toArray();
		Object[] array2 = queue.toArray(array);
		System.out.println(Arrays.toString(array));
		System.out.println(Arrays.toString(array2));
		ArrayList<Domain> list = new ArrayList<Domain>();
		ArrayList<Domain> list2 = new ArrayList<Domain>();
//		删除队列中的元素并保存到另外的容器中，
		int drainTo = queue.drainTo(list);
		Thread.sleep(500);
		int drainTo2 = queue.drainTo(list2, 5);
		System.out.println(drainTo + "转换了全部元素" + list);
		System.out.println(drainTo2 + "转换了5个元素" + list2);

		System.out.println();
		System.out.println();
		System.out.println("================队列是否包含元素==================");
		// =====================队列是否包含元素=================
		boolean contains = queue.contains(poll2);
		System.out.println(Arrays.toString(queue.toArray()));
		System.out.println("元素" + poll2 + "\r\n是否在队列中" + contains);

		// ================清除元素==================
		System.out.println();
		System.out.println();
		System.out.println("==============删除元素=================");
		Domain remove = queue.remove();// poll()
		boolean remove2 = queue.remove(remove);// 使用迭代器删除
		System.out.println("remove" + remove);
		System.out.println("remove(remove)是否成功" + remove2);
		queue.clear();
		System.out.println("clear后");
		System.out.println(Arrays.toString(queue.toArray()));
	}

	static Random r = new Random(5000);

	private static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
