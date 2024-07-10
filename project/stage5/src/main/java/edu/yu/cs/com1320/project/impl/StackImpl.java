package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
    private Node<T> head;
    private int size;
    private static class Node<T>{
        private T data;
        private Node<T> next;
        public Node(T data){
            this.data = data;
            this.next = null;
        }
    }
    public StackImpl() {
        this.head = null;
        this.size = 0;
    }

    public void push(T element){
        if(element == null){
            throw new IllegalArgumentException("null element");
        }
        Node<T> toAdd = new Node<>(element);
        toAdd.next = head;
        head = toAdd;
        size++;
    }

    public T pop(){
        if(size == 0){
            return null;
        }
        T data = head.data;
        head = head.next;
        size--;
        return data;
    }

    public T peek() {
        if (size ==0) {
            return null;
        }
        return head.data;
    }

    public int size() {
        if(head == null){
            return 0;
        }
        return size;
    }
}
