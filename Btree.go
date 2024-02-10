package main

import (
	"fmt"
)

// reference: https://www.bilibili.com/video/BV1Jh411q7xP/?spm_id_from=333.337.search-card.all.click&vd_source=b9a995f049da13fe81c8e1e2c914a92e

type Key struct {
	key, val int
}

// BTreeNode represents a node in the B-tree
type BTreeNode struct {
	leaf     bool
	keys     []Key
	children []*BTreeNode
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

//t = 3
//insert 8
//      5
//  2 3    7 8, 9

// Insert a key into the B-tree
func (bt *BTree) Insert(key, val int) {
	root := bt.root
	if len(root.keys) == (2*bt.t)-1 {
		newRoot := &BTreeNode{}
		bt.root = newRoot
		newRoot.children = append(newRoot.children, root)
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
		if len(x.children[i].keys) == (2*bt.t)-1 {
			bt.splitChild(x, i) // After splitting, the median key from the full children x.children[i] is moved up to the parent x at index i.
			if key > x.keys[i].key {
				i++
			}
		}
		bt.insertNonFull(x.children[i], key, val)
	}
}

// Split a children node of the B-tree
// x: parent node, i: index of the children to be split.
func (bt *BTree) splitChild(x *BTreeNode, i int) {
	t := bt.t                     // minimum degree
	y := x.children[i]            // y is the node to be split
	z := &BTreeNode{leaf: y.leaf} // a new node (right sibling of y, to put values split from y)

	x.children = append(x.children[:i+1], nil)
	copy(x.children[i+2:], x.children[i+1:])
	x.children[i+1] = z // line79-81 inserts z(the new node) to the appropriate position in x.children(parent of y and z)

	x.keys = append(x.keys[:i], Key{})
	copy(x.keys[i+1:], x.keys[i:])
	x.keys[i] = y.keys[t-1] // line84-86 inserts key(taken from y.keys) to x.keys

	z.keys = append(z.keys, y.keys[t:]...) // halve y.keys, move the second part to z.keys
	y.keys = y.keys[:t-1]                  // halve y.keys, and kept the first part

	if !y.leaf { // halve y.children, move the second half to z.children
		z.children = append(z.children, y.children[t:]...)
		y.children = y.children[:t]
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
		// Recursively search in the appropriate children
		return bt.lookupKey(node.children[i], key)
	}
}

// nodes = [0x121212,0x121216,0x12124232,0x12134342,0x12134342]
//
// 0x121212
// 0x121216
// 0x12124232
// 0x12134342
// 0x12134342
// [1, 2, 3, 4]
// [x, x, x, x, x, x, x]
//
//	x
//
// x    x
// x x  x  x
// Display prints out the structure of the B-tree under the specified node.
// 需要加工一下, 没太明白要怎么display
func (bt *BTree) Display(node *BTreeNode, level int) {
	if node != nil {
		fmt.Printf("Level %d: ", level)
		for _, key := range node.keys {
			fmt.Printf("%d ", key)
		}
		fmt.Println()

		for _, child := range node.children {
			bt.Display(child, level+1)
		}
	}
}

// Print the B-tree in-order traversal
func (bt *BTree) inOrderTraversal(node *BTreeNode) {
	if node != nil {
		for i, key := range node.keys {
			if !node.leaf {
				bt.inOrderTraversal(node.children[i])
			}
			fmt.Print(key, " ")
		}
		if !node.leaf {
			bt.inOrderTraversal(node.children[len(node.keys)])
		}
	}
}

func main() {
	btree := NewBTree(7) // Create a B-tree with a minimum degree of 2

	keys := []int{29, 41, 44, 62, 46, 49, 27, 76, 91, 30, 100, 47, 34, 53, 9, 45}
	for _, key := range keys {
		btree.Insert(key, key*2)
	}

	fmt.Println("In-order traversal of B-tree:")
	btree.inOrderTraversal(btree.root)
	fmt.Println()

	fmt.Println("Find if key 100 exist")
	fmt.Println(btree.Lookup(100))

	fmt.Println("Find if key 99 exist")
	fmt.Println(btree.Lookup(99))

	fmt.Println("Find if key 36 exist")
	fmt.Println(btree.Lookup(36))
}
