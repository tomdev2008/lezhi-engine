package org.arabidopsis.ahocorasick;

/**
   Simple interface for mapping bytes to States.
 */
interface EdgeList<E> {
    State<E> get(byte ch);
    void put(byte ch, State<E> state);
    byte[] keys();
}
