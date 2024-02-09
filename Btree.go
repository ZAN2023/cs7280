package main

import (
    "fmt"
)

// BTreeNode is an interface that represents a node in the B-Tree
type BTreeNode interface {
    InsertNonFull(value int, t int)
    Split(childIndex int, t int) *Node
    IsLeaf() bool
    Keys() []int
    Insert(value int, t int)
    Search(value int) (BTreeNode, int, bool)
}

// Node represents a node in the B-Tree, which can be either an internal node or a leaf node
type Node struct {
    keys     []int
    children []BTreeNode
    leaf     bool
}

// BTree represents a B-Tree
type BTree struct {
    root *Node
    t    int // Minimum degree
}

// NewNode creates a new Node
func NewNode(leaf bool) *Node {
    return &Node{
        keys:     []int{},
        children: []BTreeNode{},
        leaf:     leaf,
    }
}

// NewBTree creates a new B-Tree with a given minimum degree
func NewBTree(t int) *BTree {
    root := NewNode(true)
    return &BTree{root: root, t: t}
}

// IsLeaf checks if the node is a leaf
func (n *Node) IsLeaf() bool {
    return n.leaf
}

// Keys returns the keys of the node
func (n *Node) Keys() []int {
    return n.keys
}

// InsertNonFull inserts a new key into a non-full node
func (n *Node) InsertNonFull(value int, t int) {
    i := len(n.keys) - 1
    if n.IsLeaf() {
        // Insert in a leaf node
        n.keys = append(n.keys, 0) // Ensure space for the new key
        for i >= 0 && value < n.keys[i] {
            n.keys[i+1] = n.keys[i]
            i--
        }
        n.keys[i+1] = value
    } else {
        // Insert in an internal node
        for i >= 0 && value < n.keys[i] {
            i--
        }
        i++
        if len(n.children[i].Keys()) == 2*t-1 {
            n.Split(i, t)
            if value > n.keys[i] {
                i++
            }
        }
        n.children[i].InsertNonFull(value, t)
    }
}

// Split splits the child of the node at childIndex
func (n *Node) Split(childIndex int, t int) *Node {
    child := n.children[childIndex].(*Node)
    newChild := NewNode(child.IsLeaf())
    newChild.keys = append(newChild.keys, child.keys[t:(2*t-1)]...)
    child.keys = child.keys[:t-1]

    if !child.IsLeaf() {
        newChild.children = append(newChild.children, child.children[t:(2*t)]...)
        child.children = child.children[:t]
    }

    n.children = append(n.children[:childIndex+1], append([]BTreeNode{newChild}, n.children[childIndex+1:]...)...)
    n.keys = append(n.keys[:childIndex], append([]int{child.keys[t-1]}, n.keys[childIndex:]...)...)

    return newChild
}

// Insert inserts a new key into the B-Tree
func (b *BTree) Insert(value int) {
    root := b.root
    if len(root.keys) == 2*b.t-1 {
        // Root is full, need to split
        newRoot := NewNode(false)
        b.root = newRoot
        newRoot.children = append(newRoot.children, root)
        newRoot.Split(0, b.t)
        newRoot.InsertNonFull(value, b.t)
    } else {
        root.InsertNonFull(value, b.t)
    }
}

// Search searches for a value in the B-Tree
func (n *Node) Search(value int) (BTreeNode, int, bool) {
    i := 0
    for i < len(n.keys) && value > n.keys[i] {
        i++
    }
    if i < len(n.keys) && value == n.keys[i] {
        return n, i, true
    }
    if n.IsLeaf() {
        return nil, -1, false
    }
    return n.children[i].Search(value)
}

func main() {
    t := 3 // Minimum degree
    btree := NewBTree(t)

    // Example inserts
    btree.Insert(10)
    btree.Insert(20)
    btree.Insert(5)
    btree.Insert(6)
    btree.Insert(12)
    btree.Insert(30)
    btree.Insert(7)
    btree.Insert(17)

    // Example search
    node, index, found := btree.root.Search(6)
    if found {
        fmt.Printf("Value %d found in node with keys: %v at index %d\n", 6, node.Keys(), index)
    } else {
        fmt.Println("Value not found")
    }
}
