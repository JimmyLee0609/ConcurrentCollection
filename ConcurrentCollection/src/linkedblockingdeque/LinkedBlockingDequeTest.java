package linkedblockingdeque;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import domain.Domain;

public class LinkedBlockingDequeTest {

	public static void main(String[] args) throws InterruptedException {
		LinkedBlockingDeque<Domain> deque = new LinkedBlockingDeque<Domain>();
//		==================添加==========================
		System.out.println("===================添加=========================");
//		add，offer没指明就后面加
		boolean add = deque.add(getDomain());															//addLast
		deque.addFirst(getDomain());																			//offerFirst
		deque.addLast(getDomain());																			//offerLast
		boolean offer = deque.offer(getDomain());														//offerLast
		boolean offerFirst = deque.offerFirst(getDomain());  										    //linkFirst
		boolean offerLast = deque.offerLast(getDomain());   										   //linkLast
//		push没指明就前面加
		deque.push(getDomain());																						//addFirst
//		添加没成功，就等待传入时间再添加一次，还没成功就返回false
		boolean offer2 = deque.offer(getDomain(), 1, TimeUnit.SECONDS); 					  //offerLast
		boolean offerFirst2 = deque.offerFirst(getDomain(), 1, TimeUnit.SECONDS); 		 //linkFirst
		boolean offerLast2 = deque.offerLast(getDomain(), 1, TimeUnit.SECONDS); 		 //linkLast
//		put没指明就后面加，put添加没成功，就阻塞线程，等待
		deque.put(getDomain());																						//putLast
		for(int i=0;i<300;i++){
			new Thread(()->{
				try {
					deque.put(getDomain());
					System.out.println(Thread.currentThread().getName()+"添加元素");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}).start();
		}
		deque.putFirst(getDomain());																					//linkFirst
		deque.putLast(getDomain());																					//linkLast
//		====================获取=========================
		System.out.println();
		System.out.println();
		System.out.println("====================获取==========================");
//		获取但不移除，就是查看first last元素
		Domain peek = deque.peek();  													//peekFirst
		Domain element = deque.element();											//getFirst
		Domain first = deque.getFirst();													//peekFirst
		Domain last = deque.getLast();													//peekLast
		Domain peekFirst = deque.peekFirst();										//first.item
		Domain peekLast = deque.peekLast();										//last.item
//		获取并移除元素，传入了时间，就先获取一遍，获取不到，等待传入时间再获取一遍，失败，返回false
		Domain poll = deque.poll();															//pollFirst
		Domain poll2 = deque.poll(1, TimeUnit.SECONDS);						//pollFirst
		Domain pollFirst = deque.pollFirst();												//unlinkFirst
		Domain pollFirst2 = deque.pollFirst(1, TimeUnit.SECONDS);			//unlinkFirst
		Domain pollLast = deque.pollLast();												//unlinkLast
		Domain pollLast2 = deque.pollLast(1, TimeUnit.SECONDS);				//unlinkLast
		Domain pop = deque.pop();																//removeFirst
//		获取不到就阻塞线程等待
		Domain take = deque.take();																//takeFirst
		Domain takeFirst = deque.takeFirst();												//unlinkFirst
		Domain takeLast = deque.takeLast();												//unlinkLast
//		=====================转移==================================
		System.out.println();
		System.out.println();
		System.out.println("================转移====================");
//		复制队列中的元素并保存到新的数组中
		Object[] array = deque.toArray();
		Object[] array2 = deque.toArray(array);
		System.out.println("toArray复制的元素"+Arrays.toString(array2));
//		将队列中的元素逐个删除，并转移到指定的集合
		ArrayList<Domain> c = new ArrayList<Domain>();
		ArrayList<Domain> c1 = new ArrayList<Domain>();
		int drainTo = deque.drainTo(c);								//drainTo(c1,Integer.MAX_VALUE)
		int drainTo2 = deque.drainTo(c1,5);							//unlinkFirst
		System.out.println("drainTo转移的元素个数"+drainTo);
		System.out.println("drainTo(c1,5)转移的元素个数"+drainTo2);
		
//		========================大小==============================
	System.out.println();
	System.out.println();
	System.out.println("=================大小=========================");
		int remainingCapacity = deque.remainingCapacity();
		int size = deque.size();
		System.out.println("剩余容量"+remainingCapacity);
		System.out.println("队列大小"+size);
//		==========================包含===============================
		boolean contains = deque.contains(offerFirst2);
				
//		===================移除========================
		Domain remove = deque.remove();																	//removeFirst
		boolean remove2 = deque.remove(pop);													//removeFirstOccurrence
		Domain removeFirst = deque.removeFirst();												//pollFirst 获取null值抛异常
		Domain removeLast = deque.removeLast();													//pollLast 获取null值抛异常
		boolean removeFirstOccurrence = deque.removeFirstOccurrence(element);//unlink
		boolean removeLastOccurrence = deque.removeLastOccurrence(last);				//unlink
		boolean removeAll = deque.removeAll(deque);												//迭代器删除队列中的全部元素
		Thread.sleep(600);
		
		deque.clear();//从头开始接连
	}
	static Random r = new Random(5000);

	private static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
