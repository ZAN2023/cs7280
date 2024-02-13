package main

import (
	"fmt"
)

/*
Key: struct that has k-v pair
*/

type Key struct {
	Key, Val int
}

/*
 Node struct for a node in the B-tree
 Leaf: boolean, representing whether the current node is a leaf
 Keys: a slice of keys in a node
 Children: a slice of the pointers for the node's children
*/

type Node struct {
	Leaf     bool
	Keys     []*Key
	Children []*Node
}

/*
 BTree_ struct represents the B-tree
 Root: root node ptr of B-tree
 T: minimum degree of the B-tree
 Each internal node can have at most 2t - 1 keys.
 Each internal node can have at most 2t children.
 Each internal node, except for the root, must have at least t - 1 keys.
 Each internal node, except for the root, must have at least t children.
*/

type BTree_ struct {
	Root *Node
	T    int
}

/*
 NewKey creates a new Key structure with the given key and val value
*/

func NewKey(key, val int) *Key {
	return &Key{key, val}
}

/*
 NewBTreeNode creates a new B-tree node
 The leaf argument indicates whether the new node is a leaf or not
*/

func NewBTreeNode(leaf bool) *Node {
	return &Node{Leaf: leaf}
}

/*
 NewBTree creates a new B-tree
 The t argument indicates the minimum degree for the new B-tree
 Each internal node can have at most 2t - 1 keys.
 Each internal node can have at most 2t children.
 Each internal node, except for the root, must have at least t - 1 keys.
 Each internal node, except for the root, must have at least t children.
*/

func NewBTree(t int) *BTree_ {
	return &BTree_{
		Root: NewBTreeNode(true),
		T:    t,
	}
}

/*
 Insert add the new key-val pair into the current B-tree
*/

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

/*
 insertNonFull add the new key-val pair into a node of the current B-tree that is not full yet
 The arguments include: x, the pointer to the node; the key-val pair to be inserted to the node
*/

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

/*
 * splitChild splits the node when it has been full
 * The arguments include: x, the pointer to the parent node, idx, the index of the child that is going to be split
 */
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

/*
 Lookup find the specified value by recursively search in the appropriate children
 If the key is found in the current node, it returns true
 If the key is not found when reaching the leaf node, it returns false
*/

func (bt *BTree_) Lookup(key int) bool {
	return bt.LookupKey(bt.Root, key)
}

/*
 LookupKey find the specified value by recursively search in the appropriate children
 This is the helper function for Lookup
*/

func (bt *BTree_) LookupKey(node *Node, key int) bool {
	i := 0
	for i < len(node.Keys) && key > node.Keys[i].Key {
		i++
	}

	if i < len(node.Keys) && key == node.Keys[i].Key {
		// Key found in the current node
		return true
	} else if node.Leaf {
		// Key not found in a leaf node
		return false
	} else {
		// Recursively search in the appropriate children
		return bt.LookupKey(node.Children[i], key)
	}
}

/*
 PrintTree prints the current B-tree starting from a given node
 The key-value pair is printed in the B-tree structure for displaying
*/

func (bt *BTree_) PrintTree(n *Node) {
	getKV := func(keys []*Key) (ans [][]int) {
		for _, key := range keys {
			ans = append(ans, []int{key.Key, key.Val})
		}
		return
	}

	q := []*Node{n}
	cnt := 0
	for len(q) > 0 {
		size := len(q)
		level := make([][][]int, 0)
		for i := 0; i < size; i++ {
			x := q[0]
			q = q[1:]
			level = append(level, getKV(x.Keys))
			cnt++
			for _, child := range x.Children {
				q = append(q, child)
			}
		}
		for i := 0; i < len(level); i++ {
			fmt.Print(level[i])
			if i != len(level)-1 {
				fmt.Print("---")
			}
		}
		fmt.Println()
	}
}

/*
 PrintTreeWithoutVal prints the current B-tree starting from a given node
 Only the key is printed in the B-tree structure for simpler identification
*/

func (bt *BTree_) PrintTreeWithoutVal(n *Node) {
	getK := func(keys []*Key) (ans []int) {
		for _, key := range keys {
			ans = append(ans, key.Key)
		}
		return
	}

	q := []*Node{n}
	cnt := 0
	for len(q) > 0 {
		size := len(q)
		level := make([][]int, 0)
		for i := 0; i < size; i++ {
			x := q[0]
			q = q[1:]
			level = append(level, getK(x.Keys))
			cnt++
			for _, child := range x.Children {
				q = append(q, child)
			}
		}
		for i := 0; i < len(level); i++ {
			fmt.Print(level[i])
			if i != len(level)-1 {
				fmt.Print("---")
			}
		}
		fmt.Println()
	}
}

/*
 Display prints the current B-tree starting from a specified node index
 Only key will be printed to represent the sub-tree structure
*/

func (bt *BTree_) Display(node int) {
	q := []*Node{bt.Root}
	cnt := 0
	for len(q) > 0 {
		size := len(q)
		for i := 0; i < size; i++ {
			x := q[0]
			q = q[1:]
			if cnt == node {
				bt.PrintTreeWithoutVal(x)
				return
			}
			cnt++
			for _, child := range x.Children {
				q = append(q, child)
			}
		}
		fmt.Println()
	}
}

func main() {
	bTree := NewBTree(3)

	keys := []int{34, 11, 76, 53, 29, 48, 65, 95, 81, 92, 68, 59, 87, 20, 45, 26, 83, 70, 37, 7, 17, 73, 42, 96, 23, 58, 8, 50, 94, 61, 39, 40, 41, 46}

	for _, key := range keys {
		bTree.Insert(key, key*2)
	}

	fmt.Println("-----------------B-tree structure------------------")
	bTree.PrintTreeWithoutVal(bTree.Root)
	fmt.Println()

	fmt.Println("-----------------Test Lookup-------------------------")
	fmt.Print("look up 53: ")
	fmt.Println(bTree.Lookup(53))

	fmt.Print("look up 68: ")
	fmt.Println(bTree.Lookup(68))

	fmt.Print("look up 3999: ")
	fmt.Println(bTree.Lookup(3999))
	fmt.Println()

	fmt.Println("----------B-tree structure with value--------------")
	bTree.PrintTree(bTree.Root)
	fmt.Println()

	fmt.Println("---------------Test Display function----------------")
	bTree.Display(1)
	// fmt.Println()
	// fmt.Println("2")
	// bTree.Display(2)
	// fmt.Println("3")
	// bTree.Display(3)

}
