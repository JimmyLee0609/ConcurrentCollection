package priorityblockingqueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PriorityBlockingQueueTest {

	public static void main(String[] args) throws InterruptedException {
		PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<Integer>();
		// =================���Ԫ��===========================
		System.out.println("==================���Ԫ��==========================");
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
		// ====================��ȡԪ��============================
		System.out.println("=================��ȡԪ��======================");
		// ��ȡ�����Ƴ�Ԫ��
		Integer peek = queue.peek();
		Integer element = queue.element();// peek
		// ��ȡ���Ƴ���Ԫ��
		Integer poll2 = queue.poll();
		Integer poll3 = queue.poll();
		Integer poll4 = queue.poll();
		Integer poll5 = queue.poll();
		Integer poll6 = queue.poll();
		System.out.println("peek��ȡԪ��" + peek);
		System.out.println("element��ȡԪ��" + element);
		System.out.println("poll2��ȡԪ��" + poll2);
		System.out.println("poll3��ȡԪ��" + poll3);
		System.out.println("poll4��ȡԪ��" + poll4);
		System.out.println("poll5��ȡԪ��" + poll5);
		System.out.println("poll6��ȡԪ��" + poll6);
		new Thread(() -> {
			try {
				// ��ȡԪ�أ���ȡ��null�ͼ���ȴ��ȴ�����ʱ�䣬�ٻ�ȡ������û�оͷ���null
				Integer poll = queue.poll(1, TimeUnit.SECONDS);
				System.out.println(Thread.currentThread().getName() + "poll(1, TimeUnit.SECONDS)" + poll);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
		new Thread(() -> {
			try {
				// ��ȡԪ�أ���ȡ��null�ͼ���ȴ����У���ǰ�߳���������û�ͷ�,�����һֱ����
				Integer take = queue.take();
				System.out.println(Thread.currentThread().getName()+"take" + take);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();

		// =================ת��================================
		System.out.println();
		System.out.println();
		System.out.println("=================ת��===========================");
		// �����е�Ԫ�ظ��Ƶ�������
		Object[] array = queue.toArray();
		Object[] array2 = queue.toArray(array);
		System.out.println(Arrays.toString(array));
		System.out.println(Arrays.toString(array2));
		// �������е�Ԫ���Ƴ���ָ���ļ�����
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		ArrayList<Integer> list = new ArrayList<Integer>();
		// ȫ���Ƴ����Ƴ���Ԫ�ػ���ӵ�������
		int drainTo = queue.drainTo(arrayList);
		System.out.println("drainTo(arrayList)ת������"+drainTo);
		System.out.println("ת����ļ���"+Arrays.toString((arrayList.toArray())));
		// �Ƴ�ָ���������Ƴ���Ԫ�ػ���ӵ�������
		int drainTo2 = queue.drainTo(list, 5);
		System.out.println("drainTo(list, 5)ת������"+drainTo2);
		System.out.println("ת����ļ���"+Arrays.toString(list.toArray()));
		// =========================���еĴ�С===================================
		System.out.println("���д�С");
		System.out.println();
		System.out.println();
		int size = queue.size();
		int remainingCapacity = queue.remainingCapacity();
		System.out.println("������Ԫ�صĸ���"+size);
		System.out.println("������ʣ������"+remainingCapacity);
		// ====================���Ԫ��========================
		System.out.println("���Ԫ��");
		System.out.println();
		System.out.println();
		// �Ƴ���Ԫ�أ������ظ�Ԫ��,�����null�ͻ��׳��쳣
		Integer remove = queue.remove();//poll
		System.out.println("remove()�Ƴ���Ԫ�أ�������"+remove);
		// �Ƴ�ָ��Ԫ��
		boolean remove2 = queue.remove(30);
		System.out.println("queue.remove(30)�Ƴ�ָ��Ԫ��"+remove2);
		// ���������ȫ��Ԫ��
		queue.clear();
		
	}

}
