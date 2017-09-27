package concurrentlinkeddeque;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

import domain.Domain;

public class ConcurrentLinkedDequeTest {

	public static void main(String[] args) {
		ConcurrentLinkedDeque<Domain> concurrentLinkedDeque = new ConcurrentLinkedDeque<Domain>();
		System.out.println("=================添加==========================");
		boolean add = concurrentLinkedDeque.add(getDomain()); // offerLast(e);
		concurrentLinkedDeque.push(getDomain()); // addFirst(e);
		concurrentLinkedDeque.addFirst(getDomain()); // linkFirst(e);
		concurrentLinkedDeque.addLast(getDomain()); // linkLast(e);
		boolean offer = concurrentLinkedDeque.offer(getDomain()); // offerLast(e);
		boolean offerFirst = concurrentLinkedDeque.offerFirst(getDomain()); // linkFirst(e);----return----true
		boolean offerLast = concurrentLinkedDeque.offerLast(getDomain()); // linkLast(e);----return-----true
		System.out.println(
				"offer--" + offer + "--offerFirst--" + offerFirst + "--offerLast--" + offerLast + "--add---" + add);
		for (int i = 0; i < 50; i++) {
			concurrentLinkedDeque.add(getDomain());
		}

		System.out.println();
		System.out.println();
		System.out.println("=============大小===============");
		int size = concurrentLinkedDeque.size(); // 遍历获取
		System.out.println("size" + size);

		System.out.println();
		System.out.println();
		System.out.println("=============包含===============");
		boolean contains = concurrentLinkedDeque.contains(getDomain()); // 遍历查找
		System.out.println("contains---" + contains);

		System.out.println();
		System.out.println();
		System.out.println("============获取=========================");
		// ===================获取但不移除=================================
		Domain peekFirst = concurrentLinkedDeque.peekFirst(); // first()
		Domain peekLast = concurrentLinkedDeque.peekLast(); // last()
		Domain peek = concurrentLinkedDeque.peek(); // peekFirst()
		Domain first = concurrentLinkedDeque.getFirst(); // screenNullResult(peekFirst());
		Domain last = concurrentLinkedDeque.getLast(); // screenNullResult(peekLast())
		Domain element = concurrentLinkedDeque.element(); // getFirst()
		System.out.println("peekFirst--" + peekFirst);
		System.out.println("peekLast--" + peekLast);
		System.out.println("peek--" + peek);
		System.out.println("getFirst--" + first);
		System.out.println("getLast--" + last);
		System.out.println("element--" + element);
		// ===========================获取并移除==================================
		Domain poll = concurrentLinkedDeque.poll(); // pollFirst();
		Domain pollFirst = concurrentLinkedDeque.pollFirst(); // first()----
																// unlink(p);
		Domain pollLast = concurrentLinkedDeque.pollLast(); // last() unlink(p);
		Domain pop = concurrentLinkedDeque.pop(); // removeFirst();
		System.out.println("poll--" + poll);
		System.out.println("pollFirst---" + pollFirst);
		System.out.println("pollLast---" + pollLast);
		System.out.println("pop---" + pop);

		System.out.println();
		System.out.println();
		System.out.println("===============移除=====================");
		Domain removeFirst = concurrentLinkedDeque.removeFirst(); // screenNullResult(pollFirst());
		Domain removeLast = concurrentLinkedDeque.removeLast(); // screenNullResult(pollLast())
		Domain remove = concurrentLinkedDeque.remove(); // removeFirst();
		boolean removeFirstOccurrence = concurrentLinkedDeque.removeFirstOccurrence(getDomain()); // first()---unlink(p);
		boolean removeLastOccurrence = concurrentLinkedDeque.removeLastOccurrence(getDomain()); // last()-----unlink(p);
		boolean remove2 = concurrentLinkedDeque.remove(getDomain()); // removeFirstOccurrence(o)
		System.out.println("removeFirst---" + removeFirst);
		System.out.println("removeLast---" + removeLast);
		System.out.println("remove---" + remove);
		System.out.println("removeFirstOccurrence---" + removeFirstOccurrence);
		System.out.println("removeLastOccurrence---" + removeLastOccurrence);
		System.out.println("remove(getDomain())---" + remove2);

		System.out.println();
		System.out.println();
		System.out.println("=============================转移==========================");
		Object[] array = concurrentLinkedDeque.toArray();
		concurrentLinkedDeque.toArray(array);

		String string = concurrentLinkedDeque.toString();
		System.out.println(string);
	}

	static Random r = new Random(5000);

	static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
