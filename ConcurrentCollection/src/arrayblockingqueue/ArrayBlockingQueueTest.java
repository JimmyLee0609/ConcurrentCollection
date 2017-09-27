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
		// �½�10�������н����
		System.out.println("==================���Ԫ��====================");
		// ==================���Ԫ��===================
		// ֱ����ӣ���ӳɹ�����true,���ɹ�����false
		boolean add = queue.add(getDomain());
		boolean offer = queue.offer(getDomain());
		System.out.println("first-" + add + " ----second-" + offer);

		for (int i = 0; i < 50; i++) {
			new Thread(() -> {
				// ���Ԫ�ص����У��������˾ͻ���ӵ��ȴ����У�������ǰ�̡߳�
				try {
					queue.put(getDomain());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("��" +Thread.currentThread().getName() + "��put");
			}
			).start();
		}
		// ���Ԫ�ص����У�����������˾ͣ��ȴ������ʱ������ӣ��ɹ�����true,ʧ�ܷ���false������������ǰ�̣߳�
		boolean offer2 = queue.offer(getDomain(), 3, TimeUnit.SECONDS);
		System.out.println("offer�ڶ����Ƿ�ɹ�" + offer2);
		int remainingCapacity = queue.remainingCapacity();
		int size = queue.size();
		System.out.println("��ӡ��ǰ���е�����" + remainingCapacity);
		System.out.println("��ӡ��ǰ���еĴ�С" + size);
		System.out.println();
		System.out.println();
		System.out.println("======================��ȡԪ��=================");
		// ===================��ȡԪ��=====================================
		// ��ȡ����ɾ����Ԫ�أ���Ԫ����takeIndex��ά��
		Domain element = queue.element();// peek������Ԫ��Ϊ�����쳣
		Domain peek = queue.peek();
		System.out.println("element" + element + "------------peek" + peek);
		// ��ȡ��ɾ����Ԫ��,���������Ԫ�ؾͷ���Ԫ�أ�û�оͷ��ؿ�
		Domain poll = queue.poll();
		System.out.println("poll" + poll);
		// ��ȡ��ɾ����Ԫ��,���������Ԫ�ؾͷ���Ԫ�أ�û�о͵ȴ�����ʱ���ٻ�ȡ��û�оͷ��ؿ�
		Domain poll2 = queue.poll(1, TimeUnit.SECONDS);
		System.out.println("poll(1, TimeUnit.SECONDS)" + poll2);
		// ��ȡ��Ԫ�أ�����û�о͵ȴ���ֱ����Ԫ�ط��أ������������̡߳�
		Domain take = queue.take();
		System.out.println("take()" + take);

		// ת�����淽ʽ
		System.out.println();
		System.out.println();
		System.out.println("================ת��==================");

		Object[] array = queue.toArray();
		Object[] array2 = queue.toArray(array);
		System.out.println(Arrays.toString(array));
		System.out.println(Arrays.toString(array2));
		ArrayList<Domain> list = new ArrayList<Domain>();
		ArrayList<Domain> list2 = new ArrayList<Domain>();
//		ɾ�������е�Ԫ�ز����浽����������У�
		int drainTo = queue.drainTo(list);
		Thread.sleep(500);
		int drainTo2 = queue.drainTo(list2, 5);
		System.out.println(drainTo + "ת����ȫ��Ԫ��" + list);
		System.out.println(drainTo2 + "ת����5��Ԫ��" + list2);

		System.out.println();
		System.out.println();
		System.out.println("================�����Ƿ����Ԫ��==================");
		// =====================�����Ƿ����Ԫ��=================
		boolean contains = queue.contains(poll2);
		System.out.println(Arrays.toString(queue.toArray()));
		System.out.println("Ԫ��" + poll2 + "\r\n�Ƿ��ڶ�����" + contains);

		// ================���Ԫ��==================
		System.out.println();
		System.out.println();
		System.out.println("==============ɾ��Ԫ��=================");
		Domain remove = queue.remove();// poll()
		boolean remove2 = queue.remove(remove);// ʹ�õ�����ɾ��
		System.out.println("remove" + remove);
		System.out.println("remove(remove)�Ƿ�ɹ�" + remove2);
		queue.clear();
		System.out.println("clear��");
		System.out.println(Arrays.toString(queue.toArray()));
	}

	static Random r = new Random(5000);

	private static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
