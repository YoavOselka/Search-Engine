This project implements a Document Management System with various data structures.

## Data Structures
- BTreeImpl.java - implementation of a BTree, used to store documents or references to documents that were deleted.
- MinHeapImpl - implementation of a min-heap, 
- StackImpl.java - implementation of a stack, tracks actions done in a stack order, helpful to undo actions.
- TrieImpl.java - implementation of a Trie, used to search for documents in the engine.

## Core Classes
- Document.java: Interface defining a document.
- DocumentImpl.java: Implementation of the `Document` interface.
- DocumentStore.java: Interface defining the operations for managing documents.
- DocumentStoreImpl.java: Implementation of the `DocumentStore` interface.
- PersistenceManager.java: Interface for persistence management.
- DocumentPersistenceManager.java: Implementation of the `PersistenceManager` interface.
