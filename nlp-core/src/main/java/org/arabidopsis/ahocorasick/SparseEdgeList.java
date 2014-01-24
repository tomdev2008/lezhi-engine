package org.arabidopsis.ahocorasick;


/**
   Linked list implementation of the EdgeList should be less memory-intensive.
 */


class SparseEdgeList<E> implements EdgeList<E> {    
    private Cons<E> head;

    public SparseEdgeList() {
	    head = null;
    }


    public State<E> get(byte b) {
	Cons<E> c = head;
	while (c != null) {
	    if (c.b == b)
		return c.s;
	    c = c.next;
	}
	return null;
    }

    public void put(byte b, State<E> s) {
	this.head = new Cons<E>(b, s, head);
    }


    public byte[] keys() {
	int length = 0;
	Cons<E> c = head;
	while (c != null) {
	    length++;
	    c = c.next;
	}
	byte[] result = new byte[length];
	c = head;
	int j = 0;
	while (c != null) {
	    result[j] = c.b;
	    j++;
	    c = c.next;
	}
	return result;
    }


    static private class Cons<E> {
	byte b;
	State<E> s;
	Cons<E> next;

	public Cons(byte b, State<E> s, Cons<E> next) {
	    this.b = b;
	    this.s = s;
	    this.next = next;
	}
    }

}
