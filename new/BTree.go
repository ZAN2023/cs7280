package main

import (
	"fmt"
)

// Key represents a k-v pair
type Key struct {
	Key, Val int
}

// Node represents a node in the B-tree
type Node struct {
	Leaf     bool
	Keys     []*Key
	Children []*Node
}

// BTree_ represents the B-tree
type BTree_ struct {
	Root *Node
	T    int // Minimum degree
}

// NewKey creates a new Key struct
func NewKey(key, val int) *Key {
	return &Key{key, val}
}

// NewBTreeNode creates a new B-tree node
func NewBTreeNode(leaf bool) *Node {
	return &Node{Leaf: leaf}
}

// NewBTree creates a new B-tree
func NewBTree(t int) *BTree_ {
	return &BTree_{
		Root: NewBTreeNode(true),
		T:    t,
	}
}

// Insert inserts a key into the B-tree
func (bt *BTree_) Insert(key, val int) {
	target := bt.Root
	if len(target.Keys) == (2*bt.T - 1) {
		dest := NewBTreeNode(false)
		bt.Root = dest
		dest.Children = append(dest.Children, target)
		bt.splitChild(dest, 0)
		bt.insertNonFull(dest, key, val)
	} else {
		bt.insertNonFull(target, key, val)
	}
}

// insertNonFull inserts a key into a non-full B-tree node
func (bt *BTree_) insertNonFull(x *Node, key, val int) {
	idx := len(x.Keys) - 1
	if x.Leaf {
		for idx >= 0 && key < x.Keys[idx].Key {
			idx--
		}
		x.Keys = append(x.Keys, nil)
		copy(x.Keys[idx+2:], x.Keys[idx+1:])
		x.Keys[idx+1] = NewKey(key, val)
	} else {
		for idx >= 0 && key < x.Keys[idx].Key {
			idx--
		}
		idx++
		if len(x.Children[idx].Keys) == (2*bt.T - 1) {
			bt.splitChild(x, idx)
			if key > x.Keys[idx].Key {
				idx++
			}
		}
		bt.insertNonFull(x.Children[idx], key, val)
	}
}

// splitChild splits a full child node of a B-tree node
func (bt *BTree_) splitChild(x *Node, idx int) {
	y := x.Children[idx]
	z := NewBTreeNode(y.Leaf)

	x.Children = append(x.Children, nil)
	copy(x.Children[idx+2:], x.Children[idx+1:])
	x.Children[idx+1] = z

	x.Keys = append(x.Keys, nil)
	copy(x.Keys[idx+1:], x.Keys[idx:])
	x.Keys[idx] = y.Keys[bt.T-1]

	z.Keys = append(z.Keys, y.Keys[bt.T:]...)
	y.Keys = y.Keys[:bt.T-1]

	if !y.Leaf {
		z.Children = append(z.Children, y.Children[bt.T:]...)
		y.Children = y.Children[:bt.T]
	}
}

func (bt *BTree_) LookupKey(node *Node, key int) (bool, int) {
	i := 0
	for i < len(node.Keys) && key > node.Keys[i].Key {
		i++
	}

	if i < len(node.Keys) && key == node.Keys[i].Key {
		// Key found in the current node
		return true, key
	} else if node.Leaf {
		// Key not found in a leaf node
		return false, 0
	} else {
		// Recursively search in the appropriate children
		return bt.LookupKey(node.Children[i], key)
	}
}

func (bt *BTree_) PrintTree(n *Node) {
	getVals := func(keys []*Key) (ans [][]int) {
		for _, key := range keys {
			ans = append(ans, []int{key.Key, key.Val})
		}
		return
	}
	q := []*Node{n}
	for len(q) > 0 {
		size := len(q)
		for i := 0; i < size; i++ {
			x := q[0]
			q = q[1:]
			fmt.Print(getVals(x.Keys), "------")
			for _, child := range x.Children {
				q = append(q, child)
			}
		}
		fmt.Println()
	}
}

func (bt *BTree_) PrintTreeWithoutVal(n *Node) {
	getVals := func(keys []*Key) (ans []int) {
		for _, key := range keys {
			ans = append(ans, key.Key)
		}
		return
	}
	q := []*Node{n}
	cnt := 0
	for len(q) > 0 {
		size := len(q)
		for i := 0; i < size; i++ {
			x := q[0]
			q = q[1:]
			fmt.Print(cnt, ":", getVals(x.Keys), "------")
			cnt++
			for _, child := range x.Children {
				q = append(q, child)
			}
		}
		fmt.Println()
	}
}

func main() {
	bTree := NewBTree(4)

	keys := []int{34, 11, 76, 53, 29, 48, 65, 95, 81, 92, 68, 59, 87, 20, 45, 26, 83, 70, 37, 7, 17, 73, 42, 96, 23, 58, 8, 50, 94, 61, 39, 40, 41, 46}

	for _, key := range keys {
		bTree.Insert(key, key*2)
	}

	fmt.Println("B-tree structure:")
	bTree.PrintTreeWithoutVal(bTree.Root)
	fmt.Println()
	fmt.Println()
	fmt.Println()

	fmt.Print("look up 53: ")
	fmt.Println(bTree.LookupKey(bTree.Root, 53))

	fmt.Print("look up 68: ")
	fmt.Println(bTree.LookupKey(bTree.Root, 68))

	fmt.Print("look up 3999:")
	fmt.Println(bTree.LookupKey(bTree.Root, 3999))
}
