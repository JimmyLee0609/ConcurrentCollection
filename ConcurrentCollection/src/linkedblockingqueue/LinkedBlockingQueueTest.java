package linkedblockingqueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import domain.Domain;

public class LinkedBlockingQueueTest {

	public static void main(String[] args) throws InterruptedException {
		LinkedBlockingQueue<Domain> blockingQueue = new LinkedBlockingQueue<Domain>(10);
//		经典的例子是一个线程 put         一个线程take        生产者--消费者模式
		// ============添加元素===================
		System.out.println("==================添加元素============================");
		// 直接添加元素如果成功返回true，队列满时添加失败，返回false
		boolean add = blockingQueue.add(getDomain());// offer
		boolean offer = blockingQueue.offer(getDomain());
		System.out.println("add添加元素是否成功" + add);
		System.out.println("offer添加元素是否成功" + offer);
		for (int i = 0; i < 30; i++) {
//			开30条线程添加元素。
			new Thread(() -> {
				try {
//					队列满时阻塞当前线程，有空位时执行添加，并唤醒等待添加队列。
					blockingQueue.put(getDomain());
					System.out.println(Thread.currentThread().getName()+"指定添加任务");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();;
		}
//		=======================获取元素==============================
System.out.println();
System.out.println();
System.out.println("===================获取元素======================");
		//		获取但不移除首元素
		Domain element = blockingQueue.element();
		System.out.println("element获取元素"+element);
//		获取并移除首元素
		Domain poll = blockingQueue.poll();
		System.out.println("poll获取元素"+poll);
//		获取并移除首元素，如果当前队列没有元素，等待传入的时间再获取，还是没有就返回null;
		Domain poll2 = blockingQueue.poll(1, TimeUnit.SECONDS);
		System.out.println("poll(1, TimeUnit.SECONDS)"+poll2);
//		获取并移除首元素，如果当前队列没有元素，就阻塞线程，等待获取
		Domain take = blockingQueue.take();
		System.out.println("take获取元素"+take);
//		=================包含元素=================================
		System.out.println();
		System.out.println();
		System.out.println("================包含元素======================");
		
		boolean contains = blockingQueue.contains(poll);
		boolean containsAll = blockingQueue.containsAll(new ArrayList<Domain>());//true   ????bug  
		System.out.println("contains包含元素吗？"+contains);
		System.out.println("containsAll包含元素吗？"+containsAll);
//		================移除元素=======================
		System.out.println();
		System.out.println();
		System.out.println("===================移除元素==================");
//		获取并移除首元素poll  ，唤醒等待添加队列
		Domain remove = blockingQueue.remove();//poll
		System.out.println("remove获取并移除首元素"+remove);
//		移除指定元素，唤醒等待添加队列
		boolean remove2 = blockingQueue.remove(poll);
		System.out.println("remove(poll)移除指定元素是否成功？"+remove2);
//		================转移元素======================
		System.out.println();
		System.out.println();
		System.out.println("====================转移元素============================");
//		将队列中的元素复制到数组中
		Object[] array = blockingQueue.toArray();
		System.out.println("toArray将队列的元素复制到数组中"+Arrays.toString(array));
//		将队列中的元素复制到指定数组中，如果指定数组的容量不足，将用反射创建同类型的够容量的数组
		Domain[] domains = blockingQueue.toArray(new Domain[] {});
		System.out.println("toArray(new Domain[] {})将队列的元素复制到数组中\r\n"+Arrays.toString(domains));
//		将队列的元素移除并添加到指定的集合中。
		ArrayList<Domain> list = new ArrayList<Domain>();
		ArrayList<Domain> list2 = new ArrayList<Domain>();
//		系那个队列的任务全部移除，唤醒等待添加队列
		int drainTo = blockingQueue.drainTo(list);
		System.out.println("drainTo(list)全部移除队列中的元素，移除个数为"+drainTo+"\r\n集合获取到的元素为"+Arrays.toString(list.toArray()));
		int drainTo2 = blockingQueue.drainTo(list2, 5);
		System.out.println(".drainTo(list2, 5, 移除队列中指定个数元素，移除个数为"+drainTo2+"\r\n集合获取到的元素为"+Arrays.toString(list2.toArray()));
//		清除队列中的元素，唤醒等待添加队列		
		blockingQueue.clear();
		Thread.sleep(100);
		System.out.println(Arrays.toString(blockingQueue.toArray()));
	}

	static Random r = new Random(5000);

	private static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
