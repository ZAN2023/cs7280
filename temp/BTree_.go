package main

import "fmt"

type Node struct {
	leaf     bool
	keys     []int
	children []*Node
}

type BTree struct {
	root *Node
	t    int // for each non-root node, t-1 <= len(keys) <= 2*t-1
}

func NewTree(t int) *BTree {
	return &BTree{root: &Node{leaf: true}, t: t}
}

func (bt *BTree) Insert(key int) {
	root, t := bt.root, bt.t
	if len(root.keys) == (2*t - 1) {
		rootN := &Node{}
		bt.root = rootN
		rootN.children = append(rootN.children, root)
		bt.splitChild(rootN, 0)
		bt.insertNonFull(rootN, key)
	} else {
		bt.insertNonFull(root, key)
	}
}

func (bt *BTree) splitChild(x *Node, idx int) {
	from := x.children[idx]
	dest := &Node{leaf: from.leaf}

	// insert new node:from to n.children at pos idx+1, and shift eles from idx+1 to right by 1
	tmp := make([]*Node, 0)
	tmp = append(tmp, x.children[:idx+1]...)
	tmp = append(tmp, dest)
	tmp = append(tmp, x.children[idx+1:]...)
	x.children = tmp
	//tmp := append([]*Node{}, x.children[idx+1:]...)
	//x.children = x.children[:idx+1]
	//x.children = append(x.children, dest)
	//x.children = append(x.children, tmp...)

	// promote the key at pos:t-1 from node:from to x.keys at pos:idx
	tmp2 := make([]int, 0)
	tmp2 = append(tmp2, x.keys[:idx]...)
	tmp2 = append(tmp2, from.keys[bt.t-1])
	tmp2 = append(tmp2, x.keys[idx:]...)
	x.keys = tmp2
	//tmp2 := append([]int{}, x.keys[idx:]...)
	//x.keys = x.keys[:idx]
	//x.keys = append(x.keys, from.keys[bt.t-1])
	//x.keys = append(x.keys, tmp2...)

	// move keys from idx:t in node:from to node:dest
	dest.keys = append(dest.keys, from.keys[bt.t:]...)
	// remove keys from idx:t-1 in node:from
	from.keys = from.keys[:bt.t-1]

	if !from.leaf {
		// move half children from node:from to node:dest
		dest.children = append(dest.children, from.children[bt.t:]...)
		// clear the half children that were just moved in node:from
		from.children = from.children[:bt.t]
	}
}

func (bt *BTree) insertNonFull(x *Node, key int) {
	idx := len(x.keys) - 1
	if x.leaf {
		for idx >= 0 && key < x.keys[idx] {
			idx--
		}
		tmp := make([]int, 0)
		tmp = append(tmp, x.keys[:idx+1]...)
		tmp = append(tmp, key)
		tmp = append(tmp, x.keys[idx+1:]...)
		x.keys = tmp
		//tmp := append([]int{}, x.keys[idx+1:]...)
		//x.keys = x.keys[:idx+1]
		//x.keys = append(x.keys, key)
		//x.keys = append(x.keys, tmp...)
	} else {
		for idx >= 0 && key < x.keys[idx] {
			idx--
		}
		idx++
		if len(x.children[idx].keys) == (2*bt.t - 1) {
			bt.splitChild(x, idx)
			if key > x.keys[idx] {
				idx++
			}
		}
		bt.insertNonFull(x.children[idx], key)
	}
}

// Lookup searches for a key in the B-tree. Returns true if the key is found, along with the associated value.
func (bt *BTree) Lookup(key int) (bool, int) {
	return bt.lookupKey(bt.root, key)
}

// Helper function for key lookup
func (bt *BTree) lookupKey(node *Node, key int) (bool, int) {
	i := 0
	for i < len(node.keys) && key > node.keys[i] {
		i++
	}

	if i < len(node.keys) && key == node.keys[i] {
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

func (bt *BTree) PrintTree(n *Node) {
	q := []*Node{n}
	for len(q) > 0 {
		size := len(q)
		for i := 0; i < size; i++ {
			x := q[0]
			q = q[1:]
			fmt.Print(x.keys, "------")
			for _, child := range x.children {
				q = append(q, child)
			}
		}
		fmt.Println()
	}
}

func main() {
	btree := NewTree(2) // Create a B-tree with a minimum degree of 2

	keys := []int{29, 41, 44, 62, 46, 49, 27, 76, 91, 30, 100, 47, 34, 53, 9, 45}
	//keys := []int{29, 41, 44, 62, 46}
	for _, key := range keys {
		btree.Insert(key)
	}

	btree.PrintTree(btree.root)
}
