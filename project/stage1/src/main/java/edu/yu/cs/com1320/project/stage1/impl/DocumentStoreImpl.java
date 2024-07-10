package edu.yu.cs.com1320.project.stage1.impl;

import java.io.IOException;
import edu.yu.cs.com1320.project.stage1.*;

import java.io.InputStream;
import java.net.URI;

import java.util.HashMap;
import java.util.Objects;

public class DocumentStoreImpl implements DocumentStore {
    private HashMap<URI, Document> documents;

    public DocumentStoreImpl(){
        this.documents = new HashMap<>();
    }

    public String setMetadata(URI uri, String key, String value){
        if(uri == null || uri.equals(null) || key == null || key.isEmpty()){
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

    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException{
        if(uri == null || format == null || uri.toString() == ""){
            throw new IllegalArgumentException("null or empty uri or format");
        }
        try{
            if(input == null){
                if(documents.containsKey(uri)){
                    documents.remove(uri);
                    return 0;
                }
            }

            Document doc = null;
            if(format == DocumentFormat.TXT){
                String txt = new String(input.readAllBytes());
                doc = new DocumentImpl(uri, txt);
            }
            else if(format == DocumentFormat.BINARY){
                byte[] binaryData = input.readAllBytes();
                doc = new DocumentImpl(uri, binaryData);
            }
            Document docToReturn = documents.put(uri,doc);
            if(docToReturn == null){
                return 0;
            }
            else{
                return docToReturn.hashCode();
            }
        } catch (IOException e){
            throw new IOException("problem with reading the file" , e);
        }
    }

    public Document get(URI uri){
        if(uri == null || uri.toString() == ""){
            throw new IllegalArgumentException("null URI");
        }
        return documents.get(uri);
    }

    public boolean delete(URI uri){
        if(uri == null || uri.toString() == ""){
            throw new IllegalArgumentException("null URI");
        }
        Document whichBoolToReturn = documents.remove(uri);
        if(whichBoolToReturn == null ){
            return false;
        }
        return true;
    }
}