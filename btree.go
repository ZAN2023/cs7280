package main

type Key struct {
	Key, Val int
}

type Node struct {
	Leaf     bool
	Keys     []*Key
	Children []*Node
}

type BTree struct {
	Root *Node
	T    int
}

func NewKey(key, val int) *Key {
	return &Key{key, val}
}

func NewBTreeNode(leaf bool) *Node {
	return &Node{Leaf: leaf}
}

func NewBTree(t int) *BTree {
	return &BTree{
		Root: NewBTreeNode(true),
		T:    t,
	}
}

func (bt *BTree) Insert(key, val int) {
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

func (bt *BTree) insertNonFull(x *Node, key, val int) {
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

func (bt *BTree) splitChild(x *Node, idx int) {
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

func (bt *BTree) Find(key int) int {
	return bt.FindKey(bt.Root, key)
}

func (bt *BTree) FindKey(node *Node, key int) int {
	i := 0
	for i < len(node.Keys) && key > node.Keys[i].Key {
		i++
	}

	if i < len(node.Keys) && key == node.Keys[i].Key {
		// Key found in the current node
		return node.Keys[i].Val
	} else if node.Leaf {
		// Key not found in a leaf node
		return -1
	} else {
		// Recursively search in the appropriate children
		return bt.FindKey(node.Children[i], key)
	}
}
