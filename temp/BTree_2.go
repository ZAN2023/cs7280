package main

import (
	"container/list"
	"fmt"
)

// BTreeNode represents a node in the B-tree
type BTreeNode struct {
	Leaf     bool
	Keys     []int
	Children []*BTreeNode
}

// BTree_ represents the B-tree
type BTree_ struct {
	Root *BTreeNode
	T    int // Minimum degree
}

// NewBTreeNode creates a new B-tree node
func NewBTreeNode(leaf bool) *BTreeNode {
	return &BTreeNode{
		Leaf:     leaf,
		Keys:     make([]int, 0),
		Children: make([]*BTreeNode, 0),
	}
}

// NewBTree creates a new B-tree
func NewBTree(t int) *BTree_ {
	return &BTree_{
		Root: NewBTreeNode(true),
		T:    t,
	}
}

// Insert inserts a key into the B-tree
func (bt *BTree_) Insert(key int) {
	r := bt.Root
	if len(r.Keys) == (2*bt.T - 1) {
		s := NewBTreeNode(false)
		bt.Root = s
		s.Children = append(s.Children, r)
		bt.splitChild(s, 0)
		bt.insertNonFull(s, key)
	} else {
		bt.insertNonFull(r, key)
	}
}

// insertNonFull inserts a key into a non-full B-tree node
func (bt *BTree_) insertNonFull(x *BTreeNode, key int) {
	i := len(x.Keys) - 1
	if x.Leaf {
		for i >= 0 && key < x.Keys[i] {
			i--
		}
		x.Keys = append(x.Keys, 0)
		copy(x.Keys[i+2:], x.Keys[i+1:])
		x.Keys[i+1] = key
	} else {
		for i >= 0 && key < x.Keys[i] {
			i--
		}
		i++
		if len(x.Children[i].Keys) == (2*bt.T - 1) {
			bt.splitChild(x, i)
			if key > x.Keys[i] {
				i++
			}
		}
		bt.insertNonFull(x.Children[i], key)
	}
}

// splitChild splits a full child node of a B-tree node
func (bt *BTree_) splitChild(x *BTreeNode, i int) {
	y := x.Children[i]
	z := NewBTreeNode(y.Leaf)

	x.Children = append(x.Children, nil)
	copy(x.Children[i+2:], x.Children[i+1:])
	x.Children[i+1] = z

	x.Keys = append(x.Keys, 0)
	copy(x.Keys[i+1:], x.Keys[i:])
	x.Keys[i] = y.Keys[bt.T-1]

	z.Keys = append(z.Keys, y.Keys[bt.T:]...)
	y.Keys = y.Keys[:bt.T-1]

	if !y.Leaf {
		z.Children = append(z.Children, y.Children[bt.T:]...)
		y.Children = y.Children[:bt.T]
	}
}

// PrintTree prints the B-tree structure level by level
func (bt *BTree_) PrintTree(node *BTreeNode) {
	if node == nil {
		return
	}

	queue := list.New()
	queue.PushBack(node)

	for queue.Len() > 0 {
		size := queue.Len()
		for i := 0; i < size; i++ {
			element := queue.Front()
			queue.Remove(element)
			x := element.Value.(*BTreeNode)

			if x != nil {
				// Print keys
				for _, key := range x.Keys {
					fmt.Printf("%d ", key)
				}
				fmt.Print("---")

				// Enqueue children
				if !x.Leaf {
					for _, child := range x.Children {
						queue.PushBack(child)
					}
				}
			}
		}
		fmt.Println()
	}
}

func main() {
	bTree := NewBTree(3)

	keys := []int{34, 11, 76, 53, 29, 48, 65, 95, 81, 92, 68, 59, 87, 20, 45, 26, 83, 70, 37, 7, 17, 73, 42, 96, 23, 58, 8, 50, 94, 61}

	for _, key := range keys {
		bTree.Insert(key)
	}

	fmt.Println("B-tree structure:")
	bTree.PrintTree(bTree.Root)
}
