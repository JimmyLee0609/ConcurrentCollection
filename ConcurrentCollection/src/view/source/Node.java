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
     * 将节点链接到自身以避免垃圾留存。 仅在CASing头域之后调用，所以使用轻松的写。
     * Links node to itself to avoid garbage retention.  Called
     * only after CASing head field, so uses relaxed write.
     */
    final void forgetNext() {
        UNSAFE.putObject(this, nextOffset, this);
    }

    /**将item设为自身并等待被清除，以避免匹配或取消后的垃圾留存。
使用轻松的写入，因为订单已经在唯一的调用上下文中被限制：只有在提取项目的volatile / atomic mechanics之后，项目才被遗忘。
同样，清理服务员也可以跟随CAS或从公园返回（如果有停放，否则我们不在乎）。
     * Sets item to self and waiter to null, to avoid garbage
     * retention after matching or cancelling. Uses relaxed writes
     * because order is already constrained in the only calling
     * contexts: item is forgotten only after volatile/atomic
     * mechanics that extract items.  Similarly, clearing waiter
     * follows either CAS or return from park (if ever parked;
     * else we don't care).
     */
    final void forgetContents() {
        UNSAFE.putObject(this, itemOffset, this);        //设值item为自身
        UNSAFE.putObject(this, waiterOffset, null);    //设值保存的线程信息为null
    }

    /**
     * 如果此节点已匹配，则返回true，包括由于取消引起的人为匹配的情况。
     * Returns true if this node has been matched, including the
     * case of artificial matches due to cancellation.
     */
    final boolean isMatched() {
        Object x = item;
        //节点对象保存的是自己，或者不是 存 取 操作的一对标识
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
        boolean hasData = isData;   //当前节点的标记是否有对象，存，取
        Object x;
//              当前节点和操作 标记不一样      节点对象不是自己     节点是标准的  存或取一对标记
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
