package copyonwritearraylist;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import domain.Domain;

public class CopyOnWriteArrayListTrst {

	public static void main(String[] args) {
		CopyOnWriteArrayList<Domain> list = new CopyOnWriteArrayList<Domain>();
		
//		==================���===============================
		boolean add = list.add(getDomain());
		list.add(0, getDomain());               //ָ���������
		
		list.set(0, getDomain());               //ָ����������Ԫ��
//		====================��ȡ=================================
		Domain domain = list.get(0);          //ָ��������ȡ

//		=================��������=======================
		int indexOf = list.indexOf(domain);                 //ָ��Ԫ�ص�����
		int indexOf2 = list.indexOf(getDomain(), 0);    //��ָ��������ʼ���鵽��һ��ָ��Ԫ�ص�����
		int lastIndexOf = list.lastIndexOf(domain);     //�����ʼ�飬ָ��Ԫ�س��ֵ�����
		int lastIndexOf2 = list.lastIndexOf(domain, 1);//��ָ��Ԫ�ؿ�ʼ��ǰ�飬ָ��Ԫ�س��ֵ�����
		boolean contains = list.contains(domain); 		//�Ƿ����Ԫ��
		
		Domain remove = list.remove(0);                     //�����Ƴ�ָ��������Ԫ�أ��ı�ṹ������������
		boolean remove2 = list.remove(getDomain()); //�Ƴ�ָ��Ԫ�أ��ı�ṹ������������
		
		int size = list.size();// ���ϵĴ�С
		
		Iterator<Domain> iterator = list.iterator();  //���ϵĵ�������ֻ��ӳ˲ʱ��״̬�����Ͻṹ�ı�󣬵���������ı�
		Domain next = iterator.next();
		boolean hasNext = iterator.hasNext();
//		iterator.remove();   ûʵ�֣����쳣
	}

	static Random r = new Random(5000);

	static Domain getDomain() {
		Domain domain = new Domain(UUID.randomUUID().toString(), "DOMAIN", r.nextInt());
		return domain;
	}
}
