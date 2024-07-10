package edu.yu.cs.com1320.project.stage5.impl;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage5.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DocumentImpl implements Document{
    private URI uri;
    private String txt;
    private byte[] binaryData;
    private HashTableImpl<String, String> metadata;
    private DocumentStore.DocumentFormat format;
    private long LastUsedNanoTime;

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
        this.metadata = new HashTableImpl<>();
        this.format = DocumentStore.DocumentFormat.TXT;
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        this.uri = uri;
        this.txt = null;
        this.binaryData = binaryData;
        this.metadata = new HashTableImpl<>();
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
    public HashTable<String, String> getMetadata() {
        HashTable<String, String> copy = new HashTableImpl<>();
        for (String key : metadata.keySet()) {
            copy.put(key, metadata.get(key));
        }
        return copy;
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

    public int wordCount(String word){
        if (this.binaryData != null) {
            return 0;
        }
        if (word == null || word.trim().isEmpty()) {
            return 0;
        }

        int count = 0;
        if(this.txt != null){
            String[] words = txt.split("\\W+");
            for (String w : words) {
                if (w.equals(word)) {
                    count++;
                }
            }
        }
        return count;
    }

    public Set<String> getWords(){
        if(this.binaryData != null){
            return new HashSet<>();
        }
        Set<String> words = new HashSet<>();
        if(this.txt != null){
            //String fixedTxt = this.txt.replaceAll("[^a-zA-Z0-9\\s]", "");
            String[] txtWords = txt.split("\\W+");
            words.addAll(Arrays.asList(txtWords));
        }
        return words;
    }

    public long getLastUseTime(){
        return this.LastUsedNanoTime;
    }

    public void setLastUseTime(long timeInNanoseconds){
        this.LastUsedNanoTime = timeInNanoseconds;
    }

    public int compareTo(Document other){
        return Long.compare(this.LastUsedNanoTime, other.getLastUseTime());
    }
}
