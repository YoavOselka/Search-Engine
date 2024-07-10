package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    class Entry<Key, Value>{
        Key key;
        Value value;
        Entry<Key,Value> next;
        Entry(Key k, Value v){
            if(k == null){
                throw new IllegalArgumentException();
            }
            key = k;
            value = v;
        }
    }
    private Entry<Key, Value>[] table;
    private static final int arraySize = 5;
    //hashFunction return a value between 0 and this.table.length-1
    //this is how we get the location of the key in the table
    private int hashFunction(Key key){
        return (key.hashCode() & 0x7fffffff) % this.table.length;
    }
    public HashTableImpl(){
        this.table = new Entry[arraySize];
    }

    public Value get(Key k){
        int index = hashFunction(k);
        Entry<Key, Value> tableIndex = table[index];
        while(tableIndex != null){
            if(tableIndex.key.equals(k)){
                return tableIndex.value;
            }
            tableIndex = tableIndex.next;
        }
        return null;
    }

    public Value put(Key k, Value v){
        int index = hashFunction(k);
        Entry<Key,Value> tableIndex = table[index];
        while(tableIndex != null){
            if(tableIndex.key.equals(k)){
                Value oldValue = tableIndex.value;
                tableIndex.value = v;
                return oldValue;
            }
            tableIndex = tableIndex.next;
        }
        Entry<Key, Value> newEntry = new Entry<>(k, v);
        newEntry.next = table[index];
        table[index] = newEntry;
        return null;
    }

    public boolean containsKey(Key key){
        int index = hashFunction(key);
        Entry<Key, Value> tableIndex = table[index];
        while(tableIndex != null){
            if(tableIndex.key.equals(key)){
                return true;
            }
            tableIndex = tableIndex.next;
        }
        return false;
    }

    public Set<Key> keySet(){
        Set<Key> keys = new HashSet<>();
        for(Entry<Key, Value> tableIndex : table){
            Entry<Key, Value> current = tableIndex;
            while(current != null){
                keys.add(current.key);
                current = current.next;
            }
        }
        return Collections.unmodifiableSet(keys);
    }

    public Collection<Value> values(){
        Collection<Value> values = new ArrayList<>();
        for(Entry<Key,Value> tableIndex : table){
            Entry<Key, Value> current = tableIndex;
            while(current != null){
                values.add(current.value);
                current = current.next;
            }
        }
        return Collections.unmodifiableCollection(values);
    }

    public int size(){
        int size = 0;
        for(Entry<Key,Value> tableIndex : table){
            Entry<Key, Value> current = tableIndex;
            while(current != null){
                size++;
                current = current.next;
            }
        }
        return size;
    }
}
