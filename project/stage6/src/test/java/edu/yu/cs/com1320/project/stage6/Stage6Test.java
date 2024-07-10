package edu.yu.cs.com1320.project.stage6;

import edu.yu.cs.com1320.project.stage6.impl.DocumentStoreImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

class DocumentStoreImplTest {
    @Test
    public void test1() throws IOException {
        InputStream input1 = new ByteArrayInputStream("text1 yes".getBytes());
        InputStream input2 = new ByteArrayInputStream("text2".getBytes());
        InputStream input3 = new ByteArrayInputStream("text3 yes".getBytes());
        InputStream input4 = new ByteArrayInputStream("text4".getBytes());
        InputStream input5 = new ByteArrayInputStream("text5".getBytes());
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");
        URI uri1 = URI.create("http://Users/doc1");
        URI uri2 = URI.create("http://Users/doc2");
        URI uri3 = URI.create("http://Users/doc3");
        URI uri4 = URI.create("http://Users/doc4");
        URI uri5 = URI.create("http://Users/doc5");
        DocumentStoreImpl store = new DocumentStoreImpl();

        store.put(input1,uri1, DocumentStore.DocumentFormat.TXT);
        store.put(input2,uri2, DocumentStore.DocumentFormat.TXT);
        store.put(input3,uri3, DocumentStore.DocumentFormat.TXT);
        store.put(input4,uri4, DocumentStore.DocumentFormat.TXT);
        store.put(input5,uri5, DocumentStore.DocumentFormat.TXT);
        Document doc1 = store.get(uri1);
        Document doc2 = store.get(uri2);
        store.setMetadata(uri1, "key", "value");
        store.setMetadata(uri2, "key", "value");

        Set<URI> deleted = store.deleteAll("yes");
        store.undo();
        //store.delete(uri1);
        //store.delete(uri2);
        //store.delete(uri3);
        //store.delete(uri4);
        List<Document> search = new ArrayList<>();
        //search = store.searchByPrefix("ye");
        //Assertions.assertEquals(1 , deleted.size());
    }


}