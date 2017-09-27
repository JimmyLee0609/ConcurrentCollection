package priorityblockingqueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PriorityBlockingQueueTest {

	public static void main(String[] args) throws InterruptedException {
		PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<Integer>();
		// =================添加元素===========================
		System.out.println("==================添加元素==========================");
		boolean add = queue.add(5);// offer
		queue.put(15);// offer
		boolean offer = queue.offer(12, 1, TimeUnit.SECONDS);// offer
		boolean offer2 = queue.offer(10);
		System.out.println("add(5)" + add);
		System.out.println("put(15)");
		System.out.println("offer(12, 1, TimeUnit.SECONDS)" + offer);
		System.out.println("offer(10)" + offer2);
		System.out.println();
		System.out.println();
		// ====================获取元素============================
		System.out.println("=================获取元素======================");
		// 获取但不移除元素
		Integer peek = queue.peek();
		Integer element = queue.element();// peek
		// 获取并移除首元素
		Integer poll2 = queue.poll();
		Integer poll3 = queue.poll();
		Integer poll4 = queue.poll();
		Integer poll5 = queue.poll();
		Integer poll6 = queue.poll();
		System.out.println("peek获取元素" + peek);
		System.out.println("element获取元素" + element);
		System.out.println("poll2获取元素" + poll2);
		System.out.println("poll3获取元素" + poll3);
		System.out.println("poll4获取元素" + poll4);
		System.out.println("poll5获取元素" + poll5);
		System.out.println("poll6获取元素" + poll6);
		new Thread(() -> {
			try {
				// 获取元素，获取到null就加入等待等待传入时间，再获取，还是没有就返回null
				Integer poll = queue.poll(1, TimeUnit.SECONDS);
				System.out.println(Thread.currentThread().getName() + "poll(1, TimeUnit.SECONDS)" + poll);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
		new Thread(() -> {
			try {
				// 获取元素，获取到null就加入等待队列，当前线程阻塞，锁没释放,后面会一直阻塞
				Integer take = queue.take();
				System.out.println(Thread.currentThread().getName()+"take" + take);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();

		// =================转换================================
		System.out.println();
		System.out.println();
		System.out.println("=================转换===========================");
		// 将队列的元素复制到数组中
		Object[] array = queue.toArray();
		Object[] array2 = queue.toArray(array);
		System.out.println(Arrays.toString(array));
		System.out.println(Arrays.toString(array2));
		// 将队列中的元素移除到指定的集合中
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		ArrayList<Integer> list = new ArrayList<Integer>();
		// 全部移除，移除的元素会添加到集合中
		int drainTo = queue.drainTo(arrayList);
		System.out.println("drainTo(arrayList)转换个数"+drainTo);
		System.out.println("转换后的集合"+Arrays.toString((arrayList.toArray())));
		// 移除指定个数，移除的元素会添加到集合中
		int drainTo2 = queue.drainTo(list, 5);
		System.out.println("drainTo(list, 5)转换个数"+drainTo2);
		System.out.println("转换后的集合"+Arrays.toString(list.toArray()));
		// =========================队列的大小===================================
		System.out.println("队列大小");
		System.out.println();
		System.out.println();
		int size = queue.size();
		int remainingCapacity = queue.remainingCapacity();
		System.out.println("队列中元素的个数"+size);
		System.out.println("队列中剩余容量"+remainingCapacity);
		// ====================清除元素========================
		System.out.println("清除元素");
		System.out.println();
		System.out.println();
		// 移除首元素，并返回该元素,如果是null就会抛出异常
		Integer remove = queue.remove();//poll
		System.out.println("remove()移除首元素，并返回"+remove);
		// 移除指定元素
		boolean remove2 = queue.remove(30);
		System.out.println("queue.remove(30)移除指定元素"+remove2);
		// 清除队列中全部元素
		queue.clear();
		
	}

}
