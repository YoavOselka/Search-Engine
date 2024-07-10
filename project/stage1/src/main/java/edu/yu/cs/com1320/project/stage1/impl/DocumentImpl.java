package edu.yu.cs.com1320.project.stage1.impl;
import edu.yu.cs.com1320.project.stage1.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Arrays;


public class DocumentImpl implements Document {
    private URI uri;
    private String txt;
    private byte[] binaryData;
    private HashMap<String, String> metadata;
    private DocumentStore.DocumentFormat format;

	/*public DocumentImpl(URI uri, String txt, byte[] binaryData){
		this.uri = uri;
		this.txt = txt;
		this.binaryData = binaryData;
		this.metadata = new HashMap<>();
	}*/

    public DocumentImpl(URI uri, String txt){
        this.uri = uri;
        this.txt = txt;
        this.binaryData = null;
        this.metadata = new HashMap<>();
        this.format = DocumentStore.DocumentFormat.TXT;
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        this.uri = uri;
        this.txt = "";
        this.binaryData = binaryData;
        this.metadata = new HashMap<>();
        this.format = DocumentStore.DocumentFormat.BINARY;
    }

    public String setMetadataValue(String key ,String value){
        if(key == null || key.isEmpty() || key.equals(" ")){
            throw new IllegalArgumentException("empty or null key");
        }
        return metadata.put(key, value);
    }

    public String getMetadataValue(String key){
        if(key == null || key.isEmpty() || key.equals(" ")){
            throw new IllegalArgumentException("null or empty key");
        }
        return metadata.get(key);
    }

    public HashMap<String, String> getMetadata() {
        return new HashMap<>(metadata);
    }

    public String getDocumentTxt() {
        if(format == DocumentStore.DocumentFormat.TXT){
            return txt;
        }
        else{
            return null;
        }
    }

    public byte[] getDocumentBinaryData(){
        if(format == DocumentStore.DocumentFormat.BINARY){
            return binaryData.clone();
        }
        else{
            return null;
        }
    }

    public URI getKey(){
        return uri;
    }

    /*
        public URI getKey(){
            if (DocumentStore.DocumentFormat == DocumentStore.DocumentFormat.TXT) {
                return uri;
            }
            else {
                   return null;
            }
        }

        public byte[] getDocumentBinaryData(){
            if (DocumentStore.DocumentFormat == DocumentStore.DocumentFormat.BINARY) {
                return binaryData.clone();
            }
            else {
                   return null;
            }
        }
    */
    public int hashCode(){
        int result = uri.hashCode();
        result = 31 * result + (txt!=null ? txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    public boolean equals(Document doc){
        if(this.hashCode() == doc.hashCode()){
            return true;
        }
        return false;
    }
}