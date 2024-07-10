package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
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
    private HashTableImpl<String, Integer> wordCountMap;

    //text.replaceAll("[^a-zA-Z0-9\\s]", "")
    public DocumentStoreImpl(){
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

    @Override
    public String setMetadata(URI uri, String key, String value) {
        if(uri == null || uri.equals("") || key == null || key.isEmpty()){
            throw new IllegalArgumentException("something is null or blank");
        }
        Document document = documents.get(uri);
        if(document == null){
            throw new IllegalArgumentException("No document stored in the URI");
        }
        String oldValue = document.getMetadataValue(key);
        Consumer<URI> undoAction = u -> {
            Document doc = documents.get(u);
            if (doc != null) {
                doc.setMetadataValue(key, oldValue);
            }
        };
        GenericCommand<URI> command = new GenericCommand<>(uri, undoAction);
        commandStack.push(command);
        return document.setMetadataValue(key,value);
    }

    @Override
    public String getMetadata(URI uri, String key) {
        if(uri == null || Objects.equals(uri.toString(), "") || key == null || key == "" ){
            throw new IllegalArgumentException("something is null or blank");
        }
        Document doc = documents.get(uri);
        if(doc == null){
            throw new IllegalArgumentException("No document stored in the URI");
        }
        return doc.getMetadataValue(key);
    }

    @Override
    public int put(InputStream input, URI url, DocumentFormat format) throws IOException {
        if(url == null || format == null || url.toString() == ""){
            throw new IllegalArgumentException("null or empty uri or format");
        }
        try{
            Document oldDocument = documents.get(url);
            byte[] data = (input != null) ? input.readAllBytes() : null;
            Document newDocument = (format == DocumentFormat.TXT) ?
                    new DocumentImpl(url, new String(data)) :
                    new DocumentImpl(url, data);

            // Copy metadata from old document to new document
            if (oldDocument != null) {
                HashTable<String, String> oldMetadata = oldDocument.getMetadata();
                for (String key : oldMetadata.keySet()) {
                    newDocument.setMetadataValue(key, oldMetadata.get(key));
                }
            }
            if(format == DocumentFormat.TXT){
                //String text = newDocument.getDocumentTxt();
                addToTrie(this.trie, newDocument.getDocumentTxt(), newDocument);
            }
            // Define the undo action to undo the put operation
            Consumer<URI> undoAction = u -> {
                if (oldDocument != null) {
                    this.documents.put(u, oldDocument);
                    this.removeFromTrie(this.trie,newDocument.getDocumentTxt(), newDocument);
                } else {
                    this.documents.put(u, null);
                    this.removeFromTrie(this.trie,newDocument.getDocumentTxt(), newDocument);
                }
            };
            documents.put(url, newDocument);// Update the document in the store
            GenericCommand<URI> command = new GenericCommand<>(url, undoAction);
            commandStack.push(command);
            if(oldDocument != null){
                if(format == DocumentFormat.TXT){
                    return oldDocument.hashCode();
                } else //(format == DocumentFormat.BINARY)
                {
                    return oldDocument.hashCode();
                }
            } else {
                return 0;
            }
        } catch (IOException e){
            throw new IOException("problem with reading the file" , e);
        }
    }

    private void addToTrie(TrieImpl trie, String text, Document doc){

        String[] words = text.split("\\s+");
        for(String word : words){
            if(!word.isEmpty()){
                //if(this.trie.get(word).contains(doc)){
                //    this.wordCountMap.put(word, wordCountMap.get(word) + 1);
                //}
                //else{
                    //wordCountMap.put(word,wordCountMap.get(word) + 1);
                    this.trie.put(word , doc);
                //}
            }
        }
    }
    private void removeFromTrie(TrieImpl trie, String text, Document doc) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                //if (this.trie.get(word).contains(doc)) {
                  //  this.wordCountMap.put(word, wordCountMap.get(word) - 1);
                   // if (wordCountMap.get(word) == 0) {
                        this.trie.delete(word, doc);
                    //}
                //}
            }
        }
    }
    @Override
    public Document get(URI url) {
        if(url == null || url.toString().isEmpty()){
            throw new IllegalArgumentException("null URI");
        }
        return documents.get(url);
    }

    @Override
    public boolean delete(URI url) {
        if(url== null || url.toString() == ""){
            throw new IllegalArgumentException("null URI");
        }
        Document oldDocument = documents.get(url);
        if(oldDocument == null){
            return false;
        }

        String text = oldDocument.getDocumentTxt();
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                this.trie.delete(word, oldDocument);
            }
        }

        Consumer<URI> undoAction = u -> {
            if (oldDocument != null) {
                documents.put(u, oldDocument);

                this.addToTrie(this.trie, oldDocument.getDocumentTxt(), oldDocument);
            } else {
                documents.put(u, null);
                this.addToTrie(this.trie, oldDocument.getDocumentTxt(),oldDocument);
            }
        };

        // Create the command and push it onto the stack
        GenericCommand<URI> command = new GenericCommand<>(url, undoAction);
        commandStack.push(command);
        // Remove the document from the store
        return (oldDocument != null) && (documents.put(url, null) != null);
    }

    private void addMetadata(Document doc, Map<String, String> metadata) {
        if (metadata != null && !metadata.isEmpty()) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                doc.setMetadataValue(entry.getKey(), entry.getValue());
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

    }
  /*  @Override
    public void undo(URI url) throws IllegalStateException {
        if(commandStack.size() ==0){
            throw new IllegalStateException("Empty stack");
        }
        Undoable command = this.commandStack.pop();
        command.undo();
    }*/

 /*   @Override
    public void undo(URI url){
        StackImpl<Command> tempStack = new StackImpl<>();
        Command commandToUndo = null;
        while(commandStack.size() != 0){
            Undoable command = commandStack.pop();
            if (command.getUri().equals(url)) {
                commandToUndo = command;
                break;
            } else {
                tempStack.push(command);
            }
        }
        while (tempStack.size()!= 0) {
            commandStack.push(tempStack.pop());
        }
        if(commandToUndo == null){
            throw new IllegalStateException("This URI does not exist in the command stack");
        }
        commandToUndo.undo();
    }*/

    @Override
    public void undo(URI url) throws IllegalStateException{
        StackImpl<Undoable> tempStack = new StackImpl<>();
        boolean found = false;
        while (!(commandStack.size() == 0)) {
            Undoable command = commandStack.pop();
            if (command instanceof GenericCommand && ((GenericCommand<?>) command).getTarget().equals(url)) {
                command.undo();
                found = true;
                break;
            } else if (command instanceof CommandSet && ((CommandSet) command).containsTarget(url)) {
                ((CommandSet) command).undo(url);
                if (!((CommandSet) command).isEmpty()) {
                    tempStack.push(command);
                }
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
        List<Document> toReturn = new ArrayList<>();
        toReturn = this.trie.getSorted(keyword, Comparator.comparingInt(doc -> -doc.wordCount(keyword)));
        Collections.reverse(toReturn);
        return toReturn;
        /*for(Document doc : documents.values()){
            if(doc != null && doc.getDocumentTxt() != null && doc.getDocumentTxt().contains(keyword)){
                toReturn.add(doc);
            }
        }
        toReturn.sort(Comparator.comparingInt(doc -> -doc.wordCount(keyword)));
        return toReturn;*/
    }

    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        if(keywordPrefix == null){
            return new ArrayList<>();
        }
        Comparator<Document> comp = Comparator.comparingInt(doc -> doc.wordCount(keywordPrefix));
        List<Document> toReturn = this.trie.getAllWithPrefixSorted(keywordPrefix, comp.reversed());
        Collections.reverse(toReturn);
        return toReturn;
    }

    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<URI> deletedDocuments = new HashSet<>();
        Set<Document> documentsToDelete = new HashSet<>(this.trie.get(keyword));
        if (keyword == null || keyword.isEmpty()) {
            return deletedDocuments;
        }
        CommandSet<URI> commandSet = new CommandSet<>();
        /*Consumer<URI> undoActions = u ->{
            for(Document doc : documentsToDelete){
                this.trie.put(doc.getDocumentTxt(), doc);
                command.addCommand();
            }
        };*/

        if (documentsToDelete == null || documentsToDelete.isEmpty()) {
            return deletedDocuments;
        }
        for(Document doc : documentsToDelete){
            URI uriToDelete = doc.getKey();
            deletedDocuments.add(uriToDelete);
            String text = doc.getDocumentTxt();
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    this.trie.delete(word, doc);
                }
            }
            Document originalDocument = this.documents.get(uriToDelete);
            this.documents.put(doc.getKey(), null);
            GenericCommand<URI> command = new GenericCommand<>(uriToDelete, u -> {
                this.trie.put(doc.getDocumentTxt(), doc);
                if (originalDocument != null) {
                    this.documents.put(doc.getKey(), originalDocument); // Restore the document with metadata
                }
            });
            commandSet.addCommand(command);
        }
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keywordPrefix == null || keywordPrefix.isEmpty()) {
            return deletedDocuments;
        }
        CommandSet<URI> commandSet = new CommandSet<>();
        List<Document> documentsToDelete = new ArrayList<>();
       documentsToDelete = this.trie.getAllWithPrefixSorted(keywordPrefix, Comparator.comparingInt(doc -> -doc.wordCount(keywordPrefix)));
        for (Document document : documentsToDelete) {
            String text = document.getDocumentTxt();
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    this.trie.delete(word, document);
                    deletedDocuments.add(document.getKey());
                }
            }
            Document originalDocument = this.documents.get(document.getKey()); // Get the original document
            this.documents.put(document.getKey(), null);
            GenericCommand<URI> command = new GenericCommand<>(document.getKey(), u -> {
                this.trie.put(document.getDocumentTxt(), document);
                if (originalDocument != null) {
                    this.documents.put(document.getKey(), originalDocument); // Restore the document with metadata
                }
            });
            commandSet.addCommand(command);
        }
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
            if(document!= null) {
                if (containsMetadata(document, keysValues)) {
                    toReturn.add(document);
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
            return deletedDocuments;
        }
        CommandSet<URI> commandSet = new CommandSet<>();
        Map<URI, Map<String, String>> deletedDocsMetadata = new HashMap<>();
        for(URI uri : this.documents.keySet()){
            Document doc = documents.get(uri);
            if(containsMetadata(doc, keysValues)){

                String text = doc.getDocumentTxt();
                String[] words = text.split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                        deletedDocuments.add(uri);
                    }
                }
                Document originalDocument = this.documents.get(doc.getKey()); // Get the original document
                if (originalDocument != null) {
                    this.documents.put(doc.getKey(), originalDocument); // Restore the document with metadata
                }
                //this.documents.put(uri,null);
                //this.documents.get(doc.getKey()).setMetadataValue();

                //this.documents.put(uri, null);
                GenericCommand<URI> command = new GenericCommand<>(uri, u -> {
                    this.trie.put(doc.getDocumentTxt(), doc);
                    if (originalDocument != null) {
                        this.documents.put(doc.getKey(), originalDocument); // Restore the document with metadata
                    }
                    addToTrie(this.trie,  doc.getDocumentTxt(), doc);
                });
                commandSet.addCommand(command);
                documents.put(doc.getKey(), null);
                deletedDocuments.add(uri);
            }
        }
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }



    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        Set<URI> deletedDocuments = new HashSet<>();
        if (keyword == null || keyword.isEmpty() || keysValues == null || keysValues.isEmpty()) {
            return deletedDocuments;
        }
        CommandSet<URI> commandSet = new CommandSet<>();
        List<Document> docs = trie.getSorted(keyword, Comparator.comparingInt(doc -> -doc.wordCount(keyword)));
        for (Document doc : docs) {
            if (containsMetadata(doc, keysValues)) {
                String text = doc.getDocumentTxt();
                String[] words = text.split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                    }
                }
                Document originalDocument = this.documents.get(doc.getKey()); // Get the original document
                GenericCommand<URI> command = new GenericCommand<>(doc.getKey(), u -> {
                    this.trie.put(doc.getDocumentTxt(), doc);
                    if (originalDocument != null) {
                        this.documents.put(doc.getKey(), originalDocument); // Restore the document with metadata
                    }
                    addToTrie(this.trie,  doc.getDocumentTxt(), doc);
                });
                commandSet.addCommand(command);
                documents.put(doc.getKey(), null);
                deletedDocuments.add(doc.getKey());
            }
        }
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
        // Get the documents associated with the prefix from the trie
        List<Document> docs = trie.getAllWithPrefixSorted(keywordPrefix, Comparator.comparingInt(doc -> -doc.wordCount(keywordPrefix)));
        for (Document doc : docs) {
            if (containsMetadata(doc, keysValues)) {
                String text = doc.getDocumentTxt();
                String[] words = text.split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        this.trie.delete(word, doc);
                        deletedDocuments.add(doc.getKey());
                    }
                }
                Document originalDocument = this.documents.get(doc.getKey());
                GenericCommand<URI> command = new GenericCommand<>(doc.getKey(), u -> {
                    this.trie.put(doc.getDocumentTxt(), doc);
                    if (originalDocument != null) {
                        this.documents.put(doc.getKey(), originalDocument); // Restore the document with metadata
                    }
                    addToTrie(this.trie,  doc.getDocumentTxt(), doc);
                });
                commandSet.addCommand(command);
                // Remove the document from the documents map
                documents.put(doc.getKey(), null);
                // Add the URI to the set of deleted documents
                deletedDocuments.add(doc.getKey());
            }
        }
        this.commandStack.push(commandSet);
        return deletedDocuments;
    }
}
