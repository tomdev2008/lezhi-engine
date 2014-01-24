package org.buzzinate.lezhi.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A PriorityQueue maintains a partial ordering of its elements such that the
 * least element can always be found in constant time. Put()'s and pop()'s
 * require log(size) time.
 * 
 * <p>
 * <b>NOTE</b>: This class pre-allocates a full array of length
 * <code>maxSize+1</code>, in {@link #initialize}.
 * 
 */
public class DoublePriorityQueue<E> {
	private int size;
	private int maxSize;
	private Entry<E>[] heap;
	
	public static void main(String[] args) {
		DoublePriorityQueue<Integer> pq = new DoublePriorityQueue<Integer>(4);
		double[] ds = new double[] {0.1, 0.2, 0.2, 0.2, 0.3, 0.3, 0.5, 0.5, 0.5};
		for (int i = 0; i < ds.length; i++) {
			pq.add(ds[i], i);
		}
		System.out.println(pq.values());
	}
	
	
	@SuppressWarnings("unchecked")
	public DoublePriorityQueue(int maxSize) {
		size = 0;
		
		int heapSize;
		if (0 == maxSize)
			// We allocate 1 extra to avoid if statement in top()
			heapSize = 2;
		else {
			if (maxSize == Integer.MAX_VALUE) {
				// Don't wrap heapSize to -1, in this case, which
				// causes a confusing NegativeArraySizeException.
				// Note that very likely this will simply then hit
				// an OOME, but at least that's more indicative to
				// caller that this values is too big. We don't +1
				// in this case, but it's very unlikely in practice
				// one will actually insert this many objects into
				// the PQ:
				heapSize = Integer.MAX_VALUE;
			} else {
				// NOTE: we add +1 because all access to heap is
				// 1-based not 0-based. heap[0] is unused.
				heapSize = maxSize + 1;
			}
		}
		heap = new Entry[heapSize];
		this.maxSize = maxSize;
	}
	
	private boolean lessThan(Entry<E> a, Entry<E> b) {
		return Double.compare(a.key, b.key) < 0;
	}

	/**
	 * Adds an Object to a PriorityQueue in log(size) time. It returns the
	 * object (if any) that was dropped off the heap because it was full. This
	 * can be the given parameter (in case it is smaller than the full heap's
	 * minimum, and couldn't be added), or another object that was previously
	 * the smallest value in the heap and now has been replaced by a larger one,
	 * or null if the queue wasn't yet full with maxSize elements.
	 */
	public final Entry<E> add(double key, E value) {
		Entry<E> e = new Entry<E>(key, value);
		if (size < maxSize) {
			size++;
			heap[size] = e;
			upHeap();
			return null;
		} else if (size > 0 && !lessThan(e, heap[1])) {
			Entry<E> ret = heap[1];
			heap[1] = e;
			updateTop();
			return ret;
		} else {
			return e;
		}
	}
	
	public List<Entry<E>> entries() {
		List<Entry<E>> es = new ArrayList<Entry<E>>();
		for (int i = 1; i <= size; i++) es.add(heap[i]);
		Collections.sort(es, new Comparator<Entry<E>>() {
			@Override
			public int compare(Entry<E> e1, Entry<E> e2) {
				return -Double.compare(e1.key, e2.key);
			}
		});
		
		return es;
	}
	
	public List<E> values() {
		List<Entry<E>> es = new ArrayList<Entry<E>>();
		for (int i = 1; i <= size; i++) es.add(heap[i]);
		Collections.sort(es, new Comparator<Entry<E>>() {
			@Override
			public int compare(Entry<E> e1, Entry<E> e2) {
				return -Double.compare(e1.key, e2.key);
			}
		});
		
		List<E> vs = new ArrayList<E>();
		for (int i = 0; i < es.size(); i++) vs.add(es.get(i).value);
		return vs;
	}
	
	public Entry<E> top() {
		if (size > 0) {
			return heap[1];
		} else return null;
	}

	/**
	 * Removes and returns the least element of the PriorityQueue in log(size)
	 * time.
	 */
	public final Entry<E> pop() {
		if (size > 0) {
			Entry<E> result = heap[1]; // save first value
			heap[1] = heap[size]; // move last to first
			heap[size] = null; // permit GC of objects
			size--;
			downHeap(); // adjust heap
			return result;
		} else
			return null;
	}

	/**
	 * Should be called when the Object at top changes values. Still log(n)
	 * worst case, but it's at least twice as fast to
	 * 
	 * <pre>
	 * pq.top().change();
	 * pq.updateTop();
	 * </pre>
	 * 
	 * instead of
	 * 
	 * <pre>
	 * o = pq.pop();
	 * o.change();
	 * pq.push(o);
	 * </pre>
	 * 
	 * @return the new 'top' element.
	 */
	public final Entry<E> updateTop() {
		downHeap();
		return heap[1];
	}

	/** Returns the number of elements currently stored in the PriorityQueue. */
	public final int size() {
		return size;
	}

	/** Removes all entries from the PriorityQueue. */
	public final void clear() {
		for (int i = 0; i <= size; i++) {
			heap[i] = null;
		}
		size = 0;
	}

	private final void upHeap() {
		int i = size;
		Entry<E> node = heap[i]; // save bottom node
		int j = i >>> 1;
		while (j > 0 && lessThan(node, heap[j])) {
			heap[i] = heap[j]; // shift parents down
			i = j;
			j = j >>> 1;
		}
		heap[i] = node; // install saved node
	}

	private final void downHeap() {
		int i = 1;
		Entry<E> node = heap[i]; // save top node
		int j = i << 1; // find smaller child
		int k = j + 1;
		if (k <= size && lessThan(heap[k], heap[j])) {
			j = k;
		}
		while (j <= size && lessThan(heap[j], node)) {
			heap[i] = heap[j]; // shift up child
			i = j;
			j = i << 1;
			k = j + 1;
			if (k <= size && lessThan(heap[k], heap[j])) {
				j = k;
			}
		}
		heap[i] = node; // install saved node
	}
	
	public static class Entry<E> {
		public double key;
		public E value;
		
		public Entry(double k, E v) {
			this.key = k;
			this.value = v;
		}
	}
}