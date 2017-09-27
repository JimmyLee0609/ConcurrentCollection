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
//		==========���=================
		Integer put = map.put(getDomain(), 10);//��Ӽ�ֵ�ԣ�����ͬ��ֵ����
//		===============�Ƴ�================
		Integer remove = map.remove(getDomain());       //ɾ�������key�ڼ��ϵ��еļ�ֵ��
		boolean remove2 = map.remove(getDomain(), 3);//����һ�Դ���ļ�--ֵ���������ļ�--ֵ�ԴӼ�����ɾ��
		map.clear();//������м�ֵ��
//		===============��С======================
		int size = map.size();//���ϵĴ�С
//		=================������ֵ��===========================
		Set<Entry<Domain,Integer>> entrySet = map.entrySet();//��---ֵ�Լ�
		KeySetView<Domain,Integer> keySet = map.keySet();//����
		KeySetView<Domain,Integer> keySet2 = map.keySet(10);//ָ��ֵ��  ����
		Enumeration<Domain> keys = map.keys();                       //������ö��
		KeySetView<Object,Boolean> newKeySet = map.newKeySet();
		Enumeration<Integer> elements = map.elements();    //ֵ ��
//		===============����==========================
		boolean contains = map.contains(5);                          //����ָ��ֵ   containsValue
		boolean containsKey = map.containsKey(getDomain());//����ָ����
		boolean containsValue = map.containsValue(10);        //����ָ��ֵ
//		================��ȡ===============================
		Integer orDefault = map.getOrDefault(getDomain(), 3);   //��ȡָ������ֵ����û�ж�Ӧֵ�����ش����Ĭ��ֵ
		Integer integer = map.get(getDomain());       //��ȡָ������ֵ
		
		long mappingCount = map.mappingCount();
//		==================�滻===========================
		Integer replace = map.replace(getDomain(), 5);        //��ָ������ֵ�滻����ֵ
		boolean replace2 = map.replace(getDomain(), 10, 20);//���ָ������ֵ�Ǵ����ֵ�������滻����ֵ
		
	}
	static Random r = new Random(5000);

	static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
