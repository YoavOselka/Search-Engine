package edu.yu.cs.com1320.project.impl;
import com.sun.jdi.Value;
import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;
import edu.yu.cs.com1320.project.stage6.impl.DocumentPersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BTreeImpl <Key extends Comparable<Key>, Value> implements BTree<Key , Value>{
    private static final int MAX = 4;
    private BTreeImpl.Node root;
    private BTreeImpl.Node leftMostExternalNode;
    private int height;
    private int n;
    private PersistenceManager<Key, Value> pm = null;
    private static final class Node {
        private int entryCount;
        private BTreeImpl.Entry[] entries = new BTreeImpl.Entry[BTreeImpl.MAX];
        private BTreeImpl.Node next;
        private BTreeImpl.Node previous;

        private Node(int k) {
            this.entryCount = k;
        }

        private void setNext(BTreeImpl.Node next) {
            this.next = next;
        }

        private BTreeImpl.Node getNext() {
            return this.next;
        }

        private void setPrevious(BTreeImpl.Node previous) {
            this.previous = previous;
        }

        private BTreeImpl.Node getPrevious() {
            return this.previous;
        }

        private BTreeImpl.Entry[] getEntries() {
            return Arrays.copyOf(this.entries, this.entryCount);
        }
    }
        private static class Entry
        {
            private Comparable key;
            private Object val;
            private BTreeImpl.Node child;

            public Entry(Comparable key, Object val, BTreeImpl.Node child) {
                this.key = key;
                this.val = val;
                this.child = child;
            }
            public Object getValue()
            {
                return this.val;
            }
            public Comparable getKey()
            {
                return this.key;
            }
        }

    public BTreeImpl()
    {
        this.root = new BTreeImpl.Node(0);
        this.leftMostExternalNode = this.root;
    }

    @Override
    public void moveToDisk(Key k) throws IOException {
        if (pm == null) {
            throw new IllegalStateException("PersistenceManager is not set");
        }

        // Get the current value associated with the key
        Value v = get(k);

        if (v != null) {
            try {
                // Serialize the value to disk
                pm.serialize(k, v);

                // Remove the value from memory by setting it to null in the BTree
                put(k, null);

                // If you're tracking the number of in-memory entries, decrement it here
                // this.inMemoryEntryCount--;

            } catch (IOException e) {
                // If serialization fails, we don't want to remove the entry from memory
                // So we catch the IOException and re-throw it
                throw new IOException("Failed to move entry to disk: " + k.toString(), e);
            }
        } else {
            // If the value is not in the tree, we can't move it to disk
            throw new IllegalArgumentException("No value associated with key: " + k.toString());
        }
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        this.pm = pm;
    }
    private static boolean less(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) < 0;
    }

    private static boolean isEqual(Comparable k1, Comparable k2)
    {
        return k1.compareTo(k2) == 0;
    }

    public Set<Key> getAllKeys() {
        Set<Key> keys = new HashSet<>();
        collectKeys(this.root, keys, this.height);
        return keys;
    }

    private void collectKeys(Node node, Set<Key> keys, int height) {
        if (height == 0) {
            // Leaf node
            for (int i = 0; i < node.entryCount; i++) {
                if (node.entries[i].val != null) {
                    keys.add((Key) node.entries[i].key);
                }
            }
        } else {
            // Internal node
            for (int i = 0; i < node.entryCount; i++) {
                if (node.entries[i].val != null) {
                    keys.add((Key) node.entries[i].key);
                }
                collectKeys(node.entries[i].child, keys, height - 1);
            }
        }
    }
    @Override
    public Value get(Key k) {
        if (k == null)
        {
            throw new IllegalArgumentException("argument to get() is null");
        }
        BTreeImpl.Entry entry = this.get(this.root, k, this.height);
        if(entry != null)
        {
            Value output =  (Value)entry.val;
            if(output==null)
            {
                try {

                    output=pm.deserialize(k);
                    this.put(k,output);
                    return output;

                }
                catch(IOException e)
                {
                    return null;
                }
            }
            return output;
        }
        else
        {
            return null;
        }

    }
    private BTreeImpl.Entry get(BTreeImpl.Node currentNode, Key key, int height)
    {
        BTreeImpl.Entry[] entries = currentNode.entries;
        if (height == 0)
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {
                if(isEqual(key, entries[j].key))
                {
                    return entries[j];
                }
            }
            return null;
        }

        else
        {
            for (int j = 0; j < currentNode.entryCount; j++)
            {

                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key))
                {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            return null;
        }
    }
    private BTreeImpl.Node split(BTreeImpl.Node currentNode, int height)
    {
        BTreeImpl.Node newNode = new BTreeImpl.Node(BTreeImpl.MAX / 2);

        currentNode.entryCount = BTreeImpl.MAX / 2;

        for (int j = 0; j < BTreeImpl.MAX / 2; j++)
        {
            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
        }
        for(int i = currentNode.entries.length-1; i!=currentNode.entries.length/2-1;i--)
        {
            currentNode.entries[i]=null;
        }
        if (height == 0)
        {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }
    @Override
    public Value put(Key key, Value val)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("argument key to put() is null");
        }
        BTreeImpl.Entry alreadyThere = this.get(this.root, key, this.height);
        if(alreadyThere != null)
        {
            Entry output = this.get(this.root,key, this.height);
            alreadyThere.val = val;
            return (Value) output.val;
        }

        BTreeImpl.Node newNode = this.put(this.root, key, val, this.height);
        this.n++;

        if (newNode == null)
        {
            try
            {
                pm.delete(key);
                return null;

            }
            catch (NullPointerException e)
            {

                return null;
            }
            catch (IOException e){
                return null;
            }

        }

        BTreeImpl.Node newRoot = new BTreeImpl.Node(2);
        newRoot.entries[0] = new BTreeImpl.Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new BTreeImpl.Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        this.height++;
        return null;
    }
    /**
     *
     * @param currentNode
     * @param key
     * @param val
     * @param height
     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
     */
    private BTreeImpl.Node put(BTreeImpl.Node currentNode, Key key, Value val, int height)
    {
        int j;
        BTreeImpl.Entry newEntry = new BTreeImpl.Entry(key, val, null);

        if (height == 0)
        {

            for (j = 0; j < currentNode.entryCount; j++)
            {
                if (less(key, currentNode.entries[j].key))
                {
                    break;
                }
            }
        }

        else
        {
            for (j = 0; j < currentNode.entryCount; j++)
            {
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key))
                {
                    BTreeImpl.Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null)
                    {
                        return null;
                    }
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        for (int i = currentNode.entryCount; i > j; i--)
        {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < BTreeImpl.MAX)
        {
            return null;
        }
        else
        {
            return this.split(currentNode, height);
        }
    }
}
