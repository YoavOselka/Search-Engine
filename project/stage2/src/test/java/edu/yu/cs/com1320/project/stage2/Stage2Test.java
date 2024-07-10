package edu.yu.cs.com1320.project.stage2;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.stage2.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage2.impl.DocumentStoreImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.Collection;
import java.util.Set;
import java.net.URI;

public class Stage2Test{
	private HashTableImpl<String, Integer> hashTable;

	@BeforeEach
	public void setUp(){
		hashTable = new HashTableImpl<>();
	}

	@Test
	public void testPutAndGet() {
		Assertions.assertNull(hashTable.put("key1", 1));
		Assertions.assertEquals(1, hashTable.size());
		Assertions.assertEquals(1, hashTable.get("key1"));

		Assertions.assertEquals(1, hashTable.put("key1", 2));
		Assertions.assertEquals(1, hashTable.size());
		Assertions.assertEquals(2, hashTable.get("key1"));

		Assertions.assertNull(hashTable.put("key2", 3));
		Assertions.assertEquals(2, hashTable.size());
		Assertions.assertEquals(3, hashTable.get("key2"));
	}
	@Test
	public void testContainsKey() {
		Assertions.assertThrows(NullPointerException.class, ()->{
			hashTable.containsKey(null);
		});
		Assertions.assertFalse(hashTable.containsKey("key1"));
		hashTable.put("key1", 1);
		Assertions.assertTrue(hashTable.containsKey("key1"));
	}

	@Test
	public void testKeySet() {
		hashTable.put("key1", 1);
		hashTable.put("key2", 2);

		Set<String> keySet = hashTable.keySet();
		Assertions.assertTrue(keySet.contains("key1"));
		Assertions.assertTrue(keySet.contains("key2"));
		Assertions.assertFalse(keySet.contains("key3"));
	}

	@Test
	public void testValues() {
		hashTable.put("key1", 1);
		hashTable.put("key2", 2);

		Collection<Integer> values = hashTable.values();
		Assertions.assertTrue(values.contains(1));
		Assertions.assertTrue(values.contains(2));
		Assertions.assertFalse(values.contains(3));
	}

	@Test
	public void testSize() {
		Assertions.assertEquals(0, hashTable.size());
		hashTable.put("key1", 1);
		Assertions.assertEquals(1, hashTable.size());
		hashTable.put("key2", 2);
		Assertions.assertEquals(2, hashTable.size());
	}

	private DocumentImpl createDocument(URI uri, String txt) {
		return new DocumentImpl(uri, txt);
	}

	@Test
	public void testSetAndGetMetadataValue() {
		DocumentImpl document = createDocument(URI.create("http://example.com/document1"), "This is a sample document.");
		document.setMetadataValue("author", "John Doe");
		Assertions.assertEquals("John Doe", document.getMetadataValue("author"));
	}

	@Test
	public void testGetMetadata() {
		DocumentImpl document = createDocument(URI.create("http://example.com/document1"), "This is a sample document.");
		document.setMetadataValue("author", "John Doe");
		document.setMetadataValue("date", "2024-02-25");
		HashTable<String, String> metadata = document.getMetadata();
		Assertions.assertEquals(2, metadata.size());
		Assertions.assertEquals("John Doe", metadata.get("author"));
		Assertions.assertEquals("2024-02-25", metadata.get("date"));
	}

	@Test
	public void testGetDocumentTxt() {
		DocumentImpl document = createDocument(URI.create("http://example.com/document1"), "This is a sample document.");
		Assertions.assertEquals("This is a sample document.", document.getDocumentTxt());
	}

	@Test
	public void testGetDocumentBinaryData() {
		byte[] binaryData = {0x00, 0x01, 0x02, 0x03};
		DocumentImpl document = new DocumentImpl(URI.create("http://example.com/document1"), binaryData);
		Assertions.assertArrayEquals(binaryData, document.getDocumentBinaryData());
	}

	@Test
	public void testGetKey() {
		URI uri = URI.create("http://example.com/document1");
		DocumentImpl document = createDocument(uri, "This is a sample document.");
		Assertions.assertEquals(uri, document.getKey());
	}

	@Test
	public void testHashCode() {
		DocumentImpl document1 = createDocument(URI.create("http://example.com/document1"), "This is a sample document.");
		DocumentImpl document2 = createDocument(URI.create("http://example.com/document1"), "This is a sample document.");
		Assertions.assertEquals(document1.hashCode(), document2.hashCode());
	}

	@Test
	public void testEquals() {
		DocumentImpl document1 = createDocument(URI.create("http://example.com/document1"), "This is a sample document.");
		DocumentImpl document2 = createDocument(URI.create("http://example.com/document1"), "This is a sample document.");
		Assertions.assertTrue(document1.equals(document2));
	}

	@Test
	public void testCollision() {
		HashTableImpl<Integer, String> hashTable = new HashTableImpl<>();
		hashTable.put(1, "One");
		hashTable.put(6, "Six"); // This should collide with key 1
		Assertions.assertEquals("One", hashTable.get(1)); // Make sure the first key-value pair is still accessible
		Assertions.assertEquals("Six", hashTable.get(6)); // Make sure the second key-value pair is accessible
	}

	@Test
	public void testMultipleCollisions() {
		HashTableImpl<Integer, String> hashTable = new HashTableImpl<>();
		hashTable.put(1, "One");
		hashTable.put(6, "Six"); // This should collide with key 1
		hashTable.put(11, "Eleven"); // This should collide with key 1 and key 6
		Assertions.assertEquals("One", hashTable.get(1)); // Make sure the first key-value pair is still accessible
		Assertions.assertEquals("Six", hashTable.get(6)); // Make sure the second key-value pair is still accessible
		Assertions.assertEquals("Eleven", hashTable.get(11)); // Make sure the third key-value pair is accessible

	}


}