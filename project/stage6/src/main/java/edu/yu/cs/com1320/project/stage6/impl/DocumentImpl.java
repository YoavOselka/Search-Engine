package edu.yu.cs.com1320.project.stage6.impl;

import edu.yu.cs.com1320.project.stage6.*;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.impl.DocumentStoreImpl;
import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document{
    private URI uri;
    private String txt;
    private byte[] binaryData;
    private HashMap<String, String> metadata;
    private HashMap<String, Integer> words;
    private DocumentStore.DocumentFormat format;
    private long LastUsedNanoTime;

    public DocumentImpl(URI uri, String txt, Map<String, Integer> wordCountMap)
    {
        this.words = new HashMap<>();
        if(uri==null|| uri.toString().isBlank()) {
            throw new IllegalArgumentException("uri cannot be null or blank");
        }
        else if(txt==null || txt.isEmpty())
        {
            throw new IllegalArgumentException("text cannot be empty or null");
        }

        if(wordCountMap==null)
        {
            String[] textArr = txt.split(" ");
            for(String wrd: textArr)
            {
                if(this.words.get(wrd)==null)
                {
                    this.words.put(wrd, 1);
                }
                else
                {
                    this.words.put(wrd, this.words.get(wrd)+1);
                }
            }
        }
        else {
            this.words=new HashMap<>(wordCountMap);
        }
        this.metadata = new HashMap<>();
        this.format = DocumentStore.DocumentFormat.TXT;

        this.uri = uri;
        this.txt=txt;
    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if(uri.toString().isBlank() || uri==null)
        {
            throw new IllegalArgumentException("uri cannot be null or blank");
        }
        else if(binaryData.length ==0 || binaryData==null)
        {
            throw new IllegalArgumentException("binaryData cannot be empty or null");
        }
        this.uri = uri;
        this.txt = null;
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
    private String stripPunctuation(String str)
    {
        String newStr = "";
        char[] keyArr = str.toCharArray();
        for(int i =0; i<keyArr.length;i++)
        {
            if(Character.isDigit(keyArr[i]) || Character.isLetter(keyArr[i]))
            {
                newStr+=keyArr[i];
            }
        }
        return newStr;
    }
    public String getMetadataValue(String key){
        if(key == null || key.isEmpty() || key.equals(" ")){
            throw new IllegalArgumentException("null or empty key");
        }
        return metadata.get(key);
    }
    public HashMap<String, String> getMetadata() {
        HashMap<String, String> copy = new HashMap<>();
        for (String key : metadata.keySet()) {
            copy.put(key, this.metadata.get(key));
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
    @Override
    public void setMetadata(HashMap<String, String> metadata)
    {
        this.metadata = metadata;

    }
    public URI getKey(){
        return uri;
    }

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
    @Override
    public HashMap<String, Integer> getWordMap() {
        return words;
    }
    @Override
    public void setWordMap(HashMap<String, Integer> wordMap) {
        this.words=wordMap;
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
