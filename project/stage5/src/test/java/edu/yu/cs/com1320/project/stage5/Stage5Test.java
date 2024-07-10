package edu.yu.cs.com1320.project.stage5;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import javax.print.Doc;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.*;


public class Stage5Test {
    @Test
    public void testStage5General(){
        MinHeapImpl<Integer> heap = new MinHeapImpl<>();
        heap.insert(5);
        heap.insert(3);
        heap.insert(8);

        Assertions.assertEquals(3, heap.remove().intValue()); // Smallest element
        Assertions.assertEquals(5, heap.remove().intValue());
        Assertions.assertEquals(8, heap.remove().intValue());
        Assertions.assertNull(heap.peek());
    }

    @Test
    public void testHeapProperty() {
        MinHeapImpl<Integer> heap = new MinHeapImpl<>();
        Random rand = new Random();
        int[] numbers = new int[20];

        // Insert random numbers and keep track of them
        for (int i = 0; i < numbers.length; i++) {
            int number = rand.nextInt(100);
            heap.insert(number);
            numbers[i] = number;
        }
        // Sort the array to compare with heap removal order
        Arrays.sort(numbers);
        // Check if removal order is the same as sorted order
        for (int number : numbers) {
            Assertions.assertEquals(number, heap.remove().intValue());
        }
        Assertions.assertNull(heap.peek());
    }

    @Test
    public void testDocumentCountLimit() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setMaxDocumentCount(2);

        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");

        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

        Assertions.assertNull(store.get(uri1)); // The first document should have been removed due to the limit
        Assertions.assertNotNull(store.get(uri2));
        Assertions.assertNotNull(store.get(uri3));

        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("Document 4".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        Assertions.assertNull(store.get(uri2));
        Assertions.assertNotNull(store.get(uri3));
    }

    @Test
    public void testDocumentBytesLimit() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(20);
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("Short".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        //System.out.println(store.getCurrentDocumentBytes());
        store.put(new ByteArrayInputStream("Medium length".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        //System.out.println(store.getCurrentDocumentBytes());
        store.put(new ByteArrayInputStream("A longer document".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        //System.out.println(store.getCurrentDocumentBytes());
        //store.put(new ByteArrayInputStream("Will delete document 3".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        //System.out.println(store.getCurrentDocumentBytes());
        Assertions.assertNull(store.get(uri1));
        Assertions.assertNull(store.get(uri2));
        //Assertions.assertNull(store.get(uri3));
        //Assertions.assertNull(store.get(uri4));
        //Assertions.assertNull(store.getDocumentHeap().peek());
    }

    @Test
    public void testLimitationsAfterBroken() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("Short".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Medium length".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("A longer document".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Will delete document 3".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        Assertions.assertNotNull(store.get(uri1));
        store.setMaxDocumentCount(2);
        Assertions.assertNotNull(store.get(uri1));
        Assertions.assertNull(store.get(uri2));
        Assertions.assertNull(store.get(uri3));
        Assertions.assertNotNull(store.get(uri4));
    }

    @Test
    public void testLimitationsAfterBrokenBytes() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("Short".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        //System.out.println(store.getCurrentDocumentBytes());
        store.put(new ByteArrayInputStream("Medium length".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        //System.out.println(store.getCurrentDocumentBytes());
        store.put(new ByteArrayInputStream("A longer document".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        //System.out.println(store.getCurrentDocumentBytes());
        store.put(new ByteArrayInputStream("Will delete document 3".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        //System.out.println(store.getCurrentDocumentBytes());
        Assertions.assertNotNull(store.get(uri1));
        store.setMaxDocumentBytes(20);
        Assertions.assertNotNull(store.get(uri1));
        Assertions.assertNull(store.get(uri3));
        Assertions.assertNull(store.get(uri2));
        Assertions.assertNull(store.get(uri4));
    }

    @Test
    public void testBothLimitations() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("Short".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Medium length".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("A longer document".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Will delete document 3".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        store.setMaxDocumentCount(2);
        store.setMaxDocumentBytes(40);
        Assertions.assertNotNull(store.get(uri4));
        Assertions.assertNotNull(store.get(uri3));
        Assertions.assertNull(store.get(uri2));
    }
    @Test
    public void testAddingSameDocumentMultipleTimes() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri = URI.create("http://doc1");
        String content = "Same content";
        store.put(new ByteArrayInputStream(content.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream(content.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        //Assertions.assertEquals(1, store.getCurrentDocumentCount());
        //Assertions.assertEquals(content.length(), store.getCurrentDocumentBytes());
    }

    @Test
    public void testReplacingDocumentWithDifferentSize() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri = URI.create("http://doc1");
        String smallContent = "Small";
        String largeContent = "This is a larger content";
        store.put(new ByteArrayInputStream(smallContent.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        //int initialBytes = store.getCurrentDocumentBytes();
        store.put(new ByteArrayInputStream(largeContent.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        //Assertions.assertTrue(store.getCurrentDocumentBytes() > initialBytes);
    }
    @Test
    public void testDeletingDocuments() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri = URI.create("http://doc1");
        store.put(new ByteArrayInputStream("Content".getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        store.delete(uri);
        Assertions.assertNull(store.get(uri));
        //Assertions.assertEquals(0, store.getCurrentDocumentCount());
    }

    @Test
    public void testUndoOperations() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri = URI.create("http://doc1");
        String content = "Content";
        store.put(new ByteArrayInputStream(content.getBytes()), uri, DocumentStore.DocumentFormat.TXT);
        //System.out.println(store.getCurrentDocumentBytes());
        store.undo(uri);
        //System.out.println(store.getCurrentDocumentBytes());
        Assertions.assertNull(store.get(uri));
        Assertions.assertEquals(0, store.search("Content").size());
    }
    @Test
    public void testLimitEnforcementOnUndo() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setMaxDocumentCount(1);
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        store.put(new ByteArrayInputStream("Doc1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Doc2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        Assertions.assertNull(store.get(uri1));
        store.undo(uri2);
        //Assertions.assertNotNull(store.get(uri1));
        Assertions.assertNull(store.get(uri2));
    }

    @Test
    public void testSettingLimitsToZero() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.put(new ByteArrayInputStream("Doc1".getBytes()), URI.create("http://doc1"), DocumentStore.DocumentFormat.TXT);
        store.setMaxDocumentCount(1);
        //Assertions.assertEquals(1, store.getCurrentDocumentCount());
    }
    @Test
    public void testDecreasingLimitsBelowCurrentUsage() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.put(new ByteArrayInputStream("Doc1".getBytes()), URI.create("http://doc1"), DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Doc2".getBytes()), URI.create("http://doc2"), DocumentStore.DocumentFormat.TXT);
        store.setMaxDocumentCount(1);
        //Assertions.assertEquals(1, store.getCurrentDocumentCount());
        store.setMaxDocumentCount(1);
        //Assertions.assertEquals(1, store.getCurrentDocumentCount());
    }

    @Test
    public void testDeleteAllPrefixHeap() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("one two".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("one two three".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("one1 two2".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("two2 three3".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        Set<URI> returned = store.deleteAllWithPrefix("one");
        Assertions.assertEquals(0, store.searchByPrefix("one").size());
        //Assertions.assertEquals(1, store.getCurrentDocumentCount());
        store.undo();
        //Assertions.assertEquals(4, store.getCurrentDocumentCount());
        Assertions.assertEquals(3, store.searchByPrefix("one").size());
    }

    @Test
    public void testDeleteAll() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("one two".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("one two three".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("one1 two2".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("two2 three3".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        Set<URI> deleted = store.deleteAll("one");
        Assertions.assertEquals(2, deleted.size());
        Assertions.assertEquals(0, store.search("one").size());
        //Assertions.assertEquals(2, store.getCurrentDocumentCount());
    }

    @Test
    public void testDeleteAllWithMetadata() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        store.put(new ByteArrayInputStream("Doc1 content".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.get(uri1).setMetadataValue("author", "Alice");
        store.put(new ByteArrayInputStream("Doc2 content".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.get(uri2).setMetadataValue("author", "Bob");
        store.put(new ByteArrayInputStream("Doc3 content".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.get(uri3).setMetadataValue("author", "Alice");
        // Set up the metadata for deletion
        Map<String, String> metadata = new HashMap<>();
        metadata.put("author", "Alice");
        // Delete all documents with the specified metadata
        Set<URI> deletedDocs = store.deleteAllWithMetadata(metadata);
        // Check that the correct documents were deleted
        Assertions.assertEquals(2, deletedDocs.size());
        Assertions.assertTrue(deletedDocs.contains(uri1));
        Assertions.assertTrue(deletedDocs.contains(uri3));
        Assertions.assertFalse(deletedDocs.contains(uri2));
        // Check that the document count and bytes are updated correctly
        //Assertions.assertEquals(1, store.getCurrentDocumentCount());
        //Assertions.assertEquals("Doc2 content".length(), store.getCurrentDocumentBytes());
    }


    @Test
    public void testUndoPutHeap() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        store.put(new ByteArrayInputStream("one".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("two".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.undo();
        store.put(new ByteArrayInputStream("one again".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.undo();
        Assertions.assertEquals("one", store.get(uri1).getDocumentTxt());
    }

    @Test
    public void testDeleteAllAndUndo() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");

        store.put(new ByteArrayInputStream("test content".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("test content".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("different content".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);

        Set<URI> deletedUris = store.deleteAll("test");
        Assertions.assertEquals(2, deletedUris.size());
        Assertions.assertTrue(deletedUris.contains(uri1));
        Assertions.assertTrue(deletedUris.contains(uri2));
        Assertions.assertNull(store.get(uri1));
        Assertions.assertNull(store.get(uri2));
        Assertions.assertNotNull(store.get(uri3));
        //Assertions.assertEquals("different content" , store.getDocumentHeap().peek().getDocumentTxt());
        store.undo();
        Assertions.assertNotNull(store.get(uri1));
        Assertions.assertNotNull(store.get(uri2));
        Assertions.assertNotNull(store.get(uri3));
        //Assertions.assertEquals("test content" , store.getDocumentHeap().peek().getDocumentTxt());
    }

    @Test
    public void testDeleteAllWithPrefixHeap() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        store.put(new ByteArrayInputStream("testoo content".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("testee content".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("different content".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        Set<URI> deletedUris = store.deleteAllWithPrefix("test");
        Assertions.assertEquals(2, deletedUris.size());
        Assertions.assertTrue(deletedUris.contains(uri1));
        Assertions.assertTrue(deletedUris.contains(uri2));
        Assertions.assertNull(store.get(uri1));
        Assertions.assertNull(store.get(uri2));
        Assertions.assertNotNull(store.get(uri3));
        store.undo();
        Assertions.assertNotNull(store.get(uri1));
        Assertions.assertNotNull(store.get(uri2));
        Assertions.assertNotNull(store.get(uri3));
        //Assertions.assertEquals("testoo content" , store.getDocumentHeap().peek().getDocumentTxt());
    }

    @Test
    public void testDeleteAllWithMetadataHeap() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(1000); // Set a limit to test heap removal
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.setMetadata(uri1, "author", "Alice");
        store.setMetadata(uri2, "author", "Bob");
        store.setMetadata(uri3, "author", "Alice");
        Map<String, String> metadataQuery = new HashMap<>();
        metadataQuery.put("author", "Alice");
        Set<URI> deletedDocs = store.deleteAllWithMetadata(metadataQuery);
        Assertions.assertTrue(deletedDocs.contains(uri1));
        Assertions.assertTrue(deletedDocs.contains(uri3));
        Assertions.assertFalse(deletedDocs.contains(uri2));
        Assertions.assertNull(store.get(uri1));
        Assertions.assertNotNull(store.get(uri2));
        Assertions.assertNull(store.get(uri3));
        store.undo();
        Assertions.assertNotNull(store.get(uri1));
        Assertions.assertNotNull(store.get(uri2));
        Assertions.assertNotNull(store.get(uri3));
    }

    @Test
    public void testUndoChanges() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        long before = store.get(uri3).getLastUseTime();
        store.undo();
        long after = store.get(uri1).getLastUseTime();
        Assertions.assertNotEquals(before,after);

    }

    @Test
    public void testBinaryDocFirstTime() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, DocumentStore.DocumentFormat.BINARY);
        //System.out.println(store.getCurrentDocumentBytes());
        HashTable<String, String> metadata = new HashTableImpl<>();
        metadata.put("key", "value");
        store.undo(uri2);
        //System.out.println(store.getCurrentDocumentBytes());
    }
    @Test
    public void testGetBinary() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        Document doc1 = store.get(uri1);
        Document doc3 = store.get(uri3);
        doc1.setMetadataValue("key", "value");
        Set<URI> returned = store.deleteAllWithMetadata(metadata);
        Assertions.assertEquals(1, returned.size());
        Assertions.assertNotNull(store.get(uri3));
        Assertions.assertNotNull(store.get(uri2));
        store.undo();
        //Assertions.assertEquals(3, store.getCurrentDocumentCount());
        Assertions.assertNotNull(store.get(uri1));
        //Assertions.assertEquals(doc3, store.getDocumentHeap().peek());
    }

    @Test
    public void testDeleteAllMethodsWithBinary() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        Document doc1 = store.get(uri1);
        Document doc3 = store.get(uri3);
        doc1.setMetadataValue("key", "value");
        doc3.setMetadataValue("key", "value");
        Set<URI> returned = store.deleteAllWithPrefixAndMetadata( "Docume",metadata);
        Assertions.assertEquals(1, returned.size());
        Assertions.assertNull(store.get(uri3));
        store.undo();
        //Assertions.assertEquals(3, store.getCurrentDocumentCount());
    }

    @Test
    public void testDeleteAllWithBinary() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        //Document doc3 = store.get(uri3);
        store.undo();
        //Assertions.assertEquals(2, store.getCurrentDocumentCount());
    }

    @Test
    public void testBreakLimitWithUndo() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.BINARY);
        store.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 4".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        store.deleteAllWithPrefix("Documen");
        //Assertions.assertEquals(2, store.getCurrentDocumentCount());
        store.setMaxDocumentCount(2);
        store.undo();
        //Assertions.assertEquals(2, store.getCurrentDocumentCount());
        Assertions.assertNull(store.get(uri1));
    }

    @Test
    public void testDeletedCompletely() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        Document doc2 = store.get(uri2);
        store.setMaxDocumentCount(1);
        Assertions.assertNull(store.get(uri1));
        //Assertions.assertEquals(doc2, store.getDocumentHeap().peek());
        Assertions.assertEquals(1, store.search("Document").size());
    }

    @Test
    public void testNanoTimeDelete() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        Document doc1 = store.get(uri1);
        //System.out.println(doc1.getLastUseTime());
        store.delete(uri1);
        store.undo();
        //System.out.println(doc1.getLastUseTime());
    }

    @Test
    public void testLastDay() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        store.put(new ByteArrayInputStream("Document 1".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 2".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        Document doc2 = store.get(uri2);
        Document doc1 = store.get(uri1);
        store.delete(uri1);
        store.setMaxDocumentCount(1);
        //Assertions.assertEquals(doc2, store.getDocumentHeap().peek());
        store.undo();
        Assertions.assertNull(store.get(uri2));
    }

    @Test
    public void testAfterStage5Results() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("Document 1 will".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 2 will".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 3".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 4".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        Set<URI> deleted = store.deleteAll("will");
        Assertions.assertEquals(2, store.getCurrentDocumentCount());
        Assertions.assertEquals(2, deleted.size());
        store.undo(uri1);
        Assertions.assertEquals(3, store.getCurrentDocumentCount());
        Assertions.assertNotNull(uri1);
        Assertions.assertEquals(1, store.searchByPrefix("wil").size());
    }

    @Test
    public void testAfterStage5Results2() throws IOException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        URI uri1 = URI.create("http://doc1");
        URI uri2 = URI.create("http://doc2");
        URI uri3 = URI.create("http://doc3");
        URI uri4 = URI.create("http://doc4");
        store.put(new ByteArrayInputStream("Document 1 will".getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 2 will".getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 3 will2".getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        store.put(new ByteArrayInputStream("Document 4".getBytes()), uri4, DocumentStore.DocumentFormat.TXT);
        store.deleteAllWithPrefix("will");
        Assertions.assertEquals(1, store.search("Document").size());
        store.undo();
        Assertions.assertEquals(4, store.search("Document").size());
    }


}
