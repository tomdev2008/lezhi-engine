package org.arabidopsis.ahocorasick;


/**
   Represents an EdgeList by using a single array.  Very fast lookup
   (just an array access), but expensive in terms of memory.
 */

class DenseEdgeList<E> implements EdgeList<E> {    
    private State<E>[] array;

    public DenseEdgeList() {
	this.array = new State[256];
	for (int i = 0; i < array.length; i++)
	    this.array[i] = null;
    }
    

    /**
       Helps in converting to dense representation.
     */
    public static <E> DenseEdgeList<E> fromSparse(SparseEdgeList<E> list) {
	byte[] keys = list.keys();
	DenseEdgeList<E> newInstance = new DenseEdgeList<E>();
	for (int i = 0; i < keys.length; i++) {
	    newInstance.put(keys[i], list.get(keys[i]));
	}
	return newInstance;
    }


    public State<E> get(byte b) {
	return this.array[(int) b & 0xFF];
    }


    public void put(byte b, State<E> s) {
	this.array[(int) b & 0xFF] = s;
    }


    public byte[] keys() {
	int length = 0;
	for(int i = 0; i < array.length; i++) {
	    if (array[i] != null)
		length++;
	}
	byte[] result = new byte[length];
	int j = 0;
	for(int i = 0; i < array.length; i++) {
	    if (array[i] != null) {
		result[j] = (byte) i;
		j++;
	    }
	}
	return result;
    }

}
