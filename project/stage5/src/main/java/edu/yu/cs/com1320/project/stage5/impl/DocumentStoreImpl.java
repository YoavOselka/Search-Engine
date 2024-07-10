package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.MinHeap;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.undo.Command;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;

public class DocumentStoreImpl implements DocumentStore {
    private TrieImpl<Document> trie;
    private HashTableImpl<URI, Document> documents;
    private StackImpl<Undoable> commandStack;
    private int maxDocumentCount = Integer.MAX_VALUE;
    private int maxDocumentBytes = Integer.MAX_VALUE;
    private int currentDocumentCount = 0;
    private int currentDocumentBytes = 0;
    private MinHeapImpl<Document> documentHeap = new MinHeapImpl<>();
    private HashTableImpl<String, Integer> wordCountMap;

    //text.replaceAll("[^a-zA-Z0-9\\s]", "")
    public DocumentStoreImpl() {
        this.documents = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.wordCountMap = new HashTableImpl<>();
    }

    //delete this method before submission!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public StackImpl<Undoable> getCommandStack() {
        return commandStack;
    }
    public TrieImpl<Document> getTrie(){
        return trie;
    }
    public int getCurrentDocumentBytes(){
        return this.currentDocumentBytes;
    }
    public MinHeapImpl<Document> getDocumentHeap(){
        return this.documentHeap;
    }
    public int getCurrentDocumentCount(){
        return this.currentDocumentCount;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be bigger than 0");
        }
        this.maxDocumentCount = limit;
        limitations();
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be bigger than 0");
        }
        this.maxDocumentBytes = limit;
        limitations();
    }

    private void limitations() {
        while (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            Document leastUsed = documentHeap.remove(); // Remove the least recently used document from the heap
            removeDocument(leastUsed.getKey()); // Remove the document from the store
        }
    }

    private void removeDocument(URI uri) {
        Document doc = documents.get(uri);
        if (doc != null) {
            currentDocumentCount--;
            currentDocumentBytes -= getDocumentSize(doc);
            documents.put(uri, null);// Remove the document from the hash table
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
        Document document = documents.get(uri);
        if (document == null) {
            throw new IllegalArgumentException("No document stored in the URI");
        }

        String oldValue = document.getMetadataValue(key);
        Consumer<URI> undoAction = u -> {
            Document doc = documents.get(u);
            if (doc != null) {
                doc.setMetadataValue(key, oldValue);
                doc.setLastUseTime(lastUsedTime);
            }
        };
        GenericCommand<URI> command = new GenericCommand<>(uri, undoAction);
        commandStack.push(command);
        document.setLastUseTime(System.nanoTime());
        this.documentHeap.reHeapify(document);
        return document.setMetadataValue(key, value);
    }

    @Override
    public String getMetadata(URI uri, String key) {
        if (uri == null || Objects.equals(uri.toString(), "") || key == null || key == "") {
            throw new IllegalArgumentException("something is null or blank");
        }
        Document doc = documents.get(uri);
        if (doc == null) {
            throw new IllegalArgumentException("No document stored in the URI");
        }
        doc.setLastUseTime(System.nanoTime());
        this.documentHeap.reHeapify(doc);
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
    private void updateDocumentStore(Document newDocument, Document oldDocument, DocumentFormat format, URI url) {
        if (oldDocument != null) {
            HashTable<String, String> oldMetadata = oldDocument.getMetadata();
            for (String key : oldMetadata.keySet()) {
                newDocument.setMetadataValue(key, oldMetadata.get(key));
            }
            currentDocumentBytes -= getDocumentSize(oldDocument);
        }
        if(format == DocumentFormat.TXT){
            addToTrie(this.trie, newDocument.getDocumentTxt(), newDocument);
        }
        newDocument.setLastUseTime(System.nanoTime());
        documentHeap.insert(newDocument);
        this.documentHeap.reHeapify(newDocument);
        currentDocumentBytes += getDocumentSize(newDocument);
        if (documents.put(url, newDocument) == null) {
            currentDocumentCount++;
        }
        limitations();
    }

    private void insideUndo(Document newDocument, Document oldDocument,long undoTime){
        oldDocument.setLastUseTime(System.nanoTime()); // Restore the original last use time for the old document
        documentHeap.reHeapify(oldDocument); // Reheapify to move the old document to the correct position in the heap
        currentDocumentBytes += getDocumentSize(oldDocument);
        newDocument.setLastUseTime(Long.MIN_VALUE);
        documentHeap.reHeapify(newDocument); // Move the new document to the root of the heap
        documentHeap.remove(); // Remove the new document from the heap
        currentDocumentBytes -= getDocumentSize(newDocument);
        currentDocumentCount--;
        oldDocument.setLastUseTime(undoTime);
    }

      @Override
      public int put(InputStream input, URI url, DocumentFormat format) throws IOException {
          validatePutArguments(input, url, format);
          try{
              Document oldDocument = documents.get(url);
              byte[] data = (input != null) ? input.readAllBytes() : null;
              if (data != null && data.length > maxDocumentBytes) {
                  throw new IllegalArgumentException("Document size exceeds allowed limit");}
              Document newDocument = (format == DocumentFormat.TXT) ?
                      new DocumentImpl(url, new String(data)) :
                      new DocumentImpl(url, data);
              updateDocumentStore(newDocument, oldDocument, format , url);
              if(oldDocument != null && !oldDocument.equals(newDocument)) {
                  oldDocument.setLastUseTime(System.nanoTime());
                  documentHeap.reHeapify(oldDocument);}
              long undoTime = System.nanoTime();
              Consumer<URI> undoAction = u -> {
                  if (oldDocument != null) {
                      this.documents.put(u, oldDocument);
                      if(oldDocument.getDocumentTxt() != null){
                          this.addToTrie(this.trie, oldDocument.getDocumentTxt(), oldDocument);}
                      insideUndo(newDocument,oldDocument,undoTime);}
                  else {
                      this.documents.put(u, null);
                      if(newDocument.getDocumentTxt() != null){
                      this.removeFromTrie(this.trie, newDocument.getDocumentTxt(), newDocument);}
                      newDocument.setLastUseTime(Long.MIN_VALUE);
                      documentHeap.reHeapify(newDocument); // Move the new document to the root of the heap
                      documentHeap.remove(); // Remove the new document from the heap
                      currentDocumentBytes -= getDocumentSize(newDocument);
                      currentDocumentCount--;}};
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
    public Document get(URI url) {
        if (url == null || url.toString().isEmpty()) {
            throw new IllegalArgumentException("null URI");
        }
        Document doc = this.documents.get(url);
        if (doc != null) {
            doc.setLastUseTime(System.nanoTime());
            this.documentHeap.reHeapify(doc);
        }
        return doc;
    }

    private void insideUndo(long nanoUsed, Document oldDocument){
        oldDocument.setLastUseTime(nanoUsed);
        documentHeap.insert(oldDocument);
        currentDocumentCount++;
        currentDocumentBytes += getDocumentSize(oldDocument);
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            documentHeap.reHeapify(oldDocument);
            Document leastUsedDocument = documentHeap.remove();
            if (leastUsedDocument != null) {
                URI leastUsedUri = leastUsedDocument.getKey();
                if (leastUsedDocument.getDocumentTxt() != null) {
                    this.removeFromTrie(this.trie, leastUsedDocument.getDocumentTxt(), leastUsedDocument);
                }
                documents.put(leastUsedUri, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(leastUsedDocument);
            }
        }
    }
@Override
public boolean delete(URI url) {
    if (url == null || url.toString().isEmpty()) {
        throw new IllegalArgumentException("null URI");
    }
    Document oldDocument = documents.get(url);
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
    documentHeap.reHeapify(oldDocument);
    documentHeap.remove();
    currentDocumentCount--;
    currentDocumentBytes -= getDocumentSize(oldDocument);
    Consumer<URI> undoAction = u -> {
        documents.put(u, oldDocument);
        if(oldDocument.getDocumentTxt() != null){
            this.addToTrie(this.trie, oldDocument.getDocumentTxt(), oldDocument);
        }
        insideUndo(nanoUsed, oldDocument);
    };
    GenericCommand<URI> command = new GenericCommand<>(url, undoAction);
    commandStack.push(command);
    documents.put(url, null);
    return true;
}

    private void addMetadata(Document doc, Map<String, String> metadata) {
        if (metadata != null && !metadata.isEmpty()) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                doc.setMetadataValue(entry.getKey(), entry.getValue());
            }
        }
    }

    private void updateLastUseTimeWhenUndo(Undoable command){
        if(command instanceof GenericCommand){
            URI uri = ((GenericCommand<URI>) command).getTarget();
            Document doc = documents.get(uri);
            if (doc != null) {
                doc.setLastUseTime(System.nanoTime());
            }
        } else if (command instanceof CommandSet){
            for(Object obj : ((CommandSet<?>) command)){
                if (obj instanceof GenericCommand) {
                    URI uri = ((GenericCommand<URI>) obj).getTarget();
                    Document doc = documents.get(uri);
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
        List<Document> toReturn = new ArrayList<>();
        toReturn = this.trie.getSorted(keyword, Comparator.comparingInt(doc -> -doc.wordCount(keyword)));
        Collections.reverse(toReturn);
        long currentTime = System.nanoTime();
        for (Document doc : toReturn) {
            if(doc != null){
                doc.setLastUseTime(currentTime);
                this.documentHeap.reHeapify(doc);
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
        List<Document> toReturn = this.trie.getAllWithPrefixSorted(keywordPrefix, comp.reversed());
        Collections.reverse(toReturn);
        long currentTime = System.nanoTime();
        for (Document doc : toReturn) {
            if(doc != null){
                doc.setLastUseTime(currentTime);
                this.documentHeap.reHeapify(doc);
            }
        }
        return toReturn;
    }
private void insideUndoAll(Document doc, long lastUseTime){
    doc.setLastUseTime(lastUseTime);
    documentHeap.insert(doc);
    currentDocumentCount++;
    currentDocumentBytes += getDocumentSize(doc);
    // Check if adding the document breaks any limitations
    if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
        // Remove the least recently used document
        Document leastUsedDocument = documentHeap.remove();
        if (leastUsedDocument != null) {
            URI leastUsedUri = leastUsedDocument.getKey();
            if (leastUsedDocument.getDocumentTxt() != null) {
                this.removeFromTrie(this.trie, leastUsedDocument.getDocumentTxt(), leastUsedDocument);
            }
            this.documents.put(leastUsedUri, null);
            currentDocumentCount--;
            currentDocumentBytes -= getDocumentSize(leastUsedDocument);
        }
    }
}

private void setSomething(Document doc, URI uriToDelete){
    doc.setLastUseTime(Long.MIN_VALUE);
    documentHeap.reHeapify(doc);
    documentHeap.remove();
    this.documents.put(doc.getKey(), null);
    currentDocumentCount--;
    currentDocumentBytes -= getDocumentSize(doc);
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
            deletedDocuments.add(uriToDelete);
            if (doc.getDocumentTxt() != null) { // Check if the document is a text document
                String[] words = doc.getDocumentTxt().split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                    }}}
            Document originalDocument = this.documents.get(uriToDelete);
            setSomething(doc, uriToDelete);
            GenericCommand<URI> command = new GenericCommand<>(uriToDelete, u -> {
                //this.trie.put(doc.getDocumentTxt(), doc);
                this.addToTrie(this.trie, doc.getDocumentTxt(), doc);
                if (originalDocument != null) {
                    this.documents.put(doc.getKey(), originalDocument); // Restore the document with metadata
                }
                insideUndoAll(doc, lastUseTime);
            });
            commandSet.addCommand(command);}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }

    private void insideAllPrefix(Document document){
        document.setLastUseTime(Long.MIN_VALUE);
        documentHeap.reHeapify(document);
        documentHeap.remove();
        this.documents.put(document.getKey(), null);
        currentDocumentCount--;
        currentDocumentBytes -= getDocumentSize(document);
    }

    private void insideUndoAllPrefix(Document document, long lastUsedTime){
        document.setLastUseTime(lastUsedTime);
        documentHeap.insert(document);
        currentDocumentCount++;
        currentDocumentBytes += getDocumentSize(document);
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            Document leastUsedDocument = documentHeap.remove();
            if (leastUsedDocument != null) {
                URI leastUsedUri = leastUsedDocument.getKey();
                if (leastUsedDocument.getDocumentTxt() != null) {
                    this.removeFromTrie(this.trie, leastUsedDocument.getDocumentTxt(), leastUsedDocument);}
                this.documents.put(leastUsedUri, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(leastUsedDocument);}}
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
            String text = document.getDocumentTxt();
            String[] words = text.split("\\W+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    this.trie.delete(word, document);
                    deletedDocuments.add(document.getKey());}}
            Document originalDocument = this.documents.get(document.getKey()); // Get the original document
            insideAllPrefix(document);
            GenericCommand<URI> command = new GenericCommand<>(document.getKey(), u -> {
                //this.trie.put(document.getDocumentTxt(), document);
                this.addToTrie(this.trie, document.getDocumentTxt(), document);
                if (originalDocument != null) {
                    this.documents.put(document.getKey(), originalDocument);
                    insideUndoAllPrefix(document, lastUsedTime);
                }});
            commandSet.addCommand(command);}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }


    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) {
        List<Document> toReturn = new ArrayList<>();
        if (keysValues == null || keysValues.isEmpty()) {
            return toReturn;
        }
        for (Document document : this.documents.values()) {
            if(document!= null &&containsMetadata(document, keysValues)) {
                if (containsMetadata(document, keysValues)) {
                    toReturn.add(document);
                    document.setLastUseTime(System.nanoTime());
                    this.documentHeap.reHeapify(document);
                }
            }
        }
        return toReturn;
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
    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        List<Document> toReturn = new ArrayList<>();
        if (keyword == null || keysValues == null || keysValues.isEmpty()) {
            return toReturn;
        }
        List<Document> documentsWithKeyword = search(keyword); // get the documents containing the keyword
        for (Document document : documentsWithKeyword) {
            if (containsMetadata(document, keysValues)) {
                toReturn.add(document);
                document.setLastUseTime(System.nanoTime());
                this.documentHeap.reHeapify(document);
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
        List<Document> documentsWithKeyword = searchByPrefix(keywordPrefix); // get the documents containing the keyword
        for (Document document : documentsWithKeyword) {
            if (containsMetadata(document, keysValues)) {
                toReturn.add(document);
                document.setLastUseTime(System.nanoTime());
                this.documentHeap.reHeapify(document);
            }
        }
        toReturn.sort(Comparator.comparingInt(doc -> -doc.wordCount(keywordPrefix)));
        Collections.reverse(toReturn);
        return toReturn;
    }

    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keysValues == null || keysValues.isEmpty()) {
            return deletedDocuments;}
        CommandSet<URI> commandSet = new CommandSet<>();
        long nanoUsed = System.nanoTime();
        for(URI uri : this.documents.keySet()) {
            Document doc = documents.get(uri);
            if (containsMetadata(doc, keysValues)) {
                    String text = doc.getDocumentTxt();
                    String[] words = text != null ? text.split("\\s+") : new String[0];
                    for (String word : words) {
                        if (!word.isEmpty()) {
                            this.trie.delete(word, doc);}}
                    Document originalDocument = this.documents.get(doc.getKey()); // Get the original document
                    if (originalDocument != null) {
                        this.documents.put(doc.getKey(), originalDocument); }
                    insideSomething(doc);
                    GenericCommand<URI> command = new GenericCommand<>(uri, u -> {
                        if (doc.getDocumentTxt() != null) {
                            for (String word : words) {
                                if (!word.isEmpty()) {
                                    this.trie.put(word, doc);}}}
                        this.documents.put(uri, doc);
                        doc.setLastUseTime(nanoUsed);
                        documentHeap.insert(doc);
                        currentDocumentCount++;
                        currentDocumentBytes += getDocumentSize(doc);
                        checkLimitations();});
                    commandSet.addCommand(command);
                    deletedDocuments.add(uri);}}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }

    private void insideSomething(Document doc){
        doc.setLastUseTime(Long.MIN_VALUE);
        documentHeap.reHeapify(doc);
        documentHeap.remove();
        documents.put(doc.getKey(), null);
        currentDocumentCount--;
        currentDocumentBytes -= getDocumentSize(doc);
    }
    private void checkLimitations(){
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            Document leastUsedDocument = documentHeap.remove();
            if (leastUsedDocument != null) {
                URI leastUsedUri = leastUsedDocument.getKey();
                if (leastUsedDocument.getDocumentTxt() != null) {
                    String[] leastUsedWords = leastUsedDocument.getDocumentTxt().split("\\s+");
                    for (String word : leastUsedWords) {
                        if (!word.isEmpty()) {
                            this.trie.delete(word, leastUsedDocument);
                        }
                    }
                }
                this.documents.put(leastUsedUri, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(leastUsedDocument);
            }
        }
    }
    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keyword == null || keyword.isEmpty() || keysValues == null || keysValues.isEmpty()) {
            return deletedDocuments;}
        CommandSet<URI> commandSet = new CommandSet<>();
        List<Document> docs = trie.getSorted(keyword, Comparator.comparingInt(doc -> -doc.wordCount(keyword)));
        long nanoUsed = System.nanoTime();
        for (Document doc : docs) {
            if (containsMetadata(doc, keysValues)) {
                String text = doc.getDocumentTxt();
                String[] words = text.split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                    }}
                inside(doc);
                GenericCommand<URI> command = new GenericCommand<>(doc.getKey(), u -> {
                    if (doc.getDocumentTxt() != null) {
                        for (String word : words) {
                            if (!word.isEmpty()) {
                                this.trie.put(word, doc);}}}
                    this.documents.put(doc.getKey(), doc);
                    doc.setLastUseTime(nanoUsed);
                    documentHeap.insert(doc);
                    currentDocumentCount++;
                    currentDocumentBytes += getDocumentSize(doc);
                    checkLimitationsM();
                });
                commandSet.addCommand(command);
                documents.put(doc.getKey(), null);
                deletedDocuments.add(doc.getKey());
            }}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }

private void inside(Document doc){
    doc.setLastUseTime(Long.MIN_VALUE);
    documentHeap.reHeapify(doc);
    documentHeap.remove();
    currentDocumentCount--;
    currentDocumentBytes -= getDocumentSize(doc);
}
    private void checkLimitationsM(){
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            // Remove the least recently used document
            Document leastUsedDocument = documentHeap.remove();
            if (leastUsedDocument != null) {
                URI leastUsedUri = leastUsedDocument.getKey();
                if (leastUsedDocument.getDocumentTxt() != null) {
                    String[] leastUsedWords = leastUsedDocument.getDocumentTxt().split("\\s+");
                    for (String word : leastUsedWords) {
                        if (!word.isEmpty()) {
                            this.trie.delete(word, leastUsedDocument);
                        }
                    }
                }
                this.documents.put(leastUsedUri, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(leastUsedDocument);
            }
        }
    }
    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keywordPrefix == null || keywordPrefix.isEmpty() || keysValues == null || keysValues.isEmpty()) {
            return deletedDocuments;
        }
        CommandSet<URI> commandSet = new CommandSet<>();
        List<Document> docs = trie.getAllWithPrefixSorted(keywordPrefix, Comparator.comparingInt(doc -> -doc.wordCount(keywordPrefix)));
        long nanoUsed = System.nanoTime();
        for (Document doc : docs) {
            if (containsMetadata(doc, keysValues)) {
                String text = doc.getDocumentTxt();
                String[] words = text.split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                        deletedDocuments.add(doc.getKey());}}
                insideLast(doc);
                GenericCommand<URI> command = new GenericCommand<>(doc.getKey(), u -> {
                    if (doc.getDocumentTxt() != null) {
                        for (String word : words) {
                            if (!word.isEmpty()) {
                                this.trie.put(word, doc);}}}
                    this.documents.put(doc.getKey(), doc);
                    doc.setLastUseTime(nanoUsed);
                    documentHeap.insert(doc);
                    currentDocumentCount++;
                    currentDocumentBytes += getDocumentSize(doc);
                    limitationsLastUndo();});
                commandSet.addCommand(command);
                documents.put(doc.getKey(), null);
                deletedDocuments.add(doc.getKey());}}
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }
    private void insideLast(Document doc){
        doc.setLastUseTime(Long.MIN_VALUE);
        documentHeap.reHeapify(doc);
        documentHeap.remove();
        currentDocumentCount--;
        currentDocumentBytes -= getDocumentSize(doc);
    }
    private void limitationsLastUndo(){
        if (currentDocumentCount > maxDocumentCount || currentDocumentBytes > maxDocumentBytes) {
            Document leastUsedDocument = documentHeap.remove();
            if (leastUsedDocument != null) {
                URI leastUsedUri = leastUsedDocument.getKey();
                if (leastUsedDocument.getDocumentTxt() != null) {
                    String[] leastUsedWords = leastUsedDocument.getDocumentTxt().split("\\s+");
                    for (String word : leastUsedWords) {
                        if (!word.isEmpty()) {
                            this.trie.delete(word, leastUsedDocument);
                        }
                    }
                }
                this.documents.put(leastUsedUri, null);
                currentDocumentCount--;
                currentDocumentBytes -= getDocumentSize(leastUsedDocument);
            }
        }
    }
}
