package delayqueue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.DelayQueue;

import domain.Domain;

public class DelayQueueTest {

	public static void main(String[] args) {
		DelayQueue<Domain> delayQueue = new DelayQueue<Domain>();
		System.out.println("新建队列" + delayQueue);
		boolean add = delayQueue.add(getDomain());
		System.out.println("添加了一个元素" + delayQueue);
		delayQueue.clear();
		delayQueue.put(getDomain());
		System.out.println("put添加了一个元素" + delayQueue);
		Domain domain = getDomain();
		boolean contains = delayQueue.contains(domain);
		System.out.println(delayQueue + "\r\n队列中是否包含contains指定元素" + domain + "---" + contains);
		Domain element = delayQueue.element();// peek()
		Domain peek = delayQueue.peek();
		System.out.println("element获取但不移除首元素" + element + "---peek---" + peek);
		Iterator<Domain> iterator = delayQueue.iterator();
		System.out.println("获取队列的迭代器" + iterator);
		boolean offer = delayQueue.offer(getDomain());
		System.out.println("offer向队列中添加一个元素" + offer);
		ArrayList<Domain> array = new ArrayList<Domain>();
		delayQueue.remove(peek);

		Domain poll = delayQueue.poll();
		Domain remove = delayQueue.remove();// 就是用poll(), 获取不到会抛异常
		System.out.println("获取并移除首元素，延迟每到就返回null----" + poll + "----移除首元素" + remove);
		try {
			Domain take = delayQueue.take();
			System.out.println("获取并移除首元素，会等待延迟" + take);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		delayQueue.stream();// 流式编程还没看
		int drainTo = delayQueue.drainTo(array);

	}

	static Random r = new Random(5000);

	private static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}

}
