package main

import (
	"fmt"
)

type Key struct {
	key, val int
}

// BTreeNode represents a node in the B-tree
type BTreeNode struct {
	leaf  bool
	keys  []Key
	child []*BTreeNode
}

// BTree represents the B-tree
// Each internal node can have at most 2t - 1 keys.
// Each internal node can have at most 2t children.
// Each internal node, except for the root, must have at least t - 1 keys.
// Each internal node, except for the root, must have at least t children.
// All leaves must be at the same level.
type BTree struct {
	root *BTreeNode
	t    int // Minimum degree (defines the range for number of keys)
}

// Create a new B-tree with the specified minimum degree
func NewBTree(t int) *BTree {
	return &BTree{
		root: &BTreeNode{leaf: true},
		t:    t,
	}
}

// Insert a key into the B-tree
func (bt *BTree) Insert(key, val int) {
	root := bt.root
	if len(root.keys) == (2*bt.t)-1 {
		newRoot := &BTreeNode{}
		bt.root = newRoot
		newRoot.child = append(newRoot.child, root)
		bt.splitChild(newRoot, 0)
		bt.insertNonFull(newRoot, key, val)
	} else {
		bt.insertNonFull(root, key, val)
	}
}

// Insert a key into a non-full B-tree node
func (bt *BTree) insertNonFull(x *BTreeNode, key, val int) {
	i := len(x.keys) - 1
	if x.leaf { // if x is leaf node
		x.keys = append(x.keys, Key{})
		for i >= 0 && key < x.keys[i].key {
			x.keys[i+1] = x.keys[i]
			i--
		}
		x.keys[i+1] = Key{key: key, val: val} // line53-57 finds the appropriate position to put key to
	} else {
		for i >= 0 && key < x.keys[i].key {
			i--
		}
		i++
		if len(x.child[i].keys) == (2*bt.t)-1 {
			bt.splitChild(x, i) // After splitting, the median key from the full child x.child[i] is moved up to the parent x at index i.
			if key > x.keys[i].key {
				i++
			}
		}
		bt.insertNonFull(x.child[i], key, val)
	}
}

// Split a child node of the B-tree
// x: parent node, i: index of the child to be split.
func (bt *BTree) splitChild(x *BTreeNode, i int) {
	t := bt.t                     // minimum degree
	y := x.child[i]               // y is the node to be split
	z := &BTreeNode{leaf: y.leaf} // a new node (right sibling of y, to put values split from y)

	x.child = append(x.child[:i+1], nil)
	copy(x.child[i+2:], x.child[i+1:])
	x.child[i+1] = z // line79-81 inserts z(the new node) to the appropriate position in x.child(parent of y and z)

	x.keys = append(x.keys[:i], Key{})
	copy(x.keys[i+1:], x.keys[i:])
	x.keys[i] = y.keys[t-1] // line84-86 inserts key(taken from y.keys) to x.keys

	z.keys = append(z.keys, y.keys[t:]...) // halve y.keys, move the second part to z.keys
	y.keys = y.keys[:t-1]                  // halve y.keys, and kept the first part

	if !y.leaf { // halve y.child, more the second half to z.child
		z.child = append(z.child, y.child[t:]...)
		y.child = y.child[:t]
	}
}

// Lookup searches for a key in the B-tree. Returns true if the key is found, along with the associated value.
func (bt *BTree) Lookup(key int) (bool, int) {
	return bt.lookupKey(bt.root, key)
}

// Helper function for key lookup
func (bt *BTree) lookupKey(node *BTreeNode, key int) (bool, int) {
	i := 0
	for i < len(node.keys) && key > node.keys[i].key {
		i++
	}

	if i < len(node.keys) && key == node.keys[i].key {
		// Key found in the current node
		return true, key
	} else if node.leaf {
		// Key not found in a leaf node
		return false, 0
	} else {
		// Recursively search in the appropriate child
		return bt.lookupKey(node.child[i], key)
	}
}

// Display prints out the structure of the B-tree under the specified node.
// 需要加工一下
func (bt *BTree) Display(node *BTreeNode, level int) {
	if node != nil {
		fmt.Printf("Level %d: ", level)
		for _, key := range node.keys {
			fmt.Printf("%d ", key)
		}
		fmt.Println()

		for _, child := range node.child {
			bt.Display(child, level+1)
		}
	}
}

// Print the B-tree in-order traversal
func (bt *BTree) inOrderTraversal(node *BTreeNode) {
	if node != nil {
		for i, key := range node.keys {
			if !node.leaf {
				bt.inOrderTraversal(node.child[i])
			}
			fmt.Print(key, " ")
		}
		if !node.leaf {
			bt.inOrderTraversal(node.child[len(node.keys)])
		}
	}
}

func main() {
	btree := NewBTree(3) // Create a B-tree with a minimum degree of 2

	keys := []int{50, 30, 70, 10, 40, 60, 80, 20, 45, 65, 35, 55, 75, 90}
	for _, key := range keys {
		btree.Insert(key, key*2)
	}

	fmt.Println("In-order traversal of B-tree:")
	btree.inOrderTraversal(btree.root)
}
