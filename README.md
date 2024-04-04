## Group 7 cs7280: MyPFS File System
## Member
Jianhua Xue and Zihan Zheng

## Project Phase 2:
### Allocation method:
Indexed allocation

### Freespace management:
Bitmap

### Developing environment:
- Language: Java
- IDE: IntelliJ IDEA

### Assumptions and limitations
- The database file has 1MB (1024KB) fixed size, and the system supports increasing by 1MB automatically
- Each database file contains 4096 blocks, and the size of each block is 256 bytes
- On top of each database, 1 block is used to store metadata, and 2 blocks are used to store the bitmap information
- In each database file, a maximum of 8 File Control Blocks (FCB) is allowed
- Each block supports 5 data entries, each data entry has a size of 44 bytes(40 bytes value + 4 bytes key)
- The data entry is in the format of key-value, while keys are unique
- Duplicate keys are not handled
- Only CSV format is supported
- Not a memory-based file system, but based on the existing file system on OS

### Project structure
- Main.java: the main entry point of this system
- DB.java: supports database operations including open, put, get, etc
- Block.java: represents a block in the database file with relevant methods
- DataEntry.java: represents a data entry with an ID and a value with relevant methods
- FCB.java: represents a FCB with file metadata with relevant methods
- Metadata.java: represents the metadata of the database file with relevant methods
- Bitmap.java: provides methods for serialization and deserialization of bitmap
- BTree.java, BTreeNode.java, BTreeSerializer.java: adapted from phase 1 of the project and implemented the B-Tree indexing structure
- Utils.java: contains methods that handle the input from CSV file

### Database structure overview
METADATA block:
- The code defines the Metadata class to represent the metadata block.
- The Metadata class contains fields such as dbName (database name) and suffix, which can be used to store relevant metadata information.
- The serialize and deserialize methods in the Metadata class handle the serialization and deserialization of the metadata block.

Bitmap blocks:
- The code allocates 2 blocks (BITMAP_BLOCK_CNT) to store the bitmap information.
- The BitMap class provides methods to serialize and deserialize the bitmap.
- The serializeBitmap method converts the boolean array representation of the bitmap into a byte array of 512 bytes (2 blocks * 256 bytes per block).
- The deserializeBitmap method converts the byte array representation back into a boolean array.

FCB (File Control Block):
- The code defines the FCB class to represent the File Control Block.
- The FCB class contains fields such as name (file name), type (file type), indexBlocks (list of index block numbers), and dataBlocks (list of data block numbers).
- The serialize and deserialize methods in the FCB class handle the serialization and deserialization of the FCB.
- The serialized FCB occupies a fixed size of 256 bytes (BLOCK_SIZE).

File Block:
- The code defines the Block class to represent a file block.
- Each block has a fixed size of 256 bytes (BLOCK_SIZE).
- The Block class contains a byte array (data) to store the actual data.
- The initializeDefaultBytes, fillUpWithDefaultBytes, write, isFull, getDataEntries, and getValidLength methods in the Block class provide functionality to manage and manipulate the file blocks.

Overall directory:
- The code defines the DB class to represent the database.
- The DB class contains fields such as blocks (list of blocks), metadata (metadata block), bitmap (bitmap information), and fcbs (list of FCBs).
- The BLOCK_CNT constant is set to 4096, indicating that each database file contains 4096 blocks. The BLOCK_SIZE constant is set to 256, specifying the size of each block in bytes. The METADATA_BLOCK_CNT constant is set to 1, indicating that 1 block is used to store metadata. The BITMAP_BLOCK_CNT constant is set to 2, indicating that 2 blocks are used to store the bitmap information.

### Running myPFS
To execute myPFS under the directory 'PFS/src', please follow the steps:
1. 'git clone https://github.com/ZAN2023/cs7280.git';
2. go to the directory of PFS/src/com/neu/nosql
4. run main.java
5. in the terminal, test with the below commands:
   - open <db_name>: Allocate a new 1 MByte <db_name> file if it does not already exist. If it does exist, begin using it for further commands
   - put <local_file>: Inserts data from the OS file <local_file> into the NoSQL database <db_name>.
   - get <local_file>: Downloads the data file <local_file> from the NoSQL database <db_name> and saves it to the current OS directory
   - rm <local_file>: Deletes <local_file> from the NoSQL database <db_name>
   - dir: Lists all data files in the NoSQL database <db_name>
   - find <local_file> <key>: Finds the value associated with the given key from <local_file> in the NoSQL database
   - kill <db_name>: Removes the PFS file <db_name> from the OS file system
   - quit: Exit the program
     

## Project Phase 1:
B-Tree Indexing Structure Implementation in Go
### Index Name
B-tree
### Source of selected index
Introduction to Algorithms, Third Edition, by Cormen, Leiserson, Rivest and Stein, from MIT Press (https://mitpress.mit.edu/books/introduction-algorithms), Chapter 18
### Input
Manual
### Assumptions and limitations
- The index is designed for operating on main memory storage only in this Phase 1;
- The keys and values are taken as integer values;
- In the simulation of block operations, a simple array referencing in the blocks is used, which is dynamically growing if the number of occupied blocks increases.
- For the index of NoSQL database in Phase 2, the simple integers of key-value pair might need adjustment;
- In Phase 2, the index would be an index file with blocks on disk, which increases the challenge of considering disk I/O, block design, and buffer;
- Limited for handling larger datasets in the efficiency of lookup and insertion operations. Need to think about scalability and performance.

### Introduction
This project is aimed at implementing a main memory B-tree indexing structure. Three methods are included:
- Lookup(int keyValue): Searches for a specified value within the B-tree. Returns True if the value exists, otherwise False.
- Insert(int keyValue): Inserts a specified value into the B-tree, maintaining the tree's balanced structure.
- Display(int node): Prints the structure of the indexing tree starting from the specified node.

### Running 'BTree.go'
To execute the 'Btree.go' file under the directory 'new', please follow the steps:
1. Install Go;
2. 'git clone https://github.com/ZAN2023/cs7280.git';
3. 'cd cs7280/new';
4. 'go run Btree.go';


To test with a differnt degree(t):
- Each node can contain between t-1 to 2t-1 keys (inclusive)
- Change the input of "bTree := NewBTree(4)" in main() function


To test with a different numbers:
-  Change the input of "keys := int{}" in main() function
