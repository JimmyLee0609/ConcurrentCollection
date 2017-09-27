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
//		==================���==========================
		System.out.println("===================���=========================");
//		add��offerûָ���ͺ����
		boolean add = deque.add(getDomain());															//addLast
		deque.addFirst(getDomain());																			//offerFirst
		deque.addLast(getDomain());																			//offerLast
		boolean offer = deque.offer(getDomain());														//offerLast
		boolean offerFirst = deque.offerFirst(getDomain());  										    //linkFirst
		boolean offerLast = deque.offerLast(getDomain());   										   //linkLast
//		pushûָ����ǰ���
		deque.push(getDomain());																						//addFirst
//		���û�ɹ����͵ȴ�����ʱ�������һ�Σ���û�ɹ��ͷ���false
		boolean offer2 = deque.offer(getDomain(), 1, TimeUnit.SECONDS); 					  //offerLast
		boolean offerFirst2 = deque.offerFirst(getDomain(), 1, TimeUnit.SECONDS); 		 //linkFirst
		boolean offerLast2 = deque.offerLast(getDomain(), 1, TimeUnit.SECONDS); 		 //linkLast
//		putûָ���ͺ���ӣ�put���û�ɹ����������̣߳��ȴ�
		deque.put(getDomain());																						//putLast
		for(int i=0;i<300;i++){
			new Thread(()->{
				try {
					deque.put(getDomain());
					System.out.println(Thread.currentThread().getName()+"���Ԫ��");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}).start();
		}
		deque.putFirst(getDomain());																					//linkFirst
		deque.putLast(getDomain());																					//linkLast
//		====================��ȡ=========================
		System.out.println();
		System.out.println();
		System.out.println("====================��ȡ==========================");
//		��ȡ�����Ƴ������ǲ鿴first lastԪ��
		Domain peek = deque.peek();  													//peekFirst
		Domain element = deque.element();											//getFirst
		Domain first = deque.getFirst();													//peekFirst
		Domain last = deque.getLast();													//peekLast
		Domain peekFirst = deque.peekFirst();										//first.item
		Domain peekLast = deque.peekLast();										//last.item
//		��ȡ���Ƴ�Ԫ�أ�������ʱ�䣬���Ȼ�ȡһ�飬��ȡ�������ȴ�����ʱ���ٻ�ȡһ�飬ʧ�ܣ�����false
		Domain poll = deque.poll();															//pollFirst
		Domain poll2 = deque.poll(1, TimeUnit.SECONDS);						//pollFirst
		Domain pollFirst = deque.pollFirst();												//unlinkFirst
		Domain pollFirst2 = deque.pollFirst(1, TimeUnit.SECONDS);			//unlinkFirst
		Domain pollLast = deque.pollLast();												//unlinkLast
		Domain pollLast2 = deque.pollLast(1, TimeUnit.SECONDS);				//unlinkLast
		Domain pop = deque.pop();																//removeFirst
//		��ȡ�����������̵߳ȴ�
		Domain take = deque.take();																//takeFirst
		Domain takeFirst = deque.takeFirst();												//unlinkFirst
		Domain takeLast = deque.takeLast();												//unlinkLast
//		=====================ת��==================================
		System.out.println();
		System.out.println();
		System.out.println("================ת��====================");
//		���ƶ����е�Ԫ�ز����浽�µ�������
		Object[] array = deque.toArray();
		Object[] array2 = deque.toArray(array);
		System.out.println("toArray���Ƶ�Ԫ��"+Arrays.toString(array2));
//		�������е�Ԫ�����ɾ������ת�Ƶ�ָ���ļ���
		ArrayList<Domain> c = new ArrayList<Domain>();
		ArrayList<Domain> c1 = new ArrayList<Domain>();
		int drainTo = deque.drainTo(c);								//drainTo(c1,Integer.MAX_VALUE)
		int drainTo2 = deque.drainTo(c1,5);							//unlinkFirst
		System.out.println("drainToת�Ƶ�Ԫ�ظ���"+drainTo);
		System.out.println("drainTo(c1,5)ת�Ƶ�Ԫ�ظ���"+drainTo2);
		
//		========================��С==============================
	System.out.println();
	System.out.println();
	System.out.println("=================��С=========================");
		int remainingCapacity = deque.remainingCapacity();
		int size = deque.size();
		System.out.println("ʣ������"+remainingCapacity);
		System.out.println("���д�С"+size);
//		==========================����===============================
		boolean contains = deque.contains(offerFirst2);
				
//		===================�Ƴ�========================
		Domain remove = deque.remove();																	//removeFirst
		boolean remove2 = deque.remove(pop);													//removeFirstOccurrence
		Domain removeFirst = deque.removeFirst();												//pollFirst ��ȡnullֵ���쳣
		Domain removeLast = deque.removeLast();													//pollLast ��ȡnullֵ���쳣
		boolean removeFirstOccurrence = deque.removeFirstOccurrence(element);//unlink
		boolean removeLastOccurrence = deque.removeLastOccurrence(last);				//unlink
		boolean removeAll = deque.removeAll(deque);												//������ɾ�������е�ȫ��Ԫ��
		Thread.sleep(600);
		
		deque.clear();//��ͷ��ʼ����
	}
	static Random r = new Random(5000);

	private static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
