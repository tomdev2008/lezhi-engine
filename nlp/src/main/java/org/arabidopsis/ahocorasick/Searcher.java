package org.arabidopsis.ahocorasick;


import java.util.Iterator;
import java.util.NoSuchElementException;

/**
   Iterator returns a list of Search matches.
 */

class Searcher<E> implements Iterator<SearchResult<E>> {
    private SearchResult<E> currentResult;
    private AhoCorasick<E> tree;

    Searcher(AhoCorasick<E> tree, SearchResult<E> result) {
	this.tree = tree;
	this.currentResult = result;
    }


    public boolean hasNext() {
	return (this.currentResult != null);
    }


    public SearchResult<E> next() {
	if (!hasNext())
	    throw new NoSuchElementException();
	SearchResult<E> result = currentResult;
	currentResult = tree.continueSearch(currentResult);
	return result;
    }


    public void remove() {
	throw new UnsupportedOperationException();
    }
}
