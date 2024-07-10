package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Collection;

public class TrieImpl<Value> implements Trie<Value> {
    private static final int alphabetSize = 256; // extended ASCII
    private TrieImpl.Node<Value> root; // root of trie
    public static class Node<Value>
    {
        protected Set<Value> val = new HashSet<>();
        protected TrieImpl.Node[] links = new TrieImpl.Node[TrieImpl.alphabetSize];
    }
    public TrieImpl(){
        this.root = new Node<>();
    }

    @Override
    public void put(String key, Value val) {
        if(val == null){
            this.deleteAll(key);
        }
        else{
            this.root = put(this.root, key,val,0);
        }
    }
    private Node<Value> put(Node<Value> x, String key, Object val, int d){
        if(x == null){
            x = new Node<>();
        }
        if(d == key.length()){
            x.val.add((Value) val);
            return x;
        }
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    @Override
    public List<Value> getSorted(String key, Comparator<Value> comparator) {
        Set<Value> set= this.get(key);
        List<Value> list = new ArrayList<>(set);
        list.sort(comparator.reversed());
        return list;
    }

    @Override
    public Set<Value> get(String key) {
        if(key == null){
            return new HashSet<>();
        }
        Node<Value> x = get(this.root, key,0);
        Set<Value> xx = (x != null) ? x.val : new HashSet<>();

        return (x == null) ? new HashSet<>() : xx;
    }

    private Node<Value> get(Node<Value> x, String key, int d){
        if(x == null){
            return null;
        }
        if(d == key.length()){
            return x;
        }
        char c = key.charAt(d);
        if(x.links[c] != null){
            return this.get(x.links[c], key, d+1);
        }
        return null;
    }

    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        List<Value> toReturn = new ArrayList<>();

        Node<Value> x = get(this.root, prefix,0);
        if(x != null && x.val != null){
            //toReturn.add((Value) x.val);
            for (Value val : x.val) {
                toReturn.add(val);
            }
        }
        getAllWithPrefixSorted(x, prefix, toReturn, comparator);
        toReturn.sort(comparator.reversed());
        //Set<Value> toReturnSet = new HashSet<>(toReturn);
        List<Value> toReturn1 = new LinkedList<>(toReturn);

        return toReturn1;
    }
    private void getAllWithPrefixSorted(Node<Value> x, String prefix, List<Value> list, Comparator comparator){
        if(x == null){
            return;
        }
        /*if (x.val != null) {
            for (Value val : x.val) {
                System.out.println("Adding value: " + val);
                list.add(val);
            }
        }*/
        for(char c = 0;c<alphabetSize; c++) {
            String nextPrefix = prefix + c;
            Node<Value> next = x.links[c];
            if (next != null) {
                for(Value val : next.val){
                    list.add(val);
                }
                getAllWithPrefixSorted(next, nextPrefix, list, comparator);
            }
        }
    }

   /* @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        Set<Value> toReturn = new HashSet<>();

        Node<Value> x = get(root, prefix, 0);
        if(x != null){
            deleteAllWithPrefix(x,prefix, toReturn);
        }
        return toReturn;
    }

    private void deleteAllWithPrefix(Node<Value> x ,String prefix, Set<Value> set){
        if(x==null){
            return;
        }
        if (x.val != null) {
            set.add((Value)x.val);
        }
        for (char c = 0; c < alphabetSize; c++) {
            deleteAllWithPrefix(x.links[c],prefix + c, set);
        }
        if (!prefix.isEmpty() && x.val == null && hasNoChildren(x)) {
            x = null; // Remove the current node if it has no value and no children
        }
        if (x!=null){
            x.val = null;
        }
    } */
   @Override
   public Set<Value> deleteAllWithPrefix(String prefix) {
       Set<Value> deletedValues = new HashSet<>();
       deleteAllWithPrefix(this.root, prefix, 0, deletedValues);
       return deletedValues;
   }

    private void deleteAllWithPrefix(Node<Value> x, String prefix, int d, Set<Value> deletedValues) {
        if (x == null) {
            return;
        }
        if (d == prefix.length()) {
            collectAndDeleteAll(x, deletedValues);
        } else {
            char c = prefix.charAt(d);
            deleteAllWithPrefix(x.links[c], prefix, d + 1, deletedValues);
        }

        // Remove the node if it is empty after deletion
        if (d > 0 && x.val.isEmpty() && hasNoChildren(x)) {
            x = null;
        }
    }

    private void collectAndDeleteAll(Node<Value> x, Set<Value> deletedValues) {
        if (x == null) {
            return;
        }
        deletedValues.addAll(x.val); // Collect the values
        x.val.clear(); // Delete the values
        for (int c = 0; c < alphabetSize; c++) {
            collectAndDeleteAll(x.links[c], deletedValues); // Recursively collect and delete for all child nodes
            x.links[c] = null; // Remove the link to the child node
        }
    }
    private Node<Value> getParent(Node<Value> x, String key) {
        Node<Value> parent = x;
        for (int i = 0; i < key.length() - 1; i++) {
            char c = key.charAt(i);
            parent = parent.links[c];
            if (parent == null) {
                break;
            }
        }
        return parent;
    }

    private boolean hasNoChildren(Node<Value> x) {
        for (int i = 0; i < alphabetSize; i++) {
            if (x.links[i] != null) {
                return false;
            }
        }
        return true;
    }
    @Override
    public Set<Value> deleteAll(String key) {
        Set<Value> toReturn = get(key);
        Node<Value> x = get(this.root, key,0);
        if(x != null){
            x.val = null;
            removeEmptyNodes(this.root, key, 0);
        }
        List toReturnList = new ArrayList<>(toReturn);
        Collections.sort(toReturnList);
        Collections.reverse(toReturnList);
        Set<Value> newSet = new LinkedHashSet<>();
        for(Object obj : toReturnList){
            newSet.add((Value) obj);
        }
        return newSet;
    }

    @Override
    public Value delete(String key, Value val) {
        Node<Value> x = get(root, key , 0);
        if(x != null && x.val != null && x.val.contains(val)){
            x.val.remove(val);
            removeEmptyNodes(root, key, 0);
            return val;
        }
        return null;
    }
    private boolean removeEmptyNodes(Node<Value> x, String key, int d) {
        if (x == null) return false;
        if (d == key.length()) {
            x.val = null;
            return isNodeEmpty(x);
        }
        char c = key.charAt(d);
        boolean isEmpty = removeEmptyNodes(x.links[c], key, d + 1);
        if (isEmpty) {
            x.links[c] = null;
            return isNodeEmpty(x);
        }
        return false;
    }

    private boolean isNodeEmpty(Node<Value> x) {
        for (Node<Value> node : x.links) {
            if (node != null) {
                return false;
            }
        }
        return x.val == null || x.val.isEmpty();
    }
}

