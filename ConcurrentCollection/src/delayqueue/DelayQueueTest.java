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
		System.out.println("�½�����" + delayQueue);
		boolean add = delayQueue.add(getDomain());
		System.out.println("�����һ��Ԫ��" + delayQueue);
		delayQueue.clear();
		delayQueue.put(getDomain());
		System.out.println("put�����һ��Ԫ��" + delayQueue);
		Domain domain = getDomain();
		boolean contains = delayQueue.contains(domain);
		System.out.println(delayQueue + "\r\n�������Ƿ����containsָ��Ԫ��" + domain + "---" + contains);
		Domain element = delayQueue.element();// peek()
		Domain peek = delayQueue.peek();
		System.out.println("element��ȡ�����Ƴ���Ԫ��" + element + "---peek---" + peek);
		Iterator<Domain> iterator = delayQueue.iterator();
		System.out.println("��ȡ���еĵ�����" + iterator);
		boolean offer = delayQueue.offer(getDomain());
		System.out.println("offer����������һ��Ԫ��" + offer);
		ArrayList<Domain> array = new ArrayList<Domain>();
		delayQueue.remove(peek);

		Domain poll = delayQueue.poll();
		Domain remove = delayQueue.remove();// ������poll(), ��ȡ���������쳣
		System.out.println("��ȡ���Ƴ���Ԫ�أ��ӳ�ÿ���ͷ���null----" + poll + "----�Ƴ���Ԫ��" + remove);
		try {
			Domain take = delayQueue.take();
			System.out.println("��ȡ���Ƴ���Ԫ�أ���ȴ��ӳ�" + take);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		delayQueue.stream();// ��ʽ��̻�û��
		int drainTo = delayQueue.drainTo(array);

	}

	static Random r = new Random(5000);

	private static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}

}
