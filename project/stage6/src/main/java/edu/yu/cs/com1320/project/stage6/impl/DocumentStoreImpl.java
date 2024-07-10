package edu.yu.cs.com1320.project.stage6.impl;


import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

/*
    we want to add every document to duplicates, not add a reference to the document, a new one, duplicated
    when a document is deleted, we don't delete it from the duplicates,
    when we search, we search in the duplicated trie/BTree
    and then check if the documents that hit are not in memory (val == null)
    then we get() the documents to being them back to memory from disk
 */

public class DocumentStoreImpl implements DocumentStore {
    private TrieImpl<Document> trie;
    private TrieImpl<Document> duplicateTrie;
    private BTreeImpl<URI, Document> btree;
    private BTreeImpl<URI, Document> duplicateBTree;
    private StackImpl<Undoable> commandStack;
    private int maxDocumentCount = Integer.MAX_VALUE;
    private int maxDocumentBytes = Integer.MAX_VALUE;
    private int currentDocumentCount = 0;
    private int currentDocumentBytes = 0;
    private MinHeapImpl<URI> documentHeap;
    private HashMap<String, Integer> wordCountMap;


    //text.replaceAll("[^a-zA-Z0-9\\s]", "")
    public DocumentStoreImpl() {
        this(null);
    }

    public DocumentStoreImpl(File baseDir){
        this.btree = new BTreeImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.duplicateTrie = new TrieImpl<>();
        this.duplicateBTree = new BTreeImpl<>();
        this.wordCountMap = new HashMap<>();
        this.documentHeap = new MinHeapImpl<>();
        this.btree.setPersistenceManager(new DocumentPersistenceManager(baseDir));
    }

    public StackImpl<Undoable> getCommandStack() {
        return commandStack;
    }
    public TrieImpl<Document> getTrie(){
        return trie;
    }
    public int getCurrentDocumentBytes(){
        return this.currentDocumentBytes;
    }
    public MinHeapImpl<URI> getDocumentHeap(){
        return this.documentHeap;
    }
    public int getCurrentDocumentCount(){
        return this.currentDocumentCount;
    }

    @Override
    public void setMaxDocumentCount(int limit) throws IOException {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be bigger than 0");
        }
        this.maxDocumentCount = limit;
        limitations();
    }

    @Override
    public void setMaxDocumentBytes(int limit) throws IOException {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be bigger than 0");
        }
        this.maxDocumentBytes = limit;
        limitations();
    }

    private void limitations() throws IOException{
        while (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            URI leastUsedURI = documentHeap.remove();
            Document doc = btree.get(leastUsedURI);
            if (doc != null) {
                btree.moveToDisk(leastUsedURI);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(doc);
            }
        }
    }

    private void removeDocument(URI uri) {
        Document doc = btree.get(uri);
        if (doc != null) {
            currentDocumentCount--;
            currentDocumentBytes -= getDocumentSize(doc);
            btree.put(uri, null);// Remove the document from the hash table
            if (doc.getDocumentTxt() != null) {
                String[] words = doc.getDocumentTxt().split("\\W+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        trie.delete(word, doc);
                    }
                }
            }
            // Also remove the document from any other data structures it might be in (e.g., the trie)
        }
    }

    private int getDocumentSize(Document doc) {
        if (doc.getDocumentTxt() != null) {
            return doc.getDocumentTxt().getBytes().length;
        } else if (doc.getDocumentBinaryData() != null) {
            return doc.getDocumentBinaryData().length;
        } else {
            return 0;
        }
    }

    @Override
    public String setMetadata(URI uri, String key, String value) {
        if (uri == null || uri.equals("") || key == null || key.isEmpty()) {
            throw new IllegalArgumentException("something is null or blank");
        }
        long lastUsedTime = System.nanoTime();
        Document document = btree.get(uri);
        Document duplicateDocument = duplicateBTree.get(uri);
        if (document == null) {
            throw new IllegalArgumentException("No document stored in the URI");
        }

        String oldValue = document.getMetadataValue(key);
        Consumer<URI> undoAction = u -> {
            Document doc = btree.get(u);
            if (doc != null) {
                doc.setMetadataValue(key, oldValue);
                doc.setLastUseTime(lastUsedTime);
            }
        };
        GenericCommand<URI> command = new GenericCommand<>(uri, undoAction);
        commandStack.push(command);
        document.setLastUseTime(System.nanoTime());
        this.documentHeap.reHeapify(uri);
        duplicateDocument.setMetadataValue(key,value);
        return document.setMetadataValue(key, value);
    }

    @Override
    public String getMetadata(URI uri, String key) {
        if (uri == null || Objects.equals(uri.toString(), "") || key == null || key == "") {
            throw new IllegalArgumentException("something is null or blank");
        }
        Document doc = btree.get(uri);
        if (doc == null) {
            throw new IllegalArgumentException("No document stored in the URI");
        }
        doc.setLastUseTime(System.nanoTime());
        this.documentHeap.reHeapify(uri);
        return doc.getMetadataValue(key);
    }

    private void validatePutArguments(InputStream input, URI url, DocumentFormat format) throws IOException{
        if (url == null || format == null || url.toString().isEmpty()) {
            throw new IllegalArgumentException("null or empty uri or format");
        }
        if(input == null){
            throw new IOException("null input");
        }
    }
    private void updateDocumentStore(Document newDocument, Document oldDocument, DocumentFormat format, URI url) throws IOException{
        if (oldDocument != null) {
            HashMap<String, String> oldMetadata = oldDocument.getMetadata();
            for (String key : oldMetadata.keySet()) {
                newDocument.setMetadataValue(key, oldMetadata.get(key));
            }
            currentDocumentBytes -= getDocumentSize(oldDocument);
        }
        DocumentImpl duplicateNewDocument = new DocumentImpl(newDocument.getKey(), newDocument.getDocumentTxt(), newDocument.getWordMap());
        duplicateNewDocument.setMetadata(newDocument.getMetadata());
        if(format == DocumentFormat.TXT){
            addToTrie(this.trie, newDocument.getDocumentTxt(), newDocument);
            addToTrie(this.duplicateTrie, duplicateNewDocument.getDocumentTxt(), duplicateNewDocument);
        }
        newDocument.setLastUseTime(System.nanoTime());
        documentHeap.insert(newDocument.getKey());
        this.documentHeap.reHeapify(newDocument.getKey());
        currentDocumentBytes += getDocumentSize(newDocument);
        if (btree.put(url, newDocument) == null) {
            currentDocumentCount++;
        }

        duplicateBTree.put(url,duplicateNewDocument);

        limitations();
    }

    private void insideUndo(Document newDocument, Document oldDocument,long undoTime){
        oldDocument.setLastUseTime(System.nanoTime()); // Restore the original last use time for the old document
        documentHeap.reHeapify(oldDocument.getKey()); // Reheapify to move the old document to the correct position in the heap
        currentDocumentBytes += getDocumentSize(oldDocument);
        newDocument.setLastUseTime(Long.MIN_VALUE);
        documentHeap.reHeapify(newDocument.getKey()); // Move the new document to the root of the heap
        documentHeap.remove(); // Remove the new document from the heap
        currentDocumentBytes -= getDocumentSize(newDocument);
        currentDocumentCount--;
        oldDocument.setLastUseTime(undoTime);
    }

    @Override
    public int put(InputStream input, URI url, DocumentFormat format) throws IOException {
        validatePutArguments(input, url, format);
        try{
            Document oldDocument = btree.get(url);
            byte[] data = (input != null) ? input.readAllBytes() : null;
            if (data != null && data.length > maxDocumentBytes) {
                throw new IllegalArgumentException("Document size exceeds allowed limit");}
            Document newDocument = (format == DocumentFormat.TXT) ? new DocumentImpl(url, new String(data), null) : new DocumentImpl(url, data);
            updateDocumentStore(newDocument, oldDocument, format , url);
            if(oldDocument != null && !oldDocument.equals(newDocument)) {
                oldDocument.setLastUseTime(System.nanoTime());
                documentHeap.reHeapify(oldDocument.getKey());}
            long undoTime = System.nanoTime();
            Consumer<URI> undoAction = u -> {
                if (oldDocument != null) {
                    this.btree.put(u, oldDocument);
                    if(oldDocument.getDocumentTxt() != null){
                        this.addToTrie(this.trie, oldDocument.getDocumentTxt(), oldDocument);}
                    insideUndo(newDocument,oldDocument,undoTime);}
                else {
                    this.btree.put(u, null);
                    if(newDocument.getDocumentTxt() != null){
                        this.removeFromTrie(this.trie, newDocument.getDocumentTxt(), newDocument);}
                    newDocument.setLastUseTime(Long.MIN_VALUE);
                    documentHeap.reHeapify(newDocument.getKey()); // Move the new document to the root of the heap
                    documentHeap.remove(); // Remove the new document from the heap
                    currentDocumentBytes -= getDocumentSize(newDocument);
                    currentDocumentCount--;}
            };
            GenericCommand<URI> command = new GenericCommand<>(url, undoAction);
            commandStack.push(command);
            return (oldDocument != null) ? oldDocument.hashCode() : 0;
        } catch (IOException e){
            throw new IOException("problem with reading the file" , e);
        }}


    private void addToTrie(TrieImpl trie, String text, Document doc) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                trie.put(word, doc);
            }
        }
    }

    private void removeFromTrie(TrieImpl trie, String text, Document doc) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                this.trie.delete(word, doc);

            }
        }
    }

    @Override
    public Document get(URI url) throws IOException{
        if (url == null || url.toString().isEmpty()) {
            throw new IllegalArgumentException("null URI");
        }
        boolean returned = false;
        Set<URI> keys = this.btree.getAllKeys();
        if(!keys.contains(url)){ //not in memory
            Document doc = this.btree.get(url);
            if (doc != null) {
                doc.setLastUseTime(System.nanoTime());
                documentHeap.insert(url);
                this.documentHeap.reHeapify(url);
                limitations();
            }
        }
        else {
            Document doc = this.btree.get(url);
            if (doc != null) {
                doc.setLastUseTime(System.nanoTime());
                this.documentHeap.reHeapify(url);
                limitations();
                }
        }
        return this.btree.get(url);
    }

    private void insideUndo(long nanoUsed, Document oldDocument){
        oldDocument.setLastUseTime(nanoUsed);
        documentHeap.insert(oldDocument.getKey());
        currentDocumentCount++;
        currentDocumentBytes += getDocumentSize(oldDocument);
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            documentHeap.reHeapify(oldDocument.getKey());
            URI leastUsedURI = documentHeap.remove();
            if (leastUsedURI != null) {

                if (btree.get(leastUsedURI).getDocumentTxt() != null) {
                    this.removeFromTrie(this.trie, btree.get(leastUsedURI).getDocumentTxt(), btree.get(leastUsedURI));
                }
                btree.put(leastUsedURI, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(btree.get(leastUsedURI));
            }
        }
    }

    private void updateLastUseTimeWhenUndo(Undoable command){
        if(command instanceof GenericCommand){
            URI uri = ((GenericCommand<URI>) command).getTarget();
            Document doc = btree.get(uri);
            if (doc != null) {
                doc.setLastUseTime(System.nanoTime());
            }
        } else if (command instanceof CommandSet){
            for(Object obj : ((CommandSet<?>) command)){
                if (obj instanceof GenericCommand) {
                    URI uri = ((GenericCommand<URI>) obj).getTarget();
                    Document doc = btree.get(uri);
                    if (doc != null) {
                        doc.setLastUseTime(System.nanoTime());
                    }
                }
            }
        }
    }
    @Override
    public void undo() throws IllegalStateException {
        if(commandStack.size() ==0){
            throw new IllegalStateException("Empty stack");
        }
        Undoable command = this.commandStack.pop();
        command.undo();
        updateLastUseTimeWhenUndo(command);
    }

    @Override
    public void undo(URI url) throws IllegalStateException{
        StackImpl<Undoable> tempStack = new StackImpl<>();
        boolean found = false;
        while (!(commandStack.size() == 0)) {
            Undoable command = commandStack.pop();
            if (command instanceof GenericCommand && ((GenericCommand<?>) command).getTarget().equals(url)) {
                command.undo();
                updateLastUseTimeWhenUndo(command);
                found = true;
                break;
            } else if (command instanceof CommandSet && ((CommandSet) command).containsTarget(url)) {
                ((CommandSet) command).undo(url);
                updateLastUseTimeWhenUndo(command);
                if (!((CommandSet) command).isEmpty()) {
                    tempStack.push(command);}
                found = true;
                break;
            } else {
                tempStack.push(command);
            }
        }
        while (!(tempStack.size() == 0)) {
            commandStack.push(tempStack.pop());
        }
        if (!found) {
            throw new IllegalStateException("No actions on the command stack for the given URI");
        }
    }

    //stage4 starts here!!!!!!
    @Override
    public List<Document> search(String keyword) {
        if (keyword == null) {
            return new ArrayList<>(); // Return an empty list if the keyword is null
        }
        List<Document> inMemory = this.trie.getSorted(keyword, Comparator.comparingInt(doc -> -doc.wordCount(keyword)));
        List<Document> duplicateToReturn = this.duplicateTrie.getSorted(keyword,Comparator.comparingInt(doc -> -doc.wordCount(keyword)));
        List<Document> toReturn = new ArrayList<>();
        for (Document doc : duplicateToReturn){
            //getAllKeys brings only not null
            Set<URI> duplicateSet = this.duplicateBTree.getAllKeys(); //everything
            Set<URI> set = this.btree.getAllKeys(); //memory
            if(!set.contains(doc.getKey()) && duplicateToReturn.contains(doc)){ //not in memory
                this.btree.get(doc.getKey());
                addToTrie(this.trie, doc.getDocumentTxt(), doc);
                toReturn.add(doc);
            }
        }
        Collections.reverse(toReturn);
        long currentTime = System.nanoTime();
        for (Document doc : toReturn) {
            if(doc != null){
                doc.setLastUseTime(currentTime);
                this.documentHeap.insert(doc.getKey());
                this.documentHeap.reHeapify(doc.getKey());
            }
        }
        return toReturn;
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        if(keywordPrefix == null){
            return new ArrayList<>();
        }
        Comparator<Document> comp = Comparator.comparingInt(doc -> doc.wordCount(keywordPrefix));
        List<Document> toReturn = new ArrayList<>();
        List<Document> inMemory = this.trie.getAllWithPrefixSorted(keywordPrefix, comp.reversed());
        List<Document> duplicateToReturn = this.duplicateTrie.getAllWithPrefixSorted(keywordPrefix,Comparator.comparingInt(doc -> -doc.wordCount(keywordPrefix)));
        for (Document doc : duplicateToReturn){
            //getAllKeys brings only not null
            //Set<URI> duplicateSet = this.duplicateBTree.getAllKeys(); //everything
            Set<URI> set = this.btree.getAllKeys(); //memory
            if(!set.contains(doc.getKey()) && duplicateToReturn.contains(doc)){ //not in memory
                btree.get(doc.getKey());
                addToTrie(this.trie, doc.getDocumentTxt(), doc);
                toReturn.add(doc);
            }
        }
        Collections.reverse(toReturn);
        long currentTime = System.nanoTime();
        for (Document doc : toReturn) {
            if(doc != null){
                doc.setLastUseTime(currentTime);
                this.documentHeap.insert(doc.getKey());
                this.documentHeap.reHeapify(doc.getKey());
            }
        }
        return toReturn;
    }

    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) {
        if (keysValues == null || keysValues.isEmpty()) {
            return null;
        }
        List<URI> duplicateToReturn = new ArrayList<>();
        Set<URI> allKeys = this.duplicateBTree.getAllKeys();
        Set<URI> inMemory = this.btree.getAllKeys(); //all docs in memory
        //this.addMetaData(inMemory);
        for(URI uri : allKeys){//all documents, search for those with the metadata
            Document curr = duplicateBTree.get(uri);
            if(containsMetadata(curr, keysValues)){
                duplicateToReturn.add(uri);
            } //now it contains only documents with the correct metadata
        }
        List<Document> toReturn = new ArrayList<>();
        for (URI uri : duplicateToReturn) {
            if(!inMemory.contains(uri) && duplicateToReturn.contains(uri)){ // not in memory but contains the metadata
                Document doc = this.btree.get(uri);
                addToTrie(this.trie, doc.getDocumentTxt(), doc);
                toReturn.add(doc);
            }
        }
        return toReturn;
    }

    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        List<Document> toReturn = new ArrayList<>();
        if (keyword == null || keysValues == null || keysValues.isEmpty()) {
            return toReturn;
        }
        List<Document> documentsWithKeyword = search(keyword); // get the documents containing the keyword
        for(Document doc : documentsWithKeyword){
            if(this.btree.getAllKeys().contains(doc.getKey())){ //in memory and has the prefix
                try {
                    this.btree.moveToDisk(doc.getKey()); //it contains the prefix and now we delete it again to check if it contains the metadata also
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        List<Document> documentsWithMetadata = searchByMetadata(keysValues);
        List<URI> uriWithMetadata = new ArrayList<>();
        for(Document doc : documentsWithMetadata){
            uriWithMetadata.add(doc.getKey());
        }
        for (Document document : documentsWithKeyword) {
            if (uriWithMetadata.contains(document.getKey())) {
                toReturn.add(document);
                document.setLastUseTime(System.nanoTime());
                this.documentHeap.reHeapify(document.getKey());
            }
        }
        toReturn.sort(Comparator.comparingInt(doc -> -doc.wordCount(keyword)));
        Collections.reverse(toReturn);
        return toReturn;
    }

    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> toReturn = new ArrayList<>();
        if (keywordPrefix == null || keysValues == null || keysValues.isEmpty()) {
            return toReturn;
        }
        List<Document> documentsWithPrefix = searchByPrefix(keywordPrefix); // get the documents containing the keyword
        for(Document doc : documentsWithPrefix){
            if(this.btree.getAllKeys().contains(doc.getKey())){ //in memory and has the prefix
                try {
                    this.btree.moveToDisk(doc.getKey()); //it contains the prefix and now we delete it again to check if it contains the metadata also
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        List<Document> documentsWithMetadata = searchByMetadata(keysValues);
        List<URI> uriWithMetadata = new ArrayList<>();
        for(Document doc : documentsWithMetadata){
            uriWithMetadata.add(doc.getKey());
        }
        for (Document document : documentsWithPrefix) {
            if (uriWithMetadata.contains(document.getKey())) {
                toReturn.add(document);
                document.setLastUseTime(System.nanoTime());
                this.documentHeap.reHeapify(document.getKey());
            }
        }
        toReturn.sort(Comparator.comparingInt(doc -> -doc.wordCount(keywordPrefix)));
        Collections.reverse(toReturn);
        return toReturn;
    }

    private void insideUndoAll(Document doc, long lastUseTime){
        doc.setLastUseTime(lastUseTime);
        documentHeap.insert(doc.getKey());
        currentDocumentCount++;
        currentDocumentBytes += getDocumentSize(doc);
        // Check if adding the document breaks any limitations
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            // Remove the least recently used document
            URI leastUsedURI = documentHeap.remove();
            if (leastUsedURI != null) {

                if (btree.get(leastUsedURI).getDocumentTxt() != null) {
                    this.removeFromTrie(this.trie, btree.get(leastUsedURI).getDocumentTxt(), btree.get(leastUsedURI));
                }
                this.btree.put(leastUsedURI, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(btree.get(leastUsedURI));
            }
        }
    }

    private void setSomething(Document doc, URI uriToDelete){
        doc.setLastUseTime(Long.MIN_VALUE);
        documentHeap.reHeapify(doc.getKey());
        documentHeap.remove();
        this.btree.put(doc.getKey(), null);
        currentDocumentCount--;
        currentDocumentBytes -= getDocumentSize(doc);
    }

    @Override
    public boolean delete(URI url) {
        if (url == null || url.toString().isEmpty()) {
            throw new IllegalArgumentException("null URI");
        }
        Document oldDocument = btree.get(url);
        if (oldDocument == null) {
            return false;
        }
        if(oldDocument.getDocumentTxt() != null){
            String[] words = oldDocument.getDocumentTxt().split("\\W+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    this.trie.delete(word, oldDocument);
                }}}
        long nanoUsed = System.nanoTime();
        oldDocument.setLastUseTime(Long.MIN_VALUE);
        documentHeap.reHeapify(oldDocument.getKey());
        documentHeap.remove();
        currentDocumentCount--;
        currentDocumentBytes -= getDocumentSize(oldDocument);
        Consumer<URI> undoAction = u -> {
            //btree.put(u, oldDocument);
            this.btree.get(oldDocument.getKey());
            if(oldDocument.getDocumentTxt() != null){
                this.addToTrie(this.trie, oldDocument.getDocumentTxt(), oldDocument);
            }
            insideUndo(nanoUsed, oldDocument);
        };
        GenericCommand<URI> command = new GenericCommand<>(url, undoAction);
        commandStack.push(command);
        try {
            btree.moveToDisk(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keyword == null || keyword.isEmpty()) {
            return deletedDocuments;
        }
        Set<Document> documentsToDelete = new HashSet<>(this.trie.get(keyword));
        CommandSet<URI> commandSet = new CommandSet<>();
        if (documentsToDelete.isEmpty()) {
            return deletedDocuments;
        }
        long lastUseTime = System.nanoTime();
        for(Document doc : documentsToDelete){
            URI uriToDelete = doc.getKey();
            try {
                this.btree.moveToDisk(doc.getKey());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            deletedDocuments.add(uriToDelete);
            if (doc.getDocumentTxt() != null) { // Check if the document is a text document
                String[] words = doc.getDocumentTxt().split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                    }
                }
            }
            Document originalDocument = this.duplicateBTree.get(uriToDelete);
            setSomething(doc, uriToDelete);
            GenericCommand<URI> command = new GenericCommand<>(uriToDelete, u -> {
                this.addToTrie(this.trie, doc.getDocumentTxt(), doc);
                if (originalDocument != null) {
                    //this.btree.put(doc.getKey(), originalDocument); // Restore the document with metadata
                    this.btree.get(doc.getKey());
                }
                insideUndoAll(doc, lastUseTime);
            });
            commandSet.addCommand(command);}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keywordPrefix == null || keywordPrefix.isEmpty()) {
            return deletedDocuments;}
        CommandSet<URI> commandSet = new CommandSet<>();
        List<Document> documentsToDelete = new ArrayList<>();
        documentsToDelete = this.trie.getAllWithPrefixSorted(keywordPrefix, Comparator.comparingInt(doc -> -doc.wordCount(keywordPrefix)));
        long lastUsedTime = System.nanoTime();
        for (Document document : documentsToDelete) {
            URI uriToDelete = document.getKey();
            try{
                this.btree.moveToDisk(uriToDelete);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            deletedDocuments.add(uriToDelete);
            String[] words = document.getDocumentTxt().split("\\W+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    this.trie.delete(word, document);
                }
            }
            Document originalDocument = this.duplicateBTree.get(document.getKey()); // Get the original document
            insideAllPrefix(document);
            GenericCommand<URI> command = new GenericCommand<>(document.getKey(), u -> {
                //this.trie.put(document.getDocumentTxt(), document);
                this.addToTrie(this.trie, document.getDocumentTxt(), document);
                if (originalDocument != null) {
                   // this.btree.put(document.getKey(), originalDocument);
                    this.btree.get(document.getKey());
                    insideUndoAllPrefix(document, lastUsedTime);
                }
            });
            commandSet.addCommand(command);}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }

    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keysValues == null || keysValues.isEmpty()) {
            return deletedDocuments;}
        CommandSet<URI> commandSet = new CommandSet<>();
        long nanoUsed = System.nanoTime();
        for(URI uri : this.btree.getAllKeys()) { // all the keys
            Document doc = duplicateBTree.get(uri);
            if (containsMetadata(doc, keysValues)) {
                String text = doc.getDocumentTxt();
                try{
                    this.btree.moveToDisk(doc.getKey());
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
                deletedDocuments.add(doc.getKey());
                String[] words = text != null ? text.split("\\s+") : new String[0];
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                    }
                }
                Document originalDocument = this.duplicateBTree.get(doc.getKey()); // Get the original document
                if (originalDocument != null) {
                    this.btree.put(doc.getKey(), originalDocument); }
                //handleHeap(doc);
                GenericCommand<URI> command = new GenericCommand<>(uri, u -> {
                    if (doc.getDocumentTxt() != null) {
                        for (String word : words) {
                            if (!word.isEmpty()) {
                                this.trie.put(word, doc);}}}
                    //this.btree.put(uri, doc);
                    this.btree.get(doc.getKey());
                    doc.setLastUseTime(nanoUsed);
                    documentHeap.insert(doc.getKey());
                    currentDocumentCount++;
                    currentDocumentBytes += getDocumentSize(doc);
                    checkLimitations();});
                commandSet.addCommand(command);
                deletedDocuments.add(uri);}}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }

    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keyword == null || keyword.isEmpty() || keysValues == null || keysValues.isEmpty()) {
            return deletedDocuments;}
        CommandSet<URI> commandSet = new CommandSet<>();
        List<Document> documentsToDelete = trie.getSorted(keyword, Comparator.comparingInt(doc -> -doc.wordCount(keyword)));
        long nanoUsed = System.nanoTime();
        for (Document doc : documentsToDelete) {
            if (containsMetadata(doc, keysValues)) {
                URI uriToDelete = doc.getKey();
                try{
                    this.btree.moveToDisk(uriToDelete);
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
                deletedDocuments.add(uriToDelete);
                String[] words = doc.getDocumentTxt().split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                    }
                }
                //handleHeap(doc);
                GenericCommand<URI> command = new GenericCommand<>(doc.getKey(), u -> {
                    if (doc.getDocumentTxt() != null) {
                        for (String word : words) {
                            if (!word.isEmpty()) {
                                this.trie.put(word, doc);}}}
                    //this.btree.put(doc.getKey(), doc);
                    this.btree.get(doc.getKey());
                    doc.setLastUseTime(nanoUsed);
                    documentHeap.insert(doc.getKey());
                    currentDocumentCount++;
                    currentDocumentBytes += getDocumentSize(doc);
                    checkLimitationsM();
                });
                commandSet.addCommand(command);
                btree.put(doc.getKey(), null);
                deletedDocuments.add(doc.getKey());
            }}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }

    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keywordPrefix == null || keywordPrefix.isEmpty() || keysValues == null || keysValues.isEmpty()) {
            return deletedDocuments;
        }
        CommandSet<URI> commandSet = new CommandSet<>();
        List<Document> docsWithPrefix = trie.getAllWithPrefixSorted(keywordPrefix, Comparator.comparingInt(doc -> -doc.wordCount(keywordPrefix)));
        long nanoUsed = System.nanoTime();
        for (Document doc : docsWithPrefix) {
            if (containsMetadata(doc, keysValues)) {
                URI uriToDelete = doc.getKey();
                try{
                    this.btree.moveToDisk(uriToDelete);
                } catch (IOException e){
                    throw new RuntimeException(e);
                }
                deletedDocuments.add(uriToDelete);
                String[] words = doc.getDocumentTxt().split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                    }
                }
                //handleHeap(doc);
                GenericCommand<URI> command = new GenericCommand<>(doc.getKey(), u -> {
                    if (doc.getDocumentTxt() != null) {
                        for (String word : words) {
                            if (!word.isEmpty()) {
                                this.trie.put(word, doc);}}}
                    //this.btree.put(doc.getKey(), doc);
                    this.btree.get(doc.getKey());
                    doc.setLastUseTime(nanoUsed);
                    documentHeap.insert(doc.getKey());
                    currentDocumentCount++;
                    currentDocumentBytes += getDocumentSize(doc);
                    limitationsLastUndo();
                });
                commandSet.addCommand(command);
                btree.put(doc.getKey(), null);
                deletedDocuments.add(doc.getKey());}}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }

    private void insideAllPrefix(Document document){
        document.setLastUseTime(Long.MIN_VALUE);
        documentHeap.reHeapify(document.getKey());
        documentHeap.remove();
        this.btree.put(document.getKey(), null);
        currentDocumentCount--;
        currentDocumentBytes -= getDocumentSize(document);
    }

    private void insideUndoAllPrefix(Document document, long lastUsedTime){
        document.setLastUseTime(lastUsedTime);
        documentHeap.insert(document.getKey());
        currentDocumentCount++;
        currentDocumentBytes += getDocumentSize(document);
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            URI leastUsedURI = documentHeap.remove();
            if (leastUsedURI != null) {
                if (btree.get(leastUsedURI).getDocumentTxt() != null) {
                    this.removeFromTrie(this.trie, btree.get(leastUsedURI).getDocumentTxt(), btree.get(leastUsedURI));}
                this.btree.put(leastUsedURI, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(btree.get(leastUsedURI));}}
    }

    private boolean containsMetadata(Document doc, Map<String, String> keysValues) {
        for (Map.Entry<String, String> entry : keysValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String docValue = doc.getMetadataValue(key);
            if (docValue == null || !docValue.equals(value)) {
                return false;
            }
        }
        return true;
    }

    private void handleHeap(Document doc){
        doc.setLastUseTime(Long.MIN_VALUE);
        documentHeap.reHeapify(doc.getKey());
        documentHeap.remove();
        currentDocumentCount--;
        currentDocumentBytes -= getDocumentSize(doc);
    }
    private void checkLimitations(){
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            URI leastUsedURI = documentHeap.remove();
            if (leastUsedURI != null) {
                if (btree.get(leastUsedURI).getDocumentTxt() != null) {
                    String[] leastUsedWords = btree.get(leastUsedURI).getDocumentTxt().split("\\s+");
                    for (String word : leastUsedWords) {
                        if (!word.isEmpty()) {
                            this.trie.delete(word, btree.get(leastUsedURI));
                        }
                    }
                }
                this.btree.put(leastUsedURI, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(btree.get(leastUsedURI));
            }
        }
    }

    private void checkLimitationsM(){
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            // Remove the least recently used document
            URI leastUsedURI = documentHeap.remove();
            if (leastUsedURI != null) {

                if (btree.get(leastUsedURI).getDocumentTxt() != null) {
                    String[] leastUsedWords = btree.get(leastUsedURI).getDocumentTxt().split("\\s+");
                    for (String word : leastUsedWords) {
                        if (!word.isEmpty()) {
                            this.trie.delete(word, btree.get(leastUsedURI));
                        }
                    }
                }
                this.btree.put(leastUsedURI, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(btree.get(leastUsedURI));
            }
        }
    }

    private void limitationsLastUndo(){
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            URI leastUsedURI = documentHeap.remove();
            if (leastUsedURI != null) {

                if (btree.get(leastUsedURI).getDocumentTxt() != null) {
                    String[] leastUsedWords = btree.get(leastUsedURI).getDocumentTxt().split("\\s+");
                    for (String word : leastUsedWords) {
                        if (!word.isEmpty()) {
                            this.trie.delete(word, btree.get(leastUsedURI));
                        }
                    }
                }
                this.btree.put(leastUsedURI, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(btree.get(leastUsedURI));
            }
        }
    }
}