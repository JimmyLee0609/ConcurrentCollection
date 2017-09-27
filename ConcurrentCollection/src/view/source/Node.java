package view.source;

import java.util.concurrent.locks.LockSupport;
/**
 * Queue nodes. Uses Object, not E, for items to allow forgetting
 * them after use.  Relies heavily on Unsafe mechanics to minimize
 * unnecessary ordering constraints: Writes that are intrinsically
 * ordered wrt other accesses or CASes use simple relaxed forms.
 */
public class Node {
	final boolean isData;   // false if this is a request node
    volatile Object item;   // initially non-null if isData; CASed to match
    volatile Node next;
    volatile Thread waiter; // null until waiting

    // CAS methods for fields
    final boolean casNext(Node cmp, Node val) {
        return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
    }

    final boolean casItem(Object cmp, Object val) {
        // assert cmp == null || cmp.getClass() != Node.class;
        return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
    }

    /**
     * Constructs a new node.  Uses relaxed write because item can
     * only be seen after publication via casNext.
     */
    Node(Object item, boolean isData) {
        UNSAFE.putObject(this, itemOffset, item); // relaxed write
        this.isData = isData;
    }

    /**
     * ���ڵ����ӵ������Ա����������档 ����CASingͷ��֮����ã�����ʹ�����ɵ�д��
     * Links node to itself to avoid garbage retention.  Called
     * only after CASing head field, so uses relaxed write.
     */
    final void forgetNext() {
        UNSAFE.putObject(this, nextOffset, this);
    }

    /**��item��Ϊ�����ȴ���������Ա���ƥ���ȡ������������档
ʹ�����ɵ�д�룬��Ϊ�����Ѿ���Ψһ�ĵ����������б����ƣ�ֻ������ȡ��Ŀ��volatile / atomic mechanics֮����Ŀ�ű�������
ͬ�����������ԱҲ���Ը���CAS��ӹ�԰���أ������ͣ�ţ��������ǲ��ں�����
     * Sets item to self and waiter to null, to avoid garbage
     * retention after matching or cancelling. Uses relaxed writes
     * because order is already constrained in the only calling
     * contexts: item is forgotten only after volatile/atomic
     * mechanics that extract items.  Similarly, clearing waiter
     * follows either CAS or return from park (if ever parked;
     * else we don't care).
     */
    final void forgetContents() {
        UNSAFE.putObject(this, itemOffset, this);        //��ֵitemΪ����
        UNSAFE.putObject(this, waiterOffset, null);    //��ֵ������߳���ϢΪnull
    }

    /**
     * ����˽ڵ���ƥ�䣬�򷵻�true����������ȡ���������Ϊƥ��������
     * Returns true if this node has been matched, including the
     * case of artificial matches due to cancellation.
     */
    final boolean isMatched() {
        Object x = item;
        //�ڵ���󱣴�����Լ������߲��� �� ȡ ������һ�Ա�ʶ
        return (x == this) || ((x == null) == isData);
    }

    /**
     * Returns true if this is an unmatched request node.
     */
    final boolean isUnmatchedRequest() {
        return !isData && item == null;
    }

    /**
     * Returns true if a node with the given mode cannot be
     * appended to this node because this node is unmatched and
     * has opposite data mode.
     */
    final boolean cannotPrecede(boolean haveData) {
        boolean hasData = isData;   //��ǰ�ڵ�ı���Ƿ��ж��󣬴棬ȡ
        Object x;
//              ��ǰ�ڵ�Ͳ��� ��ǲ�һ��      �ڵ�������Լ�     �ڵ��Ǳ�׼��  ���ȡһ�Ա��
        return hasData != haveData && (x = item) != this && (x != null) == hasData;
    }

    /**
     * Tries to artificially match a data node -- used by remove.
     */
    final boolean tryMatchData() {
        // assert isData;
        Object x = item;
        if (x != null && x != this && casItem(x, null)) {
            LockSupport.unpark(waiter);
            return true;
        }
        return false;
    }

    private static final long serialVersionUID = -3375979862319811754L;

    // Unsafe mechanics
    private static final Unsafe UNSAFE;
    private static final long itemOffset;
    private static final long nextOffset;
    private static final long waiterOffset;
    static {
        try {
            UNSAFE = Unsafe.getUnsafe();
            Class<?> k = Node.class;
            itemOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("item"));
            nextOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("next"));
            waiterOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("waiter"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
