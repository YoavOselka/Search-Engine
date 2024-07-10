package edu.yu.cs.com1320.project.stage4;


import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage4.impl.DocumentStoreImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;


import javax.print.Doc;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class Stage4Test {
    @Test
    public void testSearch() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        URI URI_1 = URI.create("http://edu.yu.cs/com1320/project/stage4/document1");
        URI URI_2 = URI.create("http://edu.yu.cs/com1320/project/stage4/document2");
        String text1 = "This is a test document.";
        String text2 = "Another test document.";
        String text3 = "Yet another test document.";

        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        InputStream inputStream3 = new ByteArrayInputStream(text3.getBytes());
        TrieImpl<Document> thisTrie =  documentStore.getTrie();

        documentStore.put(inputStream1, URI_1, DocumentStore.DocumentFormat.TXT);
        List<Document> searchResults = documentStore.search("a");
        Assertions.assertEquals(1, searchResults.size());
        documentStore.undo();
        searchResults = documentStore.search("a");
        Assertions.assertEquals(0, searchResults.size());
    }

    @Test
    public void testUndoDelete() throws IOException{
        DocumentStoreImpl documentStore = new DocumentStoreImpl();

        // Add a document
        String text = "This is a test document";
        byte[] binaryData = new byte[0]; // Assuming empty byte array for simplicity
        URI uri = URI.create("test://document");
        InputStream input1 = new ByteArrayInputStream(text.getBytes());
        documentStore.put(input1, uri, DocumentStore.DocumentFormat.TXT);
        // Delete the document
        List<Document> searchResults = documentStore.search("a");
        Assertions.assertEquals(1, searchResults.size());
        documentStore.delete(uri);
        // Check if the document is deleted
        searchResults = documentStore.search("a");
        Assertions.assertNull(documentStore.get(uri));
        Assertions.assertEquals(0, searchResults.size());
        documentStore.undo();
        // Undo the delete operation
        searchResults = documentStore.search("a");
        Assertions.assertEquals(1, searchResults.size());
        Assertions.assertNotNull(documentStore.get(uri));
    }

    @Test
    public void testSearchByPrefix() throws IOException{
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        URI URI_1 = URI.create("http://edu.yu.cs/com1320/project/stage4/document1");
        URI URI_2 = URI.create("http://edu.yu.cs/com1320/project/stage4/document2");
        String text1 = "This is a test document.";
        String text2 = "Another test document.";

        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        TrieImpl<Document> thisTrie =  documentStore.getTrie();
        Document doc1 = new DocumentImpl(URI_2 ,text2);
        documentStore.put(inputStream1, URI_1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(inputStream2, URI_2, DocumentStore.DocumentFormat.TXT);
        List<Document> searchResults = documentStore.searchByPrefix("test");
        Assertions.assertEquals(2, searchResults.size());

    }

    @Test
    public void testUndoUrl() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text1 = "This is a test document";
        String text2 = "This not is a document2";
        URI uri1 = URI.create("test://document");
        URI uri2 = URI.create("test://document2");
        InputStream input1 = new ByteArrayInputStream(text1.getBytes());
        InputStream input2 = new ByteArrayInputStream(text2.getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        List<Document> searchResults = documentStore.searchByPrefix("test");
        List<Document> searchResults2 = documentStore.searchByPrefix("not");
        Assertions.assertEquals(1, searchResults.size());
        Document document = documentStore.get(uri1);
        documentStore.undo(uri1);
        searchResults = documentStore.searchByPrefix("test");
        Assertions.assertEquals(0, searchResults.size());
        Assertions.assertEquals(1, searchResults2.size());
    }

    @Test
    public void stage4UndoByURIThatImpactsOne() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();

        URI uri1 = URI.create("uri1");
        URI uri2 = URI.create("uri2");
        URI uri3 = URI.create("uri3");
        String text1 = "Document one";
        String text2 = "Document two";
        String text3 = "Document three";
        InputStream input1 = new ByteArrayInputStream(text1.getBytes());
        InputStream input2 = new ByteArrayInputStream(text2.getBytes());
        InputStream input3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(input1,uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input2,uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input3,uri3, DocumentStore.DocumentFormat.TXT);

        documentStore.deleteAll("Document");

        // Check that all documents are deleted
        Assertions.assertNull(documentStore.get(uri1));
        Assertions.assertNull(documentStore.get(uri2));
        Assertions.assertNull(documentStore.get(uri3));

        // Undo the delete operation for uri2
        documentStore.undo(uri2);

        // Check that only uri2 is restored
        Assertions.assertNull(documentStore.get(uri1));
        Assertions.assertNotNull(documentStore.get(uri2));
        Assertions.assertNull(documentStore.get(uri3));
    }

    @Test
    public void testUndoUrlDelete() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text1 = "This is a test document.";
        String text2 = "This not is a document2";
        String text3 = "This is a document3";
        URI uri1 = URI.create("test://document");
        URI uri2 = URI.create("test://document2");
        URI uri3 = URI.create("test://document3");
        InputStream input1 = new ByteArrayInputStream(text1.getBytes());
        InputStream input2 = new ByteArrayInputStream(text2.getBytes());
        InputStream input3 = new ByteArrayInputStream(text3.getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);
        documentStore.delete(uri1);
        documentStore.delete(uri3);
        List<Document> searchResults = documentStore.searchByPrefix("document2");
        Assertions.assertEquals(1, searchResults.size());

    }

    @Test
    public void testDeleteTrie(){
        Trie trie = new TrieImpl();
        trie.put("abc", 10);
        trie.put("abcd", 15);

        // Test deleting a value that exists
        Assertions.assertEquals(10, (int) trie.delete("abc", 10));
        Assertions.assertNull(trie.get("abc"));

        // Test deleting a value that doesn't exist
        Assertions.assertNull(trie.delete("abc", 10));
        // Test deleting a value from a key that doesn't exist
        Assertions.assertNull(trie.delete("ab", 5));
        // Test deleting a value from a key that exists but doesn't contain the value
        Assertions.assertNull(trie.delete("abcd", 5));
    }

    @Test
    public void testDeleteAll() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String keyword = "doc";
        String text1 = "doc"; InputStream input1 = new ByteArrayInputStream(text1.getBytes());
        String text2 = "doc";InputStream input2 = new ByteArrayInputStream(text2.getBytes());
        String text3 = "doc";InputStream input3 = new ByteArrayInputStream(text3.getBytes());
        String text4 = "doc";InputStream input4 = new ByteArrayInputStream(text4.getBytes());
        String text5 = "doc";InputStream input5 = new ByteArrayInputStream(text5.getBytes());
        URI uri1 = URI.create("test://document1");
        URI uri2 = URI.create("test://document2");
        URI uri3 = URI.create("test://document3");
        URI uri4 = URI.create("test://document4");
        URI uri5 = URI.create("test://document5");
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input4, uri4, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input5, uri5, DocumentStore.DocumentFormat.TXT);

        List<Document> searchResults = documentStore.search("doc");
        Assertions.assertEquals(5, documentStore.getTrie().get("doc").size());
        // Delete all documents with the keyword
        Set<URI> deletedDocuments = documentStore.deleteAll(keyword);
        searchResults = documentStore.search("doc");
        Assertions.assertEquals(0, searchResults.size());
        documentStore.undo();
        searchResults = documentStore.search("doc");
        Assertions.assertEquals(5, searchResults.size());
    }

    @Test
    public void testWordCount(){
        URI uri1 = URI.create("uri1");
        Document doc1 = new DocumentImpl(uri1, "test to test");
        doc1.wordCount("test");
        Assertions.assertEquals(2, doc1.wordCount("test"));
    }

    @Test
    public void testPutBinaryDoc() throws IOException {
        URI uri1 = URI.create("uri1");
        URI uri2=  URI.create("uri2");
        byte[] byte1 = "old".getBytes();
        byte[] byte2 = "new".getBytes();

        Document doc1 = new DocumentImpl(uri1,byte1);
        Document doc2 = new DocumentImpl(uri2, byte2);
        DocumentStore documentstore = new DocumentStoreImpl();
        //System.out.println(documentstore.put(new ByteArrayInputStream(byte1), uri1, DocumentStore.DocumentFormat.BINARY));
        //System.out.println(documentstore.put(new ByteArrayInputStream(byte2), uri1, DocumentStore.DocumentFormat.BINARY));
        //System.out.println(documentstore.get(uri1).getDocumentBinaryData());
    }

    @Test
    public void testDeleteAllWithPrefixTrie(){
        TrieImpl<Integer> trie = new TrieImpl<>();
        trie.put("hello", 1);
        trie.put("helloer",2 );
        trie.put("hellogr", 3);
        trie.put("nothello",4);
        Set<Integer> deletedValues = trie.deleteAllWithPrefix("hello");
        Assertions.assertEquals(3,deletedValues.size());
        System.out.println(deletedValues);
    }

    @Test
    public void testPutAndGetAll() {
        Trie<Integer> trie = new TrieImpl<>();

        trie.put("a", 1);
        trie.put("a", 2);
        trie.put("a", 3);

        Set<Integer> expected = new LinkedHashSet<>(Arrays.asList(3,2,1));
        Set<Integer> actual = trie.get("a");

        System.out.println(actual);
        System.out.println(expected);
    }
   @Test
    public void testGetAllWithPrefixSortedTrie(){
        TrieImpl<Integer> trie = new TrieImpl<>();
        trie.put("a", 10);
        trie.put("aa", 15);
        trie.put("arf", 20);
        Comparator<Integer> comparator = Comparator.naturalOrder();
        List<Integer> values = trie.getAllWithPrefixSorted("a", comparator);
        Assertions.assertEquals(3, values.size());
        //System.out.println(values);

    }
    @Test
    public void testDeleteAllTrie(){
        TrieImpl<Integer> trie = new TrieImpl<>();
        trie.put("a", 10);
        trie.put("a", 15);
        trie.put("a", 20);
        Set<Integer> deletedValues = trie.deleteAll("a");
        Iterator<Integer> iterator = deletedValues.iterator();
        int prev = Integer.MAX_VALUE;
        while (iterator.hasNext()) {
            int current = iterator.next();
            Assertions.assertTrue(current <= prev);
            prev = current;
        }
        Assertions.assertTrue(deletedValues.contains(10));
        Assertions.assertTrue(deletedValues.contains(15));
        Assertions.assertTrue(deletedValues.contains(20));
        List<Integer> remainingValues = trie.getSorted("a", Comparator.naturalOrder());
        Assertions.assertFalse(remainingValues.contains(10));
        Assertions.assertFalse(remainingValues.contains(15));
        Assertions.assertFalse(remainingValues.contains(20));
    }

    @Test
    public void testPutBinaryDocs() throws IOException {
        URI uri1 = URI.create("uri1");
        URI uri2=  URI.create("uri2");
        byte[] byte1 = "doc".getBytes();
        byte[] byte2 = "new".getBytes();

        Document doc1 = new DocumentImpl(uri1,byte1);
        Document doc2 = new DocumentImpl(uri2, byte2);
        DocumentStore documentstore = new DocumentStoreImpl();
        InputStream input1 = new ByteArrayInputStream("doc".getBytes());
        InputStream input2 = new ByteArrayInputStream("docc".getBytes());
        documentstore.put(new ByteArrayInputStream(byte1), uri1, DocumentStore.DocumentFormat.BINARY);
        documentstore.put(new ByteArrayInputStream(byte2), uri1, DocumentStore.DocumentFormat.BINARY);
        documentstore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        documentstore.put(input2, uri1, DocumentStore.DocumentFormat.TXT);
        List<Document> deleted = documentstore.searchByPrefix("doc");
        Assertions.assertEquals(2, deleted.size());

    }
    @Test
    public void testDeleteDocument() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        String text = "This is a test document";
        InputStream input1 = new ByteArrayInputStream(text.getBytes());
        URI uri = URI.create("test://document");

        documentStore.put(input1, uri, DocumentStore.DocumentFormat.TXT);
        documentStore.delete(uri);
        List<Document> searchResults = documentStore.search("a");
        Assertions.assertEquals(0, searchResults.size());
        Assertions.assertNull(documentStore.get(uri));
        documentStore.undo(uri);
        searchResults = documentStore.search("a");
        Assertions.assertEquals(1, searchResults.size());
        documentStore.undo(uri);
        searchResults = documentStore.search("a");
        Assertions.assertEquals(0, searchResults.size());
    }

    @Test
    public void testRemoveAll() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        URI URI_1 = URI.create("http://edu.yu.cs/com1320/project/stage4/document1");
        URI URI_2 = URI.create("http://edu.yu.cs/com1320/project/stage4/document2");
        String text1 = "This is a test document.";
        String text2 = "Another test document.";

        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        TrieImpl<Document> thisTrie =  documentStore.getTrie();
        documentStore.put(inputStream1, URI_1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(inputStream2, URI_2, DocumentStore.DocumentFormat.TXT);
        List<Document> searchResults = documentStore.search("test");
        Assertions.assertEquals(2, searchResults.size());
        documentStore.deleteAll("test");
        Assertions.assertNull(documentStore.get(URI_1));
        searchResults = documentStore.search("test");
        Assertions.assertEquals(0, searchResults.size());
    }

    @Test
    public void testRemoveAllWithPrefix() throws IOException{
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        URI URI_1 = URI.create("http://edu.yu.cs/com1320/project/stage4/document1");
        URI URI_2 = URI.create("http://edu.yu.cs/com1320/project/stage4/document2");
        String text1 = "This is a test document.";
        String text2 = "Another testf document.";
        InputStream inputStream1 = new ByteArrayInputStream(text1.getBytes());
        InputStream inputStream2 = new ByteArrayInputStream(text2.getBytes());
        TrieImpl<Document> thisTrie =  documentStore.getTrie();
        documentStore.put(inputStream1, URI_1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(inputStream2, URI_2, DocumentStore.DocumentFormat.TXT);
        List<Document> searchResults = documentStore.searchByPrefix("test");
        Assertions.assertEquals(2, searchResults.size());
        documentStore.deleteAllWithPrefix("test");
        searchResults = documentStore.search("test");
        Assertions.assertEquals(0, searchResults.size());
    }

    @Test
    public void testDeleteAllWithPrefix() throws IOException {
        DocumentStoreImpl documentStore = new DocumentStoreImpl();
        InputStream input1 = new ByteArrayInputStream("document1".getBytes());
        InputStream input2 = new ByteArrayInputStream("document2".getBytes());
        InputStream input3 = new ByteArrayInputStream("document3".getBytes());
        URI uri1 = URI.create("http://document1");
        URI uri2= URI.create("http://document2");
        URI uri3 = URI.create("http://document3");
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);

        // Delete all documents with the prefix "test://document"
        Set<URI> deletedDocuments = documentStore.deleteAllWithPrefix("doc");
        Assertions.assertEquals(3, deletedDocuments.size());

        Assertions.assertTrue(documentStore.get(uri1) == null);
        Assertions.assertTrue(documentStore.get(uri2) == null );
        Assertions.assertTrue(documentStore.get(uri3) == null);

        // Undo the deletion
        documentStore.undo();
        List<Document> searchResults = documentStore.searchByPrefix("doc");
        Assertions.assertEquals(3, searchResults.size());
    }

    @Test
    public void testDeleteAllByMetadata() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();
        URI uri1 = URI.create("document1");
        URI uri2 = URI.create("document2");
        InputStream input1 = new ByteArrayInputStream("doc1".getBytes());
        InputStream input2 = new ByteArrayInputStream("doc2".getBytes());
        Document doc1 = new DocumentImpl(uri1, "doc1");
        Document doc2= new DocumentImpl(uri2, "doc2");
        Map<String, String> metadata1 = new HashMap<>();
        metadata1.put("key1", "value1");
        metadata1.put("key2", "value2");
        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("key1", "value1");
        doc1.setMetadataValue("key1" , "value1");
        doc2.setMetadataValue("key1" , "value1");
        documentStore.put(input1, uri1 , DocumentStore.DocumentFormat.TXT);
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.get(uri1).setMetadataValue("key1" , "value1");
        documentStore.get(uri2).setMetadataValue("key1" , "value1");
        Map<String, String> deleteMetadata = new HashMap<>();
        deleteMetadata.put("key1", "value1");
        Set<URI> deletedUris = documentStore.deleteAllWithMetadata(deleteMetadata);
        Assertions.assertEquals(2, deletedUris.size());
        Assertions.assertTrue(deletedUris.contains(uri1));
        Assertions.assertTrue(deletedUris.contains(uri2));
        Assertions.assertNull(documentStore.get(uri1));
        documentStore.undo();
        Assertions.assertEquals(2, documentStore.searchByPrefix("do").size());
        Assertions.assertEquals(2, documentStore.searchByMetadata(deleteMetadata).size());
    }

    @Test
    public void testUndoDeleteAllWithKeywordAndMetadata() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();
        String keyword = "key";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");

        URI uri1 = URI.create("uri1");
        Document document = new DocumentImpl(uri1, "uri1");
        InputStream input1 = new ByteArrayInputStream("key amen".getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        Document doc = documentStore.get(uri1);
        doc.setMetadataValue("key1", "value1");
        List<Document> searchResults = documentStore.searchByPrefix("key");
        Assertions.assertEquals(1, searchResults.size());
        Set<URI> deletedDocuments = documentStore.deleteAllWithKeywordAndMetadata(keyword, metadata);
        searchResults = documentStore.search("key");
        Assertions.assertEquals(0, searchResults.size());
        Assertions.assertEquals(uri1, document.getKey());

        documentStore.undo();
        Document restoredDocument = documentStore.get(uri1);
        Assertions.assertEquals("key amen", restoredDocument.getDocumentTxt());
        searchResults = documentStore.search("key");
        Assertions.assertEquals(1, searchResults.size());
        //Assertions.assertEquals(metadata, restoredDocument.getMetadata());
    }

    @Test
    public void stage4UndoByURIThatImpactsEarlierThanLast() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();

        URI uri1 = URI.create("uri1");
        URI uri2 = URI.create("uri2");
        URI uri3 = URI.create("uri3");

        String text1 = "Document one";
        String text2 = "Document two";
        String text3 = "Document three";

        documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        documentStore.delete(uri1);
        documentStore.delete(uri2);
        documentStore.delete(uri3);
        // Undo delete operation for uri1
        documentStore.undo(uri1);
        // Check that uri1 is restored
        Assertions.assertNotNull(documentStore.get(uri1));
        // Check that uri2 is still deleted
        Assertions.assertNull(documentStore.get(uri2));
        // Check that uri3 is still deleted
        Assertions.assertNull(documentStore.get(uri3));
        List<Document> returned = documentStore.search("one");
        Assertions.assertEquals(1, returned.size());
    }
    @Test
    public void stage4PlainUndoThatImpactsMultiple() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();

        URI uri1 = URI.create("uri1");
        URI uri2 = URI.create("uri2");
        URI uri3 = URI.create("uri3");

        String text1 = "Document one";
        String text2 = "Document two";
        String text3 = "Document three";

        documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        // Delete all documents
        documentStore.deleteAll("Document");
        // Undo delete operation
        documentStore.undo();
        // Check that all documents are restored
        Assertions.assertNotNull(documentStore.get(uri1));
        Assertions.assertNotNull(documentStore.get(uri2));
        Assertions.assertNotNull(documentStore.get(uri3));
    }

    @Test
    public void testUndoMetadataOneDocumentThenSearch() throws IOException {
       //set metadata then delete by metadata then undo the delete then search by metadata
        DocumentStore documentStore = new DocumentStoreImpl();
        URI uri1 = URI.create("uri1");
        URI uri2 = URI.create("uri2");
        URI uri3 = URI.create("uri3");

        String text1 = "Document one";
        String text2 = "Document two";
        String text3 = "Document three";
        documentStore.put(new ByteArrayInputStream(text1.getBytes()), uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(new ByteArrayInputStream(text2.getBytes()), uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.put(new ByteArrayInputStream(text3.getBytes()), uri3, DocumentStore.DocumentFormat.TXT);
        Document doc1 = documentStore.get(uri1);
        Document doc2 = documentStore.get(uri2);
        doc1.setMetadataValue("key1", "value1");
        doc2.setMetadataValue("key2" , "value2");
        Map<String, String> metaDataToDelete = new HashMap<>();
        metaDataToDelete.put("key1", "value1");
        List<Document> returned = documentStore.searchByMetadata(metaDataToDelete);
        Assertions.assertEquals(1, returned.size());
        documentStore.deleteAllWithMetadata(metaDataToDelete);
        returned = documentStore.searchByMetadata(metaDataToDelete);
        Assertions.assertEquals(0, returned.size());
        documentStore.undo();
        returned = documentStore.searchByMetadata(metaDataToDelete);
        Assertions.assertEquals(1, returned.size());

    }
    @Test
    public void testDeleteAllWithPrefixAndMetadataUndo() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();
        String keyword = "key";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");

        URI uri1 = URI.create("uri1");
        Document document = new DocumentImpl(uri1, "uri1");
        InputStream input1 = new ByteArrayInputStream("key amen".getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        Document doc = documentStore.get(uri1);
        doc.setMetadataValue("key1", "value1");
        List<Document> searchResults = documentStore.searchByPrefix("key");
        Assertions.assertEquals(1, searchResults.size());
        Set<URI> deletedDocuments = documentStore.deleteAllWithPrefixAndMetadata(keyword, metadata);
        searchResults = documentStore.search("key");
        Assertions.assertEquals(0, searchResults.size());
        documentStore.undo();
        Document restoredDocument = documentStore.get(uri1);
        Assertions.assertEquals("key amen", restoredDocument.getDocumentTxt());
        searchResults = documentStore.search("key");
        Assertions.assertEquals(1, searchResults.size());
        List<Document> returned = documentStore.searchByMetadata(metadata);
        Assertions.assertEquals(1, returned.size());
    }

    @Test
    public void testMultipleTimeAndOrder() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();
        InputStream input1 = new ByteArrayInputStream("key key key key".getBytes());
        InputStream input2 = new ByteArrayInputStream("key key".getBytes());
        InputStream input3 = new ByteArrayInputStream("key key key".getBytes());
        List<Document> listOrder = new ArrayList<>();
        URI uri1 = URI.create("uri1");
        URI uri2 = URI.create("uri2");
        URI uri3 = URI.create("uri3");
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);
        Document doc1 = documentStore.get(uri1);
        Document doc2 = documentStore.get(uri2);
        Document doc3 = documentStore.get(uri3);
        listOrder = documentStore.search("key");
        //System.out.println(listOrder);

    }

    @Test
    public void testSearchByMetaData() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl(); // Instantiate your DocumentStoreImpl class
        URI uri1 = URI.create("document1");
        //Document doc1 = new DocumentImpl(uri1, "Document 1");
        InputStream input1 = new ByteArrayInputStream("Document 1".getBytes());
        documentStore.put(input1,uri1, DocumentStore.DocumentFormat.TXT);
        Document doc1 = documentStore.get(uri1);
        doc1.setMetadataValue("author", "Alice");
        doc1.setMetadataValue("category", "fiction");



        URI uri2 = URI.create("document2");
        //Document doc2 = new DocumentImpl(uri2, "Document 2");
        InputStream input2 = new ByteArrayInputStream("Document 2".getBytes());
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        Document doc2 = documentStore.get(uri2);
        doc2.setMetadataValue("author", "Bob");
        doc2.setMetadataValue("category", "non-fiction");


        URI uri3 = URI.create("document3");
        //Document doc3 = new DocumentImpl(uri3, "Document 3");
        InputStream input3 = new ByteArrayInputStream("Document 3".getBytes());
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);
        Document doc3 = documentStore.get(uri3);
        doc3.setMetadataValue("author", "Alice");
        doc3.setMetadataValue("category", "fiction");
        Map<String, String> metadataQuery = new HashMap<>();
        metadataQuery.put("author", "Alice");
        metadataQuery.put("category", "fiction");

        List<Document> result = documentStore.searchByMetadata(metadataQuery);

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains(doc1));
        Assertions.assertTrue(result.contains(doc3));
    }

    @Test
    public void testSearchByKeywordAndMetadata() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();
        URI uri1 = URI.create("document1");
        InputStream input1 = new ByteArrayInputStream("Document 1 contains apple".getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        Document doc1 = documentStore.get(uri1);
        doc1.setMetadataValue("author", "Alice");
        doc1.setMetadataValue("category", "fiction");

        URI uri2 = URI.create("document2");
        InputStream input2 = new ByteArrayInputStream("Document 2 contains apple".getBytes());
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        Document doc2 = documentStore.get(uri2);
        doc2.setMetadataValue("author", "Bob");
        doc2.setMetadataValue("category", "non-fiction");

        URI uri3 = URI.create("document3");
        InputStream input3 = new ByteArrayInputStream("Document 3 does contain apple".getBytes());
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);
        Document doc3 = documentStore.get(uri3);
        doc3.setMetadataValue("author", "Alice");
        doc3.setMetadataValue("category", "fiction");

        Map<String, String> metadataQuery = new HashMap<>();
        metadataQuery.put("author", "Alice");
        metadataQuery.put("category", "fiction");

        List<Document> result = documentStore.searchByKeywordAndMetadata("apple", metadataQuery);

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains(doc1));
        //Assertions.assertTrue(result.contains(doc3));
    }

    @Test
    public void testGet() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl();
        URI uri1 = URI.create("uri1");
        InputStream input1 = new ByteArrayInputStream("doc1".getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        Assertions.assertEquals(null, documentStore.get(uri1).getDocumentBinaryData());
    }

    @Test
    public void testSearchByPrefixAndMetadata() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl(); // Instantiate your DocumentStoreImpl class
        URI uri1 = URI.create("document1");
        InputStream input1 = new ByteArrayInputStream("pple is a fruit".getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        Document doc1 = documentStore.get(uri1);
        doc1.setMetadataValue("author", "Alice");
        doc1.setMetadataValue("category", "fiction");

        URI uri2 = URI.create("document2");
        InputStream input2 = new ByteArrayInputStream("Application is a software".getBytes());
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        Document doc2 = documentStore.get(uri2);
        doc2.setMetadataValue("author", "Bob");
        doc2.setMetadataValue("category", "non-fiction");

        URI uri3 = URI.create("document3");
        InputStream input3 = new ByteArrayInputStream("An Apple a day keeps the doctor away".getBytes());
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);
        Document doc3 = documentStore.get(uri3);
        doc3.setMetadataValue("author", "Alice");
        doc3.setMetadataValue("category", "fiction");

        Map<String, String> metadataQuery = new HashMap<>();
        metadataQuery.put("author", "Alice");
        metadataQuery.put("category", "fiction");

        List<Document> result = documentStore.searchByPrefixAndMetadata("App", metadataQuery);

        Assertions.assertEquals(1, result.size());
        //Assertions.assertTrue(result.contains(doc1));
        Assertions.assertTrue(result.contains(doc3));
    }

    @Test
    public void testDeleteAllWithMetadata() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl(); // Instantiate your DocumentStoreImpl class
        URI uri1 = URI.create("document1");
        InputStream input1 = new ByteArrayInputStream("Document 1".getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        Document doc1 = documentStore.get(uri1);
        doc1.setMetadataValue("author", "Alice");
        doc1.setMetadataValue("category", "fiction");

        URI uri2 = URI.create("document2");
        InputStream input2 = new ByteArrayInputStream("Document 2".getBytes());
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        Document doc2 = documentStore.get(uri2);
        doc2.setMetadataValue("author", "Bob");
        doc2.setMetadataValue("category", "non-fiction");

        URI uri3 = URI.create("document3");
        InputStream input3 = new ByteArrayInputStream("Document 3".getBytes());
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);
        Document doc3 = documentStore.get(uri3);
        doc3.setMetadataValue("author", "Alice");
        doc3.setMetadataValue("category", "fiction");

        Map<String, String> metadataQuery = new HashMap<>();
        metadataQuery.put("author", "Alice");
        metadataQuery.put("category", "fiction");

        Set<URI> deletedURIs = documentStore.deleteAllWithMetadata(metadataQuery);

        Assertions.assertEquals(2, deletedURIs.size());
        Assertions.assertTrue(deletedURIs.contains(uri1));
        Assertions.assertTrue(deletedURIs.contains(uri3));

        // Verify that the documents are actually deleted
        Assertions.assertNull(documentStore.get(uri1));
        Assertions.assertNull(documentStore.get(uri3));
    }

    @Test
    public void testDeleteAllWithKeywordAndMetadata() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl(); // Instantiate your DocumentStoreImpl class
        URI uri1 = URI.create("document1");
        InputStream input1 = new ByteArrayInputStream("Document 1 contains apple".getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        Document doc1 = documentStore.get(uri1);
        doc1.setMetadataValue("author", "Alice");
        doc1.setMetadataValue("category", "fiction");

        URI uri2 = URI.create("document2");
        InputStream input2 = new ByteArrayInputStream("Document 2 contains apple".getBytes());
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        Document doc2 = documentStore.get(uri2);
        doc2.setMetadataValue("author", "Bob");
        doc2.setMetadataValue("category", "non-fiction");

        URI uri3 = URI.create("document3");
        InputStream input3 = new ByteArrayInputStream("Document 3 does not contain apple".getBytes());
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);
        Document doc3 = documentStore.get(uri3);
        doc3.setMetadataValue("author", "Alice");
        doc3.setMetadataValue("category", "fiction");

        Map<String, String> metadataQuery = new HashMap<>();
        metadataQuery.put("author", "Alice");
        metadataQuery.put("category", "fiction");

        Set<URI> deletedURIs = documentStore.deleteAllWithKeywordAndMetadata("apple", metadataQuery);

        Assertions.assertEquals(2, deletedURIs.size());
        Assertions.assertTrue(deletedURIs.contains(uri1));
        Assertions.assertTrue(deletedURIs.contains(uri3));

        // Verify that the documents are actually deleted
        Assertions.assertNull(documentStore.get(uri1));
        Assertions.assertNull(documentStore.get(uri3));
    }

    @Test
    public void testDeleteAllWithPrefixAndMetadata() throws IOException {
        DocumentStore documentStore = new DocumentStoreImpl(); // Instantiate your DocumentStoreImpl class
        URI uri1 = URI.create("document1");
        InputStream input1 = new ByteArrayInputStream("Apple is a fruit".getBytes());
        documentStore.put(input1, uri1, DocumentStore.DocumentFormat.TXT);
        Document doc1 = documentStore.get(uri1);
        doc1.setMetadataValue("author", "Alice");
        doc1.setMetadataValue("category", "fiction");

        URI uri2 = URI.create("document2");
        InputStream input2 = new ByteArrayInputStream("Application is a software".getBytes());
        documentStore.put(input2, uri2, DocumentStore.DocumentFormat.TXT);
        Document doc2 = documentStore.get(uri2);
        doc2.setMetadataValue("author", "Bob");
        doc2.setMetadataValue("category", "non-fiction");

        URI uri3 = URI.create("document3");
        InputStream input3 = new ByteArrayInputStream("An Apple a day, keeps the doctor away.".getBytes());
        documentStore.put(input3, uri3, DocumentStore.DocumentFormat.TXT);
        Document doc3 = documentStore.get(uri3);
        doc3.setMetadataValue("author", "Alice");
        doc3.setMetadataValue("category", "fiction");

        Map<String, String> metadataQuery = new HashMap<>();
        metadataQuery.put("author", "Alice");
        metadataQuery.put("category", "fiction");

        Set<URI> deletedURIs = documentStore.deleteAllWithPrefixAndMetadata("App", metadataQuery);

        Assertions.assertEquals(2, deletedURIs.size());
        Assertions.assertTrue(deletedURIs.contains(uri1));
        Assertions.assertTrue(deletedURIs.contains(uri3));

        Assertions.assertNull(documentStore.get(uri1));
        Assertions.assertNull(documentStore.get(uri3));
    }
}
