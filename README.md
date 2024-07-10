This project implements a Document Management System with various data structures.

## Data Structures
- BTreeImpl.java - implementation of a BTree, used for storing documents or references to documents that have been deleted.
- MinHeapImpl - implementation of a min-heap, used for 
- StackImpl.java - implementation of a stack, which tracks actions in a stack order, making it useful for undoing actions.
- TrieImpl.java - implementation of a Trie, used for efficiently searching for documents in the engine.

## Core Classes
- Document.java: Interface defining a document.
- DocumentImpl.java: Implementation of the `Document` interface.
- DocumentStore.java: Interface defining the operations for managing documents.
- DocumentStoreImpl.java: Implementation of the `DocumentStore` interface.
- PersistenceManager.java: Interface for persistence management.
- DocumentPersistenceManager.java: Implementation of the `PersistenceManager` interface.
