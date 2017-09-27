package copyonwritearraylist;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import domain.Domain;

public class CopyOnWriteArrayListTrst {

	public static void main(String[] args) {
		CopyOnWriteArrayList<Domain> list = new CopyOnWriteArrayList<Domain>();
		
//		==================添加===============================
		boolean add = list.add(getDomain());
		list.add(0, getDomain());               //指定索引添加
		
		list.set(0, getDomain());               //指定索引覆盖元素
//		====================获取=================================
		Domain domain = list.get(0);          //指定索引获取

//		=================包含索引=======================
		int indexOf = list.indexOf(domain);                 //指定元素的索引
		int indexOf2 = list.indexOf(getDomain(), 0);    //从指定索引开始向后查到第一个指定元素的索引
		int lastIndexOf = list.lastIndexOf(domain);     //从最后开始查，指定元素出现的索引
		int lastIndexOf2 = list.lastIndexOf(domain, 1);//从指定元素开始向前查，指定元素出现的索引
		boolean contains = list.contains(domain); 		//是否包含元素
		
		Domain remove = list.remove(0);                     //集合移除指定索引的元素，改变结构，产生新数组
		boolean remove2 = list.remove(getDomain()); //移除指定元素，改变结构，产生新数组
		
		int size = list.size();// 集合的大小
		
		Iterator<Domain> iterator = list.iterator();  //集合的迭代器，只反映瞬时的状态，集合结构改变后，迭代器不会改变
		Domain next = iterator.next();
		boolean hasNext = iterator.hasNext();
//		iterator.remove();   没实现，抛异常
	}

	static Random r = new Random(5000);

	static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
