package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.HashTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
//Implement array doubling on the array used in your HashTableImpl to support unlimited entries. Don’t forget to re-hash all your entries after doubling the array!
public class HashTableImpl<Key, Value> implements HashTable<Key, Value>{
    private final int initialCapacity = 5;
    private final double loadFactor = 0.5;

    private Entry<Key, Value>[] table;
    class Entry<Key, Value> {
        Key key;
        Value value;
        Entry<Key, Value> next;
        Entry(Key k, Value v){
            if(k == null){
                throw new IllegalArgumentException("null key");
            }
            key = k;
            value =v;
        }
    }
    private int hashFunction(Key key){
        return (key.hashCode() & 0x7fffffff) % this.table.length;
    }
    public HashTableImpl(){
        this.table = new Entry[initialCapacity];
    }

    public Value get(Key k){
        int index = hashFunction(k);
        Entry<Key , Value> tableIndex = table[index];
        while(tableIndex != null){
            if(tableIndex.key.equals(k)){
                return tableIndex.value;
            }
            tableIndex = tableIndex.next;
        }
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

    public Collection<Value> values() {
        Collection<Value> values = new ArrayList<>();
        for (int i = 0; i < this.table.length; i++) {
            Entry<Key, Value> entry = table[i];
            while (entry != null) {
                values.add(entry.value);
                entry = entry.next;
            }
        }
        return Collections.unmodifiableCollection(values);
    }

    public int size() {
        int size = 0;
        for (Entry<Key, Value> entry : table) {
            while (entry != null) {
                size++;
                entry = entry.next;
            }
        }
        return size;
    }

    private void resizeIfNecessary(){
        if((double) size() / table.length > loadFactor){
            int newSize = this.table.length * 2;
            Entry<Key,Value>[] newTable = new Entry[newSize];
            for(Entry<Key,Value> tableIndex : this.table){
                while(tableIndex != null){
                    Entry<Key, Value> next = tableIndex.next;
                    int newIndex = (tableIndex.key.hashCode() & 0x7fffffff) % newSize;
                    tableIndex.next = newTable[newIndex];
                    newTable[newIndex] = tableIndex;
                    tableIndex = next;
                }
            }
            this.table = newTable;
        }
    }

    public Value put(Key k, Value v){
        if(k == null){
            throw new IllegalArgumentException("Null key");
        }
        resizeIfNecessary();
        int index = hashFunction(k);
        Entry<Key, Value> tableIndex = table[index];
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
}

