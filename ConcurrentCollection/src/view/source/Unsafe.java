package view.source;

import java.lang.reflect.Field;

public class Unsafe {

	public static view.source.Unsafe getUnsafe() {
		// TODO Auto-generated method stub
		return new Unsafe();
	}

	public long objectFieldOffset(Field declaredField) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean compareAndSwapInt(ViewLinkedTransferQueue viewLinkedTransferQueue, long sweepvotesoffset, int cmp,
			int val) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean compareAndSwapObject(Node node, long headoffset, Object cmp,
			Object val) {
		// TODO Auto-generated method stub
		return false;
	}

	public void putObject(Node node, long waiteroffset, Object object) {
		// TODO Auto-generated method stub
		
	}

	public boolean compareAndSwapObject(ViewLinkedTransferQueue viewLinkedTransferQueue, long tailoffset, Node cmp,
			Node val) {
		// TODO Auto-generated method stub
		return false;
	}

}
