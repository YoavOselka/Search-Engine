package edu.yu.cs.com1320.project.stage3.impl;

import java.io.IOException;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage3.*;
import edu.yu.cs.com1320.project.undo.Command;

import java.io.InputStream;
import java.net.URI;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, Document> documents;
    private StackImpl<Command> commandStack;
    public DocumentStoreImpl(){
        this.documents = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
    }
    //delete this method before submission!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    /*public StackImpl<Command> getCommandStack() {
        return commandStack;
    }*/
    public String setMetadata(URI uri, String key, String value){
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
        Command command = new Command(uri, undoAction);
        commandStack.push(command);
        return document.setMetadataValue(key,value);

        /*Command command = new Command(uri, u -> doc.setMetadataValue(key, oldValue));
        commandStack.push(command);
        return oldValue;

		String valueToReturn = doc.setMetadataValue(key, value);
		documents.put(uri, doc);
		return valueToReturn;
        return doc.setMetadataValue(key, value);*/
    }

    public String getMetadata(URI uri, String key){
        if(uri == null || Objects.equals(uri.toString(), "") || key == null || key == "" ){
            throw new IllegalArgumentException("something is null or blank");
        }
        Document doc = documents.get(uri);
        if(doc == null){
            throw new IllegalArgumentException("No document stored in the URI");
        }
        return doc.getMetadataValue(key);
    }

    public int put(InputStream input, URI url, DocumentFormat format) throws IOException{
        if(url == null || format == null || url.toString() == ""){
            throw new IllegalArgumentException("null or empty uri or format");
        }
        try{
            /*Document oldDocument = documents.get(url);
            if (oldDocument != null) {
                int oldHashCode = oldDocument.hashCode();
                if (input == null) {
                    documents.put(url, null);
                    Command command = new Command(url, u -> documents.put(u, oldDocument));
                    commandStack.push(command);
                    return oldHashCode;
                }
            }
            byte[] data = input.readAllBytes();
            Document document = format == DocumentFormat.TXT ? new DocumentImpl(url, new String(data)) :
                    new DocumentImpl(url, data);
            documents.put(url, document);
            Command command = new Command(url, u -> documents.put(u, oldDocument));
            commandStack.push(command);
            return oldDocument != null ? oldDocument.hashCode() : 0;*/
            Document oldDocument = documents.get(url);
            byte[] data = (input != null) ? input.readAllBytes() : null;
            Document newDocument = (format == DocumentFormat.TXT) ?
                    new DocumentImpl(url, new String(data)) :
                    new DocumentImpl(url, data);

            /*int oldDocumentHashCode = 0;
            if (oldDocument != null && oldDocument.getDocumentTxt() == null) {
                oldDocumentHashCode = oldDocument.hashCode();
            }*/

            // Define the undo action to undo the put operation
            Consumer<URI> undoAction = u -> {
                if (oldDocument != null) {
                    documents.put(u, oldDocument);
                } else {
                    documents.put(u, null);
                }
            };

            // Create the command and push it onto the stack
            Command command = new Command(url, undoAction);
            commandStack.push(command);
            // Update the document in the store
            documents.put(url, newDocument);

            if(oldDocument != null){
                if(format == DocumentFormat.TXT){
                    return oldDocument.hashCode();
                }
                else //(format == DocumentFormat.BINARY)
                    {
                    return oldDocument.hashCode();
                }
            }
            else{
                return 0;
            }

            //return (oldDocument != null && oldDocument.getDocumentTxt() != null) ? oldDocument.hashCode() : 0;
        } catch (IOException e){
            throw new IOException("problem with reading the file" , e);
        }
    }

    public Document get(URI url){
        if(url == null || url.toString().isEmpty()){
            throw new IllegalArgumentException("null URI");
        }
        return documents.get(url);
    }

    public boolean delete(URI url){
        if(url== null || url.toString() == ""){
            throw new IllegalArgumentException("null URI");
        }
        Document oldDocument = documents.get(url);
        /*documents.put(url, null);
        Command command = new Command(url, u -> documents.put(u, oldDocument));
        commandStack.push(command);
        return oldDocument != null;*/
        Consumer<URI> undoAction = u -> {
            if (oldDocument != null) {
                documents.put(u, oldDocument);
            } else {
                documents.put(u, null);
            }
        };

        // Create the command and push it onto the stack
        Command command = new Command(url, undoAction);
        commandStack.push(command);

        // Remove the document from the store
        return (oldDocument != null) && (documents.put(url, null) != null);
    }

    public void undo(){
        if(commandStack.size() ==0){
            throw new IllegalStateException("Empty stack");
        }
        Command command = commandStack.pop();
        command.undo();
    }

    public void undo(URI url){
        StackImpl<Command> tempStack = new StackImpl<>();
        Command commandToUndo = null;
        while(commandStack.size() != 0){
            Command command = commandStack.pop();
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
    }
}