package view.source;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.locks.LockSupport;

public class ViewLinkedTransferQueue<E> extends AbstractQueue<E> implements TransferQueue<E>, Serializable {
	/** True if on multiprocessor */
    private static final boolean MP =
        Runtime.getRuntime().availableProcessors() > 1;

    /**
     * The number of times to spin (with randomly interspersed calls
     * to Thread.yield) on multiprocessor before blocking when a node
     * is apparently the first waiter in the queue.  See above for
     * explanation. Must be a power of two. The value is empirically
     * derived -- it works pretty well across a variety of processors,
     * numbers of CPUs, and OSes.
     */
    private static final int FRONT_SPINS   = 1 << 7;

    /**
     * The number of times to spin before blocking when a node is
     * preceded by another node that is apparently spinning.  Also
     * serves as an increment to FRONT_SPINS on phase changes, and as
     * base average frequency for yielding during spins. Must be a
     * power of two.
     */
    private static final int CHAINED_SPINS = FRONT_SPINS >>> 1;

    /**
     * The maximum number of estimated removal failures (sweepVotes)
     * to tolerate before sweeping through the queue unlinking
     * cancelled nodes that were not unlinked upon initial
     * removal. See above for explanation. The value must be at least
     * two to avoid useless sweeps when removing trailing nodes.
     */
    static final int SWEEP_THRESHOLD = 32;
    /** head of the queue; null until first enqueue */
    transient volatile Node head;

    /** tail of the queue; null until first append */
    private transient volatile Node tail;

    /** The number of apparent failures to unsplice removed nodes */
    private transient volatile int sweepVotes;

    // CAS methods for fields
    private boolean casTail(Node cmp, Node val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    private boolean casHead(Node cmp, Node val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    private boolean casSweepVotes(int cmp, int val) {
        return UNSAFE.compareAndSwapInt(this, sweepVotesOffset, cmp, val);
    }

    /*
     * Possible values for "how" argument in xfer method.
     */
    private static final int NOW   = 0; // for untimed poll, tryTransfer
    private static final int ASYNC = 1; // for offer, put, add
    private static final int SYNC  = 2; // for transfer, take
    private static final int TIMED = 3; // for timed poll, tryTransfer

    @SuppressWarnings("unchecked")
    static <E> E cast(Object item) {
        // assert item == null || item.getClass() != Node.class;
        return (E) item;
    }

    /**
     * Implements all queuing methods. See above for explanation.
     *
     * @param e the item or null for take
     * @param haveData true if this is a put, else a take
     * @param how NOW, ASYNC, SYNC, or TIMED
     * @param nanos timeout in nanosecs, used only if mode is TIMED
     * @return an item if matched, else e
     * @throws NullPointerException if haveData mode but e is null
     */
    private E xfer(E e, boolean haveData, int how, long nanos) {
        if (haveData && (e == null))//��鴫���Ƿ���һ�Բ������    ��v=true   ȡnull=false
            throw new NullPointerException();
        Node temp = null;                        // the node to append, if needed

        retry:
        for (;;) {   // restart on append race   ��ѭ��  ����β���ɹ������Ǵ���Ĳ������������Ԫ��ƥ�䣬�ͻ��ٴν���ѭ��

            for (Node he = head, part = he; part != null;) { // find & match first node    ��һ����������ʱ��û�нڵ� ���� 132 if (how != NOW) {              
                boolean isData = part.isData;                      //��ͷ�ڵ����Ϣ����
                Object item = part.item;
//           �ڵ��ŵĶ����ǽڵ�  ���ڵ��ŵı���Ƿ����Ҫ��
                if (item != part && (item != null) == isData) { // ������  127    Node n = part.next;  ��һ���ڵ�
//                   ���ͷ�ڵ�  �� ׼�������ı����һ�µ�û��ƥ�䣬  ƥ����  ���--��ȡ   һ�� 
                	if (isData == haveData)   // �ڵ�ʹ���Ĳ�������һ���
                        break;   //��132      if (how != NOW) {    
//                	ƥ����һ�Դ�ȡ����ʱ
                    if (part.casItem(item, e)) { // match          CAS   ��������   �ڵ��Ķ���ʹ���Ķ���һ��ʱ�ɹ�����
//                    						ֻҪ��һ���ڵ㲻��ͷ�ڵ�
                        for (Node lookup = part; lookup != he;) {   //��ͷ��ʼ����
                            Node tempNext = lookup.next;  // ��ȡ��һ���ڵ������
                            if (head == he && casHead(he, tempNext == null ? lookup : tempNext)) {
//                                ͷ�ڵ�û�б任	     ͷ�ڵ������ڵ㽻��     	��һ���ڵ���null���û��Լ� �������˳�ѭ��
                            	he.forgetNext();	//������ڵ������next=�Լ�
                                break;//122   LockSupport.unpark(part.waiter); 
                            }                 // advance and retry
                            if ((he = head)   == null ||                //ͷ�ڵ���null      ����
                                (lookup = he.next) == null || !lookup.isMatched())   //ͷ�ڵ����һ���ڵ��ǿ�  ����һ���ڵ㲻��������Ҫ��
                                break;        // 122  LockSupport.unpark(part.waiter); 
                        }//���� for
                        LockSupport.unpark(part.waiter); //   �̵߳ȴ�
                        return ViewLinkedTransferQueue.<E>cast(item);  //��Ϊ ֮ǰ ������ ������ԭ���Ķ���
                    }//����cas
                }//�ڵ��ŵĶ����ǽڵ㣬����ͷ�ڵ��ŵ����ǲ�����Ҫ��  null=false   v=true;
//               ��һ���ڵ�
                Node n = part.next;
//                     ��һ���ڵ㲻���Լ�   
                part = (part != n) ? n : (he = head); // �����һ���ڵ����Լ�����ʾ����ڵ�����Ѿ��ѽ��ˣ��ͻ���ͷ�ڵ�
            }//  ͷ�ڵ� ��Ϊnull   for

            if (how != NOW) {                 // ������������
                if (temp == null)            //
                    temp = new Node(e, haveData);    //���ô���Ķ���ͱ�ǣ������ڵ�
                Node pred = tryAppend(temp, haveData);   //���Խ��ڵ����ӵ�β��
                if (pred == null)             // ����Ĳ�����ͷ�ڵ���һ�Դ�ȡ����     ������Ӳ��ɹ�
                    continue retry;           // �½��Ľڵ���β�ڵ���Բ��� �� ���½���ѭ��ƥ�䡣
                if (how != ASYNC)       //���ӳɹ�       �ȴ�����߳�ƥ�䣬�Լ��������ȴ�ʱ����ס�Լ����߳�
                    return awaitMatch(temp, pred, e, (how == TIMED), nanos);
            }
            return e; // NOW�������� ����Ķ���
        }//for ��ѭ��
    }

    /**
     * Tries to append node s as tail.
     *
     * @param newNodeForAppend the node to append
     * @param haveData true if appending in data mode
     * @return null on failure due to losing race with append in
     * different mode, else s's predecessor, or s itself if no
     * predecessor
     */
    private Node tryAppend(Node newNodeForAppend, boolean haveData) {
        for (Node prentTail = tail, part = prentTail;;) {        // move p to last node and append
            Node n, tempTail;                        // temps for reads of next & tail
            if (part == null && (part = head) == null) {  //��һ������Ĳ������߸պ�ƥ����Ĳ���       ���ͷ�ڵ��β�ڵ㶼��nul
                if (casHead(null, newNodeForAppend))    //������Ľڵ���Ϊͷ�ڵ�
                    return newNodeForAppend;                 // initialize
            }
            else if (part.cannotPrecede(haveData))   //������ԭ�� ��Ԫ��     ����������ͷ�ڵ�û��β�ڵ�     ���������ͷ�ڵ���һ�Դ�ȡ����     ����null
                return null;                  // ����nullֵ     �������½���ѭ��ƥ��
            else if ((n = part.next) != null)    // not last; keep traversing      ���������ͷ�ڵ㲻��һ�Դ�ȡ����  ����  ���  ȡȡ    ��part��ͷ�ڵ���滹��Ԫ��
                part = part != prentTail && prentTail != (tempTail = tail) ? (prentTail = tempTail) : // stale tail    �ƶ�����β  ���part���Ƕ�β��ת����β     part=prentTail=tempTail=tail  
                    (part != n) ? n : null;      // restart if off list                  //part    ���part=park.next  part=null   ,  ���� part=part.next
            else if (!part.casNext(null, newNodeForAppend))//        part.next=null   ��β�ڵ�  part��������µĽڵ�
                part = part.next;                   // re-read on CAS failure       ���Ӳ��ɹ���part=part.next  ����null  ���½���xfer��ѭ��
            else {     //part.next=null &&part.casNext �ɹ�
                if (part != prentTail) {                 // update if slack now >= 2         ���ӳɹ��ˣ�ֵ����ԭ����һ����
                    while ((tail != prentTail || !casTail(prentTail, newNodeForAppend)) &&  //�½ڵ���β�ڵ�
                           (prentTail = tail)   != null &&
                           (newNodeForAppend = prentTail.next) != null && // advance and retry
                           (newNodeForAppend = newNodeForAppend.next) != null && newNodeForAppend != prentTail);
                }
                return part;
            }
        }
    }

    /**
     * Spins/yields/blocks until node s is matched or caller gives up.
     *
     * @param newMake the waiting node
     * @param pred the predecessor of s, or s itself if it has no
     * predecessor, or null if unknown (the null case does not occur
     * in any current calls but may in possible future extensions)
     * @param e the comparison value for checking match
     * @param timed if true, wait only until timeout elapses
     * @param nanos timeout in nanosecs, used only if timed is true
     * @return matched item, or e if unmatched on interrupt or timeout
     */
    private E awaitMatch(Node newMake, Node pred, E e, boolean timed, long nanos) {
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        Thread w = Thread.currentThread();
        int spins = -1; // initialized after first item and cancel checks
        ThreadLocalRandom randomYields = null; // bound if needed

        for (;;) {
            Object item = newMake.item;
            if (item != e) {                  // matched  //���ݴ������ͱ�ǣ��½��Ľڵ㣬����Ķ��󲻵��ڴ���Ķ���
                // assert item != s;
                newMake.forgetContents();           // avoid garbage     ɾ������
                return ViewLinkedTransferQueue.<E>cast(item);
            }
            if ((w.isInterrupted() || (timed && nanos <= 0)) &&               
                    newMake.casItem(e, newMake)) {        // cancel   �߳��жϣ� Time��ǵ���ʱ��<=0  �ڵ㽫��ŵĶ���ת��Ϊ�Լ�
                unsplice(pred, newMake);                        //����
                return e;												//����ԭ���Ķ���
            }

            if (spins < 0) {                  // establish spins at/near front
                if ((spins = spinsFor(pred, newMake.isData)) > 0)           //����״̬   ������ֵspins
                    randomYields = ThreadLocalRandom.current();         //����  ��ǰ�߳����������     ת��else {    LockSupport.park(this);
            }     //�߳�  �����Ѻ�   ���� �ߵ� ����
            else if (spins > 0) {             // spin
                --spins;              //������ж��Ƿ����cpu��Դ���ͷ���
                if (randomYields.nextInt(CHAINED_SPINS) == 0)
                    Thread.yield();           // occasionally yield
            }    //spin=0    ����ڵ�û�б����߳����������浱ǰ�߳�
            else if (newMake.waiter == null) {
                newMake.waiter = w;                 // request unpark then recheck
            }  //spin=0
            else if (timed) {  //������0  �ȴ�ʱ��   ��ס�̡߳�
                nanos = deadline - System.nanoTime();
                if (nanos > 0L)
                    LockSupport.parkNanos(this, nanos);
            }
            else {       
                LockSupport.park(this);               //�߳�����
            }
        }
    }

    /**
     * Returns spin/yield value for a node with given predecessor and
     * data mode. See above for explanation.
     */
    private static int spinsFor(Node pred, boolean haveData) {
        if (MP && pred != null) {
            if (pred.isData != haveData)      // phase change
                return FRONT_SPINS + CHAINED_SPINS;
            if (pred.isMatched())             // probably at front
                return FRONT_SPINS;
            if (pred.waiter == null)          // pred apparently spinning
                return CHAINED_SPINS;
        }
        return 0;
    }

    /* -------------- Traversal methods -------------- */

    /**
     * Returns the successor of p, or the head node if p.next has been
     * linked to self, which will only be true if traversing with a
     * stale pointer that is now off the list.
     */
    final Node succ(Node p) {
        Node next = p.next;
        return (p == next) ? head : next;
    }

    /**
     * Returns the first unmatched node of the given mode, or null if
     * none.  Used by methods isEmpty, hasWaitingConsumer.
     */
    private Node firstOfMode(boolean isData) {
        for (Node p = head; p != null; p = succ(p)) {
            if (!p.isMatched())
                return (p.isData == isData) ? p : null;
        }
        return null;
    }

    /**
     * Version of firstOfMode used by Spliterator. Callers must
     * recheck if the returned node's item field is null or
     * self-linked before using.
     */
    final Node firstDataNode() {
        for (Node p = head; p != null;) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p)
                    return p;
            }
            else if (item == null)
                break;
            if (p == (p = p.next))
                p = head;
        }
        return null;
    }

    /**
     * Returns the item in the first unmatched node with isData; or
     * null if none.  Used by peek.
     */
    private E firstDataItem() {
        for (Node p = head; p != null; p = succ(p)) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p)
                    return ViewLinkedTransferQueue.<E>cast(item);
            }
            else if (item == null)
                return null;
        }
        return null;
    }

    /**
     * Traverses and counts unmatched nodes of the given mode.
     * Used by methods size and getWaitingConsumerCount.
     */
    private int countOfMode(boolean data) {
        int count = 0;
        for (Node p = head; p != null; ) {
            if (!p.isMatched()) {
                if (p.isData != data)
                    return 0;
                if (++count == Integer.MAX_VALUE) // saturated
                    break;
            }
            Node n = p.next;
            if (n != p)
                p = n;
            else {
                count = 0;
                p = head;
            }
        }
        return count;
    }
    /**
     * ������ǰ��ĸ�����ɾ��/ȡ���Ľڵ㣨���ڻ������ȡ����
     * Unsplices (now or later) the given deleted/cancelled node with
     * the given predecessor.
     *
     * @param pred a node that was at one time known to be the
     * predecessor of s, or null or s itself if s is/was at head
     * @param s the node to be unspliced
     */
    final void unsplice(Node pred, Node s) {     //����
        s.forgetContents(); // forget unneeded fields              ���ڵ�����Ϊ����״̬
        /*
         * �����ĵ����ɡ� ��Ҫ��˵�����pred��Ȼָ��s������ȡ�����ӡ�
���s���ܱ�ȡ�����ӣ���Ϊ����β��ڵ����pred���ܱ�ȡ�����ӣ�����pred��s������head��offlist��
������ӵ�sweepVotes��������㹻��ͶƱ�����ˣ���ɨ�衣
         * See above for rationale. Briefly: if pred still points to
         * s, try to unlink s.  If s cannot be unlinked, because it is
         * trailing node or pred might be unlinked, and neither pred
         * nor s are head or offlist, add to sweepVotes, and if enough
         * votes have accumulated, sweep.
         */
        if (pred != null && pred != s && pred.next == s) {    //�ж������ڵ�Ĺ�ϵ��ǰ���ϵ��
            Node n = s.next;          //���� ��Ҫɾ����������һ���ڵ�����
            if (n == null ||                                                                //�ڵ㲻Ϊ��
                (n != s && pred.casNext(s, n) && pred.isMatched())) {  //s.next!=s,   ��s��ǰһ���ڵ��next����s.next
                for (;;) {               // check if at, or could be, head
                    Node h = head;
                    if (h == pred || h == s || h == null)     //���ͷ=s��ǰһ���ڵ㣬 s����ͷ�ڵ㣬 ͷ�ڵ�Ϊnull
                        return;          // at head or list empty                     ֱ�ӷ���
                    if (!h.isMatched())                         //���ͷ������Ҫ��
                        break;                                  //ת��367    if (pred.next != pred && s.next != s) { // recheck if offlist
                    Node hn = h.next;
                    if (hn == null)                  // ͷ�ڵ����һ���ڵ�null  �����п���
                        return;          // now empty         ֱ�ӷ���
                    if (hn != h && casHead(h, hn))         //ͷ�ڵ����һ����=ͷ�ڵ㣬   ͷ�ڵ���ͷ�ڵ����һ��������
                        h.forgetNext();  // advance head        //ԭ����ͷ�ڵ����
                }
                if (pred.next != pred && s.next != s) { // recheck if offlist             ���¼���Ƿ����
                    for (;;) {           // sweep now if enough votes      ��ѭ��
                        int v = sweepVotes;                     
                        if (v < SWEEP_THRESHOLD) {          //  sweepVotes  ͶƱ�ۻ� ?-->32-->0
                            if (casSweepVotes(v, v + 1))
                                break;
                        }
                        else if (casSweepVotes(v, 0)) {
                            sweep();                                //ɨ����У�ɾ����ȡ�������Ѿ�ƥ��Ľڵ�
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * ��ͷ��������ȡ������ƥ�䣨ͨ��ȡ�����ڵ㡣
     * Unlinks matched (typically cancelled) nodes encountered in a
     * traversal from head.
     */
    private void sweep() {
        for (Node p = head, s, n; p != null && (s = p.next) != null; ) {
            if (!s.isMatched())         //ͷ�ڵ����һ���ڵ��Ƿ�û��ƥ��
                // Unmatched nodes are never self-linked
                p = s;      //û��ƥ�����һ���ڵ㸲��ͷ�ڵ�
            else if ((n = s.next) == null) // trailing node is pinned           s����һ���ڵ��Ƿ�Ϊ�գ���β�ˣ�
                break;
            else if (s == n)    // stale                  s��s.next�Ƿ���ͬ    �Լ�������  
                // No need to also check for p == s, since that implies s == n
                p = head;
            else                 //   ƥ����          ͷ�ڵ����һ���ڵ㽻��
                p.casNext(s, n);
        }
    }

    /**
     * Main implementation of remove(Object)
     */
    private boolean findAndRemove(Object e) {
        if (e != null) {
            for (Node pred = null, p = head; p != null; ) {
                Object item = p.item;
                if (p.isData) {
                    if (item != null && item != p && e.equals(item) &&
                        p.tryMatchData()) {
                        unsplice(pred, p);
                        return true;
                    }
                }
                else if (item == null)
                    break;
                pred = p;
                if ((p = p.next) == pred) { // stale
                    pred = null;
                    p = head;
                }
            }
        }
        return false;
    }

    /**
     * Creates an initially empty {@code LinkedTransferQueue}.
     */
    public ViewLinkedTransferQueue() {
    }

    /**
     * Creates a {@code LinkedTransferQueue}
     * initially containing the elements of the given collection,
     * added in traversal order of the collection's iterator.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public ViewLinkedTransferQueue(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never block.
     *
     * @throws NullPointerException if the specified element is null
     */
    public void put(E e) {
        xfer(e, true, ASYNC, 0);
    }

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never block or
     * return {@code false}.
     *
     * @return {@code true} (as specified by
     *  {@link java.util.concurrent.BlockingQueue#offer(Object,long,TimeUnit)
     *  BlockingQueue.offer})
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e, long timeout, TimeUnit unit) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never return {@code false}.
     *
     * @return {@code true} (as specified by {@link Queue#offer})
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    /**
     * Inserts the specified element at the tail of this queue.
     * As the queue is unbounded, this method will never throw
     * {@link IllegalStateException} or return {@code false}.
     *
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(E e) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    /**
     * Transfers the element to a waiting consumer immediately, if possible.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * otherwise returning {@code false} without enqueuing the element.
     *
     * @throws NullPointerException if the specified element is null
     */
    public boolean tryTransfer(E e) {
        return xfer(e, true, NOW, 0) == null;
    }

    /**
     * Transfers the element to a consumer, waiting if necessary to do so.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * else inserts the specified element at the tail of this queue
     * and waits until the element is received by a consumer.
     *
     * @throws NullPointerException if the specified element is null
     */
    public void transfer(E e) throws InterruptedException {
        if (xfer(e, true, SYNC, 0) != null) {
            Thread.interrupted(); // failure possible only due to interrupt
            throw new InterruptedException();
        }
    }

    /**
     * Transfers the element to a consumer if it is possible to do so
     * before the timeout elapses.
     *
     * <p>More precisely, transfers the specified element immediately
     * if there exists a consumer already waiting to receive it (in
     * {@link #take} or timed {@link #poll(long,TimeUnit) poll}),
     * else inserts the specified element at the tail of this queue
     * and waits until the element is received by a consumer,
     * returning {@code false} if the specified wait time elapses
     * before the element can be transferred.
     *
     * @throws NullPointerException if the specified element is null
     */
    public boolean tryTransfer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (xfer(e, true, TIMED, unit.toNanos(timeout)) == null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    public E take() throws InterruptedException {
        E e = xfer(null, false, SYNC, 0);
        if (e != null)
            return e;
        Thread.interrupted();
        throw new InterruptedException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = xfer(null, false, TIMED, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    public E poll() {
        return xfer(null, false, NOW, 0);
    }

    /**
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }

    /**
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; n < maxElements && (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }


    public E peek() {
        return firstDataItem();
    }

    /**
     * Returns {@code true} if this queue contains no elements.
     *
     * @return {@code true} if this queue contains no elements
     */
    public boolean isEmpty() {
        for (Node p = head; p != null; p = succ(p)) {
            if (!p.isMatched())
                return !p.isData;
        }
        return true;
    }

    public boolean hasWaitingConsumer() {
        return firstOfMode(false) != null;
    }

    /**
     * Returns the number of elements in this queue.  If this queue
     * contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * <p>Beware that, unlike in most collections, this method is
     * <em>NOT</em> a constant-time operation. Because of the
     * asynchronous nature of these queues, determining the current
     * number of elements requires an O(n) traversal.
     *
     * @return the number of elements in this queue
     */
    public int size() {
        return countOfMode(true);
    }

    public int getWaitingConsumerCount() {
        return countOfMode(false);
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.
     * Returns {@code true} if this queue contained the specified element
     * (or equivalently, if this queue changed as a result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
        return findAndRemove(o);
    }

    /**
     * Returns {@code true} if this queue contains the specified element.
     * More formally, returns {@code true} if and only if this queue contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this queue
     * @return {@code true} if this queue contains the specified element
     */
    public boolean contains(Object o) {
        if (o == null) return false;
        for (Node p = head; p != null; p = succ(p)) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p && o.equals(item))
                    return true;
            }
            else if (item == null)
                break;
        }
        return false;
    }

    /**
     * Always returns {@code Integer.MAX_VALUE} because a
     * {@code LinkedTransferQueue} is not capacity constrained.
     *
     * @return {@code Integer.MAX_VALUE} (as specified by
     *         {@link java.util.concurrent.BlockingQueue#remainingCapacity()
     *         BlockingQueue.remainingCapacity})
     */
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }
    private static final Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long sweepVotesOffset;
    static {
        try {
            UNSAFE = Unsafe.getUnsafe();
            Class<?> k = LinkedTransferQueue.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("tail"));
            sweepVotesOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("sweepVotes"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
}
