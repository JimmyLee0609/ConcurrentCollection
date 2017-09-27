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
        if (haveData && (e == null))//检查传入是否是一对操作标记    存v=true   取null=false
            throw new NullPointerException();
        Node temp = null;                        // the node to append, if needed

        retry:
        for (;;) {   // restart on append race   死循环  链接尾不成功，就是传入的操作可以与队列元素匹配，就会再次进入循环

            for (Node he = head, part = he; part != null;) { // find & match first node    第一个操作传入时，没有节点 跳到 132 if (how != NOW) {              
                boolean isData = part.isData;                      //将头节点的信息缓存
                Object item = part.item;
//           节点存放的对象不是节点  检查节点存放的标记是否符合要求
                if (item != part && (item != null) == isData) { // 不符合  127    Node n = part.next;  下一个节点
//                   如果头节点  和 准备操作的标记是一致的没法匹配，  匹配是  存放--获取   一对 
                	if (isData == haveData)   // 节点和传入的操作不是一组的
                        break;   //到132      if (how != NOW) {    
//                	匹配是一对存取操作时
                    if (part.casItem(item, e)) { // match          CAS   交换对象   节点存的对象和传入的对象不一致时成功交换
//                    						只要下一个节点不是头节点
                        for (Node lookup = part; lookup != he;) {   //从头开始遍历
                            Node tempNext = lookup.next;  // 获取下一个节点的引用
                            if (head == he && casHead(he, tempNext == null ? lookup : tempNext)) {
//                                头节点没有变换	     头节点和这个节点交换     	下一个节点是null就用回自己 ，用于退出循环
                            	he.forgetNext();	//交换后节点断链，next=自己
                                break;//122   LockSupport.unpark(part.waiter); 
                            }                 // advance and retry
                            if ((he = head)   == null ||                //头节点是null      或者
                                (lookup = he.next) == null || !lookup.isMatched())   //头节点的下一个节点是空  ，下一个节点不符合链接要求
                                break;        // 122  LockSupport.unpark(part.waiter); 
                        }//遍历 for
                        LockSupport.unpark(part.waiter); //   线程等待
                        return ViewLinkedTransferQueue.<E>cast(item);  //因为 之前 交换了 ，返回原来的对象
                    }//交换cas
                }//节点存放的对象是节点，或者头节点存放的与标记不符合要求  null=false   v=true;
//               下一个节点
                Node n = part.next;
//                     下一个节点不是自己   
                part = (part != n) ? n : (he = head); // 如果下一个节点是自己，表示这个节点就是已经脱节了，就换回头节点
            }//  头节点 不为null   for

            if (how != NOW) {                 // 不是立即返回
                if (temp == null)            //
                    temp = new Node(e, haveData);    //利用传入的对象和标记，创建节点
                Node pred = tryAppend(temp, haveData);   //尝试将节点链接到尾部
                if (pred == null)             // 传入的操作入头节点是一对存取操作     如果链接不成功
                    continue retry;           // 新建的节点与尾节点可以操作 ， 重新进入循环匹配。
                if (how != ASYNC)       //链接成功       等待别的线程匹配，自己自旋，等待时间锁住自己的线程
                    return awaitMatch(temp, pred, e, (how == TIMED), nanos);
            }
            return e; // NOW立即返回 传入的对象
        }//for 死循环
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
            if (part == null && (part = head) == null) {  //第一个传入的操作或者刚好匹配完的操作       如果头节点和尾节点都是nul
                if (casHead(null, newNodeForAppend))    //将传入的节点作为头节点
                    return newNodeForAppend;                 // initialize
            }
            else if (part.cannotPrecede(haveData))   //队列中原来 有元素     仅发生在有头节点没有尾节点     传入操作与头节点是一对存取操作     返回null
                return null;                  // 返回null值     让它重新进入循环匹配
            else if ((n = part.next) != null)    // not last; keep traversing      传入操作与头节点不是一对存取操作  而是  存存  取取    （part）头节点后面还有元素
                part = part != prentTail && prentTail != (tempTail = tail) ? (prentTail = tempTail) : // stale tail    移动到队尾  如果part不是队尾就转到队尾     part=prentTail=tempTail=tail  
                    (part != n) ? n : null;      // restart if off list                  //part    如果part=park.next  part=null   ,  否则 part=part.next
            else if (!part.casNext(null, newNodeForAppend))//        part.next=null   是尾节点  part链接这个新的节点
                part = part.next;                   // re-read on CAS failure       链接不成功，part=part.next  返回null  重新进入xfer死循环
            else {     //part.next=null &&part.casNext 成功
                if (part != prentTail) {                 // update if slack now >= 2         链接成功了，值就与原来不一样了
                    while ((tail != prentTail || !casTail(prentTail, newNodeForAppend)) &&  //新节点变成尾节点
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
            if (item != e) {                  // matched  //根据传入对象和标记，新建的节点，保存的对象不等于传入的对象
                // assert item != s;
                newMake.forgetContents();           // avoid garbage     删除链接
                return ViewLinkedTransferQueue.<E>cast(item);
            }
            if ((w.isInterrupted() || (timed && nanos <= 0)) &&               
                    newMake.casItem(e, newMake)) {        // cancel   线程中断， Time标记但是时长<=0  节点将存放的对象转换为自己
                unsplice(pred, newMake);                        //解链
                return e;												//返回原来的对象
            }

            if (spins < 0) {                  // establish spins at/near front
                if ((spins = spinsFor(pred, newMake.isData)) > 0)           //根据状态   重新设值spins
                    randomYields = ThreadLocalRandom.current();         //保存  当前线程随机数引用     转到else {    LockSupport.park(this);
            }     //线程  被唤醒后   重新 走到 这里
            else if (spins > 0) {             // spin
                --spins;              //随机数判断是否放弃cpu资源，释放锁
                if (randomYields.nextInt(CHAINED_SPINS) == 0)
                    Thread.yield();           // occasionally yield
            }    //spin=0    如果节点没有保存线程索引，保存当前线程
            else if (newMake.waiter == null) {
                newMake.waiter = w;                 // request unpark then recheck
            }  //spin=0
            else if (timed) {  //自旋到0  等待时间   锁住线程。
                nanos = deadline - System.nanoTime();
                if (nanos > 0L)
                    LockSupport.parkNanos(this, nanos);
            }
            else {       
                LockSupport.park(this);               //线程阻塞
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
     * 给定的前身的给定的删除/取消的节点（现在或更晚）的取消。
     * Unsplices (now or later) the given deleted/cancelled node with
     * the given predecessor.
     *
     * @param pred a node that was at one time known to be the
     * predecessor of s, or null or s itself if s is/was at head
     * @param s the node to be unspliced
     */
    final void unsplice(Node pred, Node s) {     //解链
        s.forgetContents(); // forget unneeded fields              将节点设置为断链状态
        /*
         * 见上文的理由。 简要地说：如果pred仍然指向s，尝试取消链接。
如果s不能被取消链接，因为它的尾随节点或者pred可能被取消链接，并且pred和s都不是head或offlist，
所以添加到sweepVotes，如果有足够的投票积累了，则扫描。
         * See above for rationale. Briefly: if pred still points to
         * s, try to unlink s.  If s cannot be unlinked, because it is
         * trailing node or pred might be unlinked, and neither pred
         * nor s are head or offlist, add to sweepVotes, and if enough
         * votes have accumulated, sweep.
         */
        if (pred != null && pred != s && pred.next == s) {    //判断两个节点的关系是前后关系。
            Node n = s.next;          //保存 将要删除的链的下一个节点引用
            if (n == null ||                                                                //节点不为空
                (n != s && pred.casNext(s, n) && pred.isMatched())) {  //s.next!=s,   将s的前一个节点的next换成s.next
                for (;;) {               // check if at, or could be, head
                    Node h = head;
                    if (h == pred || h == s || h == null)     //如果头=s的前一个节点， s就是头节点， 头节点为null
                        return;          // at head or list empty                     直接返回
                    if (!h.isMatched())                         //如果头不符合要求
                        break;                                  //转到367    if (pred.next != pred && s.next != s) { // recheck if offlist
                    Node hn = h.next;
                    if (hn == null)                  // 头节点的下一个节点null  ，队列空了
                        return;          // now empty         直接返回
                    if (hn != h && casHead(h, hn))         //头节点的下一个！=头节点，   头节点与头节点的下一个交换，
                        h.forgetNext();  // advance head        //原来的头节点断链
                }
                if (pred.next != pred && s.next != s) { // recheck if offlist             重新检测是否断链
                    for (;;) {           // sweep now if enough votes      死循环
                        int v = sweepVotes;                     
                        if (v < SWEEP_THRESHOLD) {          //  sweepVotes  投票累积 ?-->32-->0
                            if (casSweepVotes(v, v + 1))
                                break;
                        }
                        else if (casSweepVotes(v, 0)) {
                            sweep();                                //扫描队列，删除已取消或者已经匹配的节点
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 在头部遍历中取消链接匹配（通常取消）节点。
     * Unlinks matched (typically cancelled) nodes encountered in a
     * traversal from head.
     */
    private void sweep() {
        for (Node p = head, s, n; p != null && (s = p.next) != null; ) {
            if (!s.isMatched())         //头节点的下一个节点是否没被匹配
                // Unmatched nodes are never self-linked
                p = s;      //没被匹配就下一个节点覆盖头节点
            else if ((n = s.next) == null) // trailing node is pinned           s的下一个节点是否为空（到尾了）
                break;
            else if (s == n)    // stale                  s和s.next是否相同    自己脱链了  
                // No need to also check for p == s, since that implies s == n
                p = head;
            else                 //   匹配了          头节点和下一个节点交换
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
