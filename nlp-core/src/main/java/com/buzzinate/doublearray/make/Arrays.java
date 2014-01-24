package com.buzzinate.doublearray.make;

public class Arrays {
	private static final int INSERTIONSORT_THRESHOLD = 7;
	
	/**
	 * 二分法查找.摘抄了jdk的东西..只不过把他的自动装箱功能给去掉了
	 * 
	 * @param branches
	 * @param c
	 * @return
	 */
	public static int binarySearch(Branch[] branches, char c) {
		int high = branches.length - 1;
		if (branches.length < 1) {
			return high;
		}
		int low = 0;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			int cmp = branches[mid].compareTo(c);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid; // key found
		}
		return -1; // key not found.
	}
	
	public static void sort(Branch[] a) {
		Branch[] aux = (Branch[])a.clone();
        mergeSort(aux, a, 0, a.length, 0);
    }

	public static void sort(Branch[] a, int fromIndex, int toIndex) {
		rangeCheck(a.length, fromIndex, toIndex);
		Branch[] aux = java.util.Arrays.copyOfRange(a, fromIndex, toIndex);
		mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
	}

	private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex
					+ ") > toIndex(" + toIndex + ")");
		if (fromIndex < 0)
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		if (toIndex > arrayLen)
			throw new ArrayIndexOutOfBoundsException(toIndex);
	}

	private static void mergeSort(Branch[] src, Branch[] dest, int low,
			int high, int off) {
		int length = high - low;

		// Insertion sort on smallest arrays
		if (length < INSERTIONSORT_THRESHOLD) {
			for (int i = low; i < high; i++)
				for (int j = i; j > low && (dest[j - 1]).compareTo(dest[j].getC()) > 0; j--)
					swap(dest, j, j - 1);
			return;
		}

		// Recursively sort halves of dest into src
		int destLow = low;
		int destHigh = high;
		low += off;
		high += off;
		int mid = (low + high) >>> 1;
		mergeSort(dest, src, low, mid, -off);
		mergeSort(dest, src, mid, high, -off);

		// If list is already sorted, just copy from src to dest. This is an
		// optimization that results in faster sorts for nearly ordered lists.
		if (src[mid - 1].compareTo(src[mid].getC()) <= 0) {
			System.arraycopy(src, low, dest, destLow, length);
			return;
		}

		// Merge sorted halves (now in src) into dest
		for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
			if (q >= high || p < mid &&  src[p].compareTo(src[q].getC()) <= 0)
				dest[i] = src[p++];
			else
				dest[i] = src[q++];
		}
	}

	/**
	 * Swaps x[a] with x[b].
	 */
	private static void swap(Branch[] x, int a, int b) {
		Branch t = x[a];
		x[a] = x[b];
		x[b] = t;
	}
}