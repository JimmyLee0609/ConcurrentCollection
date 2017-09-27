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
//		�����������һ���߳� put         һ���߳�take        ������--������ģʽ
		// ============���Ԫ��===================
		System.out.println("==================���Ԫ��============================");
		// ֱ�����Ԫ������ɹ�����true��������ʱ���ʧ�ܣ�����false
		boolean add = blockingQueue.add(getDomain());// offer
		boolean offer = blockingQueue.offer(getDomain());
		System.out.println("add���Ԫ���Ƿ�ɹ�" + add);
		System.out.println("offer���Ԫ���Ƿ�ɹ�" + offer);
		for (int i = 0; i < 30; i++) {
//			��30���߳����Ԫ�ء�
			new Thread(() -> {
				try {
//					������ʱ������ǰ�̣߳��п�λʱִ����ӣ������ѵȴ���Ӷ��С�
					blockingQueue.put(getDomain());
					System.out.println(Thread.currentThread().getName()+"ָ���������");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();;
		}
//		=======================��ȡԪ��==============================
System.out.println();
System.out.println();
System.out.println("===================��ȡԪ��======================");
		//		��ȡ�����Ƴ���Ԫ��
		Domain element = blockingQueue.element();
		System.out.println("element��ȡԪ��"+element);
//		��ȡ���Ƴ���Ԫ��
		Domain poll = blockingQueue.poll();
		System.out.println("poll��ȡԪ��"+poll);
//		��ȡ���Ƴ���Ԫ�أ������ǰ����û��Ԫ�أ��ȴ������ʱ���ٻ�ȡ������û�оͷ���null;
		Domain poll2 = blockingQueue.poll(1, TimeUnit.SECONDS);
		System.out.println("poll(1, TimeUnit.SECONDS)"+poll2);
//		��ȡ���Ƴ���Ԫ�أ������ǰ����û��Ԫ�أ��������̣߳��ȴ���ȡ
		Domain take = blockingQueue.take();
		System.out.println("take��ȡԪ��"+take);
//		=================����Ԫ��=================================
		System.out.println();
		System.out.println();
		System.out.println("================����Ԫ��======================");
		
		boolean contains = blockingQueue.contains(poll);
		boolean containsAll = blockingQueue.containsAll(new ArrayList<Domain>());//true   ????bug  
		System.out.println("contains����Ԫ����"+contains);
		System.out.println("containsAll����Ԫ����"+containsAll);
//		================�Ƴ�Ԫ��=======================
		System.out.println();
		System.out.println();
		System.out.println("===================�Ƴ�Ԫ��==================");
//		��ȡ���Ƴ���Ԫ��poll  �����ѵȴ���Ӷ���
		Domain remove = blockingQueue.remove();//poll
		System.out.println("remove��ȡ���Ƴ���Ԫ��"+remove);
//		�Ƴ�ָ��Ԫ�أ����ѵȴ���Ӷ���
		boolean remove2 = blockingQueue.remove(poll);
		System.out.println("remove(poll)�Ƴ�ָ��Ԫ���Ƿ�ɹ���"+remove2);
//		================ת��Ԫ��======================
		System.out.println();
		System.out.println();
		System.out.println("====================ת��Ԫ��============================");
//		�������е�Ԫ�ظ��Ƶ�������
		Object[] array = blockingQueue.toArray();
		System.out.println("toArray�����е�Ԫ�ظ��Ƶ�������"+Arrays.toString(array));
//		�������е�Ԫ�ظ��Ƶ�ָ�������У����ָ��������������㣬���÷��䴴��ͬ���͵Ĺ�����������
		Domain[] domains = blockingQueue.toArray(new Domain[] {});
		System.out.println("toArray(new Domain[] {})�����е�Ԫ�ظ��Ƶ�������\r\n"+Arrays.toString(domains));
//		�����е�Ԫ���Ƴ�����ӵ�ָ���ļ����С�
		ArrayList<Domain> list = new ArrayList<Domain>();
		ArrayList<Domain> list2 = new ArrayList<Domain>();
//		ϵ�Ǹ����е�����ȫ���Ƴ������ѵȴ���Ӷ���
		int drainTo = blockingQueue.drainTo(list);
		System.out.println("drainTo(list)ȫ���Ƴ������е�Ԫ�أ��Ƴ�����Ϊ"+drainTo+"\r\n���ϻ�ȡ����Ԫ��Ϊ"+Arrays.toString(list.toArray()));
		int drainTo2 = blockingQueue.drainTo(list2, 5);
		System.out.println(".drainTo(list2, 5, �Ƴ�������ָ������Ԫ�أ��Ƴ�����Ϊ"+drainTo2+"\r\n���ϻ�ȡ����Ԫ��Ϊ"+Arrays.toString(list2.toArray()));
//		��������е�Ԫ�أ����ѵȴ���Ӷ���		
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
