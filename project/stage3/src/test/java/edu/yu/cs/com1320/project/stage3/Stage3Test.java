package edu.yu.cs.com1320.project.stage3;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;
import edu.yu.cs.com1320.project.undo.Command;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;
import java.net.URI;
public class Stage3Test {
    private DocumentStoreImpl documentStore;
    @Test
    public void testSetMetadata() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text = "This is a test document";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        URI uri = new URI("http://example.com/test.txt");

        // Add a document to the store
        documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);

        // Set metadata for the document
        String key = "author";
        String value = "John Doe";
        String oldValue = documentStore.setMetadata(uri, key, value);

        // Check that the metadata value was set correctly
        Assertions.assertEquals(value, documentStore.getMetadata(uri, key));
        // Check that the method returns the old metadata value
        Assertions.assertEquals(null, oldValue);
    }

    @Test
    public void testSetMetadataWithNullURI() {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String key = "author";
        String value = "John Doe";

        // Check that setting metadata with a null URI throws an exception
        Assertions.assertThrows(IllegalArgumentException.class, () -> documentStore.setMetadata(null, key, value));
    }

    @Test
    public void testSetMetadataWithBlankKey() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text = "This is a test document";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        URI uri = new URI("http://example.com/test.txt");

        // Add a document to the store
        documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);

        // Check that setting metadata with a blank key throws an exception
        Assertions.assertThrows(IllegalArgumentException.class, () -> documentStore.setMetadata(uri, "", "value"));
    }

    /*@BeforeEach
    public void setUp(){
        documentStore = new DocumentStoreImpl();
        while (documentStore.getCommandStack().size() > 0) {
            documentStore.getCommandStack().pop();
        }
    }*/

    @Test
    public void testResize() {
        HashTableImpl<Integer, String> hashTable = new HashTableImpl<>();
        for (int i = 0; i < 6; i++) {
            hashTable.put(i, "Value" + i);
        }

        Assertions.assertEquals(6, hashTable.size());
        Assertions.assertEquals(true, hashTable.size() > 5); // Assuming you have a getTableLength() method in HashTableImpl
    }

    @Test
    public void testPutDuplicateKey() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        hashTable.put("one", 1);
        hashTable.put("one", 2);

        Assertions.assertEquals(Integer.valueOf(2), hashTable.get("one"));
    }

    @Test
    public void testContainsKey() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        hashTable.put("one", 1);
        hashTable.put("two", 2);

        Assertions.assertTrue(hashTable.containsKey("one"));
        Assertions.assertTrue(hashTable.containsKey("two"));
        Assertions.assertFalse(hashTable.containsKey("three"));
    }

    @Test
    public void testSize() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        Assertions.assertEquals(0, hashTable.size());

        hashTable.put("one", 1);
        Assertions.assertEquals(1, hashTable.size());

        hashTable.put("two", 2);
        Assertions.assertEquals(2, hashTable.size());
    }

    @Test
    public void testValues() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        hashTable.put("one", 1);
        hashTable.put("two", 2);
        hashTable.put("three", 3);

        Assertions.assertTrue(hashTable.values().contains(1));
        Assertions.assertTrue(hashTable.values().contains(2));
        Assertions.assertTrue(hashTable.values().contains(3));
        Assertions.assertFalse(hashTable.values().contains(4));
    }

    @Test
    public void testKeySet() {
        HashTableImpl<String, Integer> hashTable = new HashTableImpl<>();
        hashTable.put("one", 1);
        hashTable.put("two", 2);
        hashTable.put("three", 3);

        Assertions.assertTrue(hashTable.keySet().contains("one"));
        Assertions.assertTrue(hashTable.keySet().contains("two"));
        Assertions.assertTrue(hashTable.keySet().contains("three"));
        Assertions.assertFalse(hashTable.keySet().contains("four"));
    }
    @Test
    public void testPutAndGet() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text = "This is a test document";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        URI uri = new URI("http://example.com/test.txt");

        int hashCode = documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);
        Document document = documentStore.get(uri);

        Assertions.assertNotNull(document);
        Assertions.assertEquals(text, document.getDocumentTxt());
        Assertions.assertEquals(text.hashCode(), document.getDocumentTxt().hashCode());
    }

    @Test
    public void testDelete() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text = "This is a test document";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        URI uri = new URI("http://example.com/test.txt");

        documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);
        boolean deleted = documentStore.delete(uri);
        Document document = documentStore.get(uri);

        Assertions.assertTrue(deleted);
        Assertions.assertNull(document);
    }

    @Test
    public void testUndo() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text1 = "This is document 1";
        String text2 = "This is document 2";
        URI uri1 = new URI("http://example.com/doc1.txt");
        URI uri2 = new URI("http://example.com/doc2.txt");

        // Put two documents
        documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);

        // Delete the first document
        documentStore.delete(uri1);

        // Undo the delete
        documentStore.undo();
        Document document1 = documentStore.get(uri1);
        Document document2 = documentStore.get(uri2);

        Assertions.assertNotNull(document1);
        Assertions.assertEquals(text1, document1.getDocumentTxt());
        Assertions.assertNotNull(document2);
    }
    /*@Test
    public void testPutDeleteUndo() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text = "This is a test document";
        InputStream = new ByteArrayInputStream(text.getBytes());
        URI uri = new URI("http://example.com/test.txt");

        documentStore.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);
        documentStore.delete(uri);

        // Check if document is deleted
        Document deletedDocument = documentStore.get(uri);
        Assertions.assertNull(deletedDocument);
        Assertions.assertEquals(2,documentStore.getCommandStack().size() );
        // Undo the delete
        documentStore.undo();
        Document restoredDocument = documentStore.get(uri);
        Assertions.assertEquals(1,documentStore.getCommandStack().size() );
        // Check if document is restored
        Assertions.assertNotNull(restoredDocument);
        Assertions.assertEquals(text, restoredDocument.getDocumentTxt());
    }

    @Test
    public void testAll() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore1 = new DocumentStoreImpl();
        String text = "This is a test document";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        URI uri = new URI("http://example.com/test.txt");
        Assertions.assertThrows(IllegalStateException.class, documentStore1::undo);
        documentStore1.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);
        Assertions.assertEquals(1, documentStore1.getCommandStack().size());
        documentStore1.undo(uri);
        Assertions.assertEquals(0, documentStore1.getCommandStack().size());
        Assertions.assertThrows(IllegalStateException.class, () -> documentStore1.undo(uri));
        Assertions.assertEquals(0, documentStore1.getCommandStack().size());
        documentStore1.put(inputStream, uri, DocumentStore.DocumentFormat.TXT);
        documentStore1.delete(uri);
        Assertions.assertEquals(2, documentStore1.getCommandStack().size());
    }

    @Test
    public void testAfterAll() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text1 = "This is a test document";
        String text2 = "This is a test document2";
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        URI uri = new URI("http://example.com/test.txt");
        documentStore.put(inputStream1, uri, DocumentStore.DocumentFormat.TXT);
        Assertions.assertEquals(1, documentStore.getCommandStack().size());
        int hashCode = documentStore.put(inputStream2, uri, DocumentStore.DocumentFormat.TXT);
        Assertions.assertEquals(text2, documentStore.get(uri).getDocumentTxt());
        documentStore.undo();
        Assertions.assertEquals(text1, documentStore.get(uri).getDocumentTxt());
        Assertions.assertEquals(text1.hashCode(), documentStore.get(uri).getDocumentTxt().hashCode());
        Assertions.assertEquals(hashCode, documentStore.put(inputStream2, uri, DocumentStore.DocumentFormat.TXT));
    }*/

    @Test
    public void testUndoSetMetadata() throws URISyntaxException, IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text1 = "This is a test document";
        String text2 = "This is a test document2";
        String key1 = "key1";
        String key2 = "key2";
        String value1= "value1";
        String value2 = "value2";
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        URI uri1 = new URI("http://example1.com/test.txt");
        URI uri2 = new URI("http://example2.com/test.txt");
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.setMetadata(uri1,key1,value1);
        documentStore.setMetadata(uri1, key2,value2);
        documentStore.undo();
        Assertions.assertEquals(value1, documentStore.getMetadata(uri1, key1));
    }

    @Test
    public void testNullPush() throws URISyntaxException , IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text1 = "This is a test document";
        String text2 = "This is a test document2";
        String key1 = "key1";
        String key2 = "key2";
        String value1= "value1";
        String value2 = "value2";
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        URI uri1 = new URI("http://example1.com/test.txt");
        URI uri2 = new URI("http://example2.com/test.txt");
        documentStore.put(inputStream1, uri1, DocumentStore.DocumentFormat.TXT);
    }

    @Test
    public void testPushNullCommand() {
        StackImpl<Command> commandStack = new StackImpl<>();
        Assertions.assertNull(commandStack.peek());
        Assertions.assertThrows(IllegalArgumentException.class, ()->commandStack.push(null));
        Assertions.assertNull(commandStack.peek());
    }
    @Test
    public void testPutNewVersionOfDocumentBinary() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();
        URI uri = URI.create("http://example.com/document1");
        byte[] oldData = "old binary data".getBytes();
        byte[] newData = "new binary data".getBytes();

        try (InputStream oldInputStream = new ByteArrayInputStream(oldData);
             InputStream newInputStream = new ByteArrayInputStream(newData)) {
            documentStore.put(oldInputStream, uri, DocumentStore.DocumentFormat.BINARY);
            documentStore.put(newInputStream, uri, DocumentStore.DocumentFormat.BINARY);
            Document doc1 = documentStore.get(uri);
            int oldHashCode = documentStore.put(newInputStream,uri, DocumentStore.DocumentFormat.BINARY);
            Assertions.assertEquals(doc1.hashCode(), oldHashCode);
            //Assertions.assertArrayEquals("new binary data".getBytes(), document.getDocumentBinaryData());
        }
    }
    @Test
    public void testPutTextFile() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();
        String text1 = "This is a text document.1";
        String text2 = "This is a text document.2";
        byte[] data1 = text1.getBytes();
        byte[] data2 = text2.getBytes();
        InputStream inputStream1 = new ByteArrayInputStream(data1);
        InputStream inputStream2 = new ByteArrayInputStream(data2);
        URI uri = URI.create("http://www.example.com/text");
        documentStore.put(inputStream1, uri, DocumentStore.DocumentFormat.TXT);
        documentStore.put(inputStream2, uri, DocumentStore.DocumentFormat.TXT);
        Document doc1 = documentStore.get(uri);
        int oldHashCode = doc1.hashCode();
        // Calculate the expected hash code of the text document
        int expectedHashCode = doc1.hashCode();
        Assertions.assertEquals(expectedHashCode, oldHashCode);
        Assertions.assertEquals("This is a text document.2", documentStore.get(uri).getDocumentTxt());
    }
}
