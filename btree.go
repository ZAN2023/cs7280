package main

import (
	"bytes"
	"encoding/binary"
)

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

func (bt *BTree) Serialize() []byte {
	var buf bytes.Buffer
	bt.serializeNode(&buf, bt.Root)
	return buf.Bytes()
}

func (bt *BTree) serializeNode(buf *bytes.Buffer, node *Node) {
	if node == nil {
		return
	}
	buf.WriteByte(byte(len(node.Keys)))
	for _, key := range node.Keys {
		binary.Write(buf, binary.LittleEndian, int32(key.Key))
		binary.Write(buf, binary.LittleEndian, int32(key.Val))
	}
	buf.WriteByte(byte(len(node.Children)))
	for _, child := range node.Children {
		bt.serializeNode(buf, child)
	}
}

func (bt *BTree) Deserialize(data []byte) {
	buf := bytes.NewBuffer(data)
	bt.Root = bt.deserializeNode(buf)
}

func (bt *BTree) deserializeNode(buf *bytes.Buffer) *Node {
	numKeys, _ := buf.ReadByte()
	node := NewBTreeNode(numKeys == 0)
	for i := 0; i < int(numKeys); i++ {
		var key, val int32
		binary.Read(buf, binary.LittleEndian, &key)
		binary.Read(buf, binary.LittleEndian, &val)
		node.Keys = append(node.Keys, NewKey(int(key), int(val)))
	}
	numChildren, _ := buf.ReadByte()
	for i := 0; i < int(numChildren); i++ {
		node.Children = append(node.Children, bt.deserializeNode(buf))
	}
	return node
}
