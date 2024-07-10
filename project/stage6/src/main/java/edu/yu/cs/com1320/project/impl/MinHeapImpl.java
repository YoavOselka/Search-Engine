package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.NoSuchElementException;

public class MinHeapImpl <E extends Comparable<E>> extends MinHeap<E> {

    public MinHeapImpl() {
        elements = (E[]) new Comparable[11];
    }
    protected int getArrayIndex(E element){
        for(int i=1;i <= this.count ;i++){
            E obj = this.elements[i];
            if(obj.compareTo(element) == 0){
                return i;
            }
        }
        return -1;
    }

    protected void doubleArraySize(){
        int newSize = this.elements.length * 2;
        E[] newElements = (E[]) new Comparable[newSize];
        for(int i=1;i <this.elements.length; i++){
            newElements[i] = this.elements[i];
        }
        this.elements = newElements;
    }

    public void reHeapify(E element){
        int index = getArrayIndex(element);
        if(index == -1 || element == null){
            throw new NoSuchElementException("no such element");
        }
        upHeap(index);
        downHeap(index);
    }
}
