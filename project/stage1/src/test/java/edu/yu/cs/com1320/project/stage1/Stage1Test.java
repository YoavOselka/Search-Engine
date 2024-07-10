package edu.yu.cs.com1320.project.stage1;

import edu.yu.cs.com1320.project.stage1.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage1.impl.DocumentStoreImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
public class Stage1Test {

    URI uri = URI.create("test://document");
    private String text = "This is a test document.";
    DocumentImpl doc = new DocumentImpl(uri,text);
    @Test
    public void testSetMetadataValue(){
        doc.setMetadataValue("key1", "value1");
        Assertions.assertEquals("value1", doc.getMetadataValue("key1"));
    }
    @Test
    public void testGetDocumentTxt() {
        DocumentImpl document = new DocumentImpl(URI.create("test://document"), "This is a test document.");
        Assertions.assertEquals("This is a test document.", document.getDocumentTxt());
    }
    @Test
    public void testGetKey() {
        URI uri = URI.create("test://document");
        DocumentImpl document = new DocumentImpl(uri, "This is a test document.");
        Assertions.assertEquals(uri, document.getKey());
    }
    @Test
    public void testEquals() {
        URI uri = URI.create("test://document");
        DocumentImpl document1 = new DocumentImpl(uri, "This is a test document.");
        DocumentImpl document2 = new DocumentImpl(uri, "This is another test document.");
        Assertions.assertTrue(document1.equals(document1));
        Assertions.assertFalse(document1.equals(document2));
    }
    @Test
    public void testGetDocumentBinaryData() {
        byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
        DocumentImpl document = new DocumentImpl(URI.create("test://document"), data);
        Assertions.assertArrayEquals(data, document.getDocumentBinaryData());
    }


    //DocumentStoreImpl store = new DocumentStoreImpl();
    @Test
    public void testSetMetadata() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        URI uri = URI.create("test://document");
        String txt = "This is a test document.";
        InputStream inputStream = new ByteArrayInputStream(txt.getBytes());
        documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);
        String oldValue = documentStore.setMetadata(uri, "key1", "value1");

        Assertions.assertNull(oldValue);
        String metadataValue = documentStore.getMetadata(uri, "key1");
        Assertions.assertEquals("value1", metadataValue);
    }
    @Test
    public void testPutAndGet() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        URI uri = URI.create("test://document");
        String txt1 = "This is the first test document.";
        String txt2 = "This is the second test document.";
        InputStream inputStream1 = new ByteArrayInputStream(txt1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(txt2.getBytes());

        int hashCode1 = documentStore.put(inputStream1, uri, DocumentStore.DocumentFormat.TXT);
        int hashCode2 = documentStore.put(inputStream2, uri, DocumentStore.DocumentFormat.TXT);

        // Check that the hash code of the previous document is returned
        Assertions.assertEquals(0, hashCode1);

        // Check that the first document was replaced by the second document
        Document document = documentStore.get(uri);
        Assertions.assertNotNull(document);
        Assertions.assertEquals(txt2, document.getDocumentTxt());
    }
    @Test
    public void testDelete() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        URI uri = URI.create("test://document");
        String txt = "This is a test document.";
        InputStream inputStream = new ByteArrayInputStream(txt.getBytes());

        documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);
        Assertions.assertTrue(documentStore.delete(uri));
        Assertions.assertNull(documentStore.get(uri));
        Assertions.assertFalse(documentStore.delete(uri));
    }
    @Test
    public void testSetAndGetMetadata() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        URI uri = URI.create("test://document");
        String txt = "This is a test document.";
        InputStream inputStream = new ByteArrayInputStream(txt.getBytes());
        documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);

        // Set metadata
        String oldValue = documentStore.setMetadata(uri, "key1", "value1");
        Assertions.assertNull(oldValue);

        // Get metadata
        String metadataValue = documentStore.getMetadata(uri, "key1");
        Assertions.assertEquals("value1", metadataValue);
    }
}
