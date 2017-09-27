package concurrenthashmap;

import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import domain.Domain;

public class ConcurrentHashMapTest {

	public static void main(String[] args) {
		ConcurrentHashMap<Domain,Integer> map = new ConcurrentHashMap<Domain,Integer>();
//		==========添加=================
		Integer put = map.put(getDomain(), 10);//添加键值对，键相同，值覆盖
//		===============移除================
		Integer remove = map.remove(getDomain());       //删除传入的key在集合的中的键值对
		boolean remove2 = map.remove(getDomain(), 3);//存在一对传入的键--值，将这样的键--值对从集合中删除
		map.clear();//清除所有键值对
//		===============大小======================
		int size = map.size();//集合的大小
//		=================键集，值集===========================
		Set<Entry<Domain,Integer>> entrySet = map.entrySet();//键---值对集
		KeySetView<Domain,Integer> keySet = map.keySet();//键集
		KeySetView<Domain,Integer> keySet2 = map.keySet(10);//指定值的  键集
		Enumeration<Domain> keys = map.keys();                       //键集的枚举
		KeySetView<Object,Boolean> newKeySet = map.newKeySet();
		Enumeration<Integer> elements = map.elements();    //值 集
//		===============包含==========================
		boolean contains = map.contains(5);                          //包含指定值   containsValue
		boolean containsKey = map.containsKey(getDomain());//包含指定键
		boolean containsValue = map.containsValue(10);        //包含指定值
//		================获取===============================
		Integer orDefault = map.getOrDefault(getDomain(), 3);   //获取指定键的值，键没有对应值，返回传入的默认值
		Integer integer = map.get(getDomain());       //获取指定键的值
		
		long mappingCount = map.mappingCount();
//		==================替换===========================
		Integer replace = map.replace(getDomain(), 5);        //将指定键的值替换成新值
		boolean replace2 = map.replace(getDomain(), 10, 20);//如果指定键的值是传入的值，将其替换成新值
		
	}
	static Random r = new Random(5000);

	static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
