# Group 7 cs7280: B-Tree Indexing Structure Implementation in Go
## Member
Jianhua Xue and Zihan Zheng
## Index Name
B-tree
## Source of selected index
Introduction to Algorithms, Third Edition, by Cormen, Leiserson, Rivest and Stein, from MIT Press (https://mitpress.mit.edu/books/introduction-algorithms), Chapter 18
## Input
Manual
## Assumptions
- The index is designed for operating on main memory storage only in this Phase 1;
- The keys and values are taken as integer values;
- In the simulation of block operations, a simple array referencing in the blocks is used, which is dynamically growing if the number of occupied blocks increases.
## Limitations
- For the index of NoSQL database in Phase 2, the simple integers of key-value pair might need adjustment;
- In Phase 2, the index would be an index file with blocks on disk, which increases the challenge of considering disk I/O, block design, and buffer;
- Limited for handling larger datasets in the efficiency of lookup and insertion operations. Need to think about scalability and performance.

## Introduction
This project is aimed at implementing a main memory B-tree indexing structure. Three methods are included:
- Lookup(int keyValue): Searches for a specified value within the B-tree. Returns True if the value exists, otherwise False.
- Insert(int keyValue): Inserts a specified value into the B-tree, maintaining the tree's balanced structure.
- Display(int node): Prints the structure of the indexing tree starting from the specified node.

## Running 'BTree.go'
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
