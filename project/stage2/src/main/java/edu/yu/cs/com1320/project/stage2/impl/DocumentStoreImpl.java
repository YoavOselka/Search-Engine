package edu.yu.cs.com1320.project.stage2.impl;

import java.io.IOException;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage2.*;

import java.io.InputStream;
import java.net.URI;

import java.util.HashMap;
import java.util.Objects;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, Document> documents;

    public DocumentStoreImpl(){
        this.documents = new HashTableImpl<>();
    }

    public String setMetadata(URI uri, String key, String value){
        if(uri == null || uri.equals("") || key == null || key.isEmpty()){
            throw new IllegalArgumentException("something is null or blank");
        }
        Document doc = documents.get(uri);
        if(doc == null){
            throw new IllegalArgumentException("No document stored in the URI");
        }
		/*String valueToReturn = doc.setMetadataValue(key, value);
		documents.put(uri, doc);
		return valueToReturn;*/
        return doc.setMetadataValue(key, value);
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
            Document oldDocument = documents.get(url);
            if (oldDocument != null) {
                int oldHashCode = oldDocument.hashCode();
                if (input == null) {
                    documents.put(url, null);
                    return oldHashCode;
                }
            }
            byte[] data = input.readAllBytes();
            Document document = format == DocumentFormat.TXT ? new DocumentImpl(url, new String(data)) :
                    new DocumentImpl(url, data);
            documents.put(url, document);
            return oldDocument != null ? oldDocument.hashCode() : 0;
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
        return documents.put(url, null) != null;
    }
}