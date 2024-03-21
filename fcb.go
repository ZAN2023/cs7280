package main

import (
	"bytes"
	"encoding/binary"
)

type FCB struct {
	FileName    string
	IndexBlocks []int
	DataBlocks  []int
	IndexTree   *BTree
}

func NewFCB(fileName string, indexBlocks, dataBlocks []int) *FCB {
	return &FCB{
		FileName:    fileName,
		IndexBlocks: indexBlocks,
		DataBlocks:  dataBlocks,
		IndexTree:   NewBTree(3), // 假设B树的阶数为3
	}
}

func (fcb *FCB) Serialize() []byte {
	var buf bytes.Buffer

	// 写入文件名
	nameBytes := []byte(fcb.FileName)
	binary.Write(&buf, binary.LittleEndian, int32(len(nameBytes)))
	buf.Write(nameBytes)

	// 写入索引块
	binary.Write(&buf, binary.LittleEndian, int32(len(fcb.IndexBlocks)))
	for _, block := range fcb.IndexBlocks {
		binary.Write(&buf, binary.LittleEndian, int32(block))
	}

	// 写入数据块
	binary.Write(&buf, binary.LittleEndian, int32(len(fcb.DataBlocks)))
	for _, block := range fcb.DataBlocks {
		binary.Write(&buf, binary.LittleEndian, int32(block))
	}

	// 序列化B树索引
	indexBytes := fcb.serializeBTree(fcb.IndexTree.Root)
	binary.Write(&buf, binary.LittleEndian, int32(len(indexBytes)))
	buf.Write(indexBytes)

	return buf.Bytes()
}

func (fcb *FCB) serializeBTree(node *Node) []byte {
	var buf bytes.Buffer

	// 写入节点类型
	var nodeType int32
	if node.Leaf {
		nodeType = 1
	} else {
		nodeType = 0
	}
	binary.Write(&buf, binary.LittleEndian, nodeType)

	// 写入键的数量
	binary.Write(&buf, binary.LittleEndian, int32(len(node.Keys)))

	// 写入键
	for _, key := range node.Keys {
		binary.Write(&buf, binary.LittleEndian, int32(key.Key))
		binary.Write(&buf, binary.LittleEndian, int32(key.Val))
	}

	// 递归序列化子节点
	if !node.Leaf {
		for _, child := range node.Children {
			childBytes := fcb.serializeBTree(child)
			binary.Write(&buf, binary.LittleEndian, int32(len(childBytes)))
			buf.Write(childBytes)
		}
	}

	return buf.Bytes()
}

func (fcb *FCB) Deserialize(data []byte) error {
	buf := bytes.NewBuffer(data)

	// 读取文件名
	var nameLen int32
	binary.Read(buf, binary.LittleEndian, &nameLen)
	nameBytes := make([]byte, nameLen)
	buf.Read(nameBytes)
	fcb.FileName = string(nameBytes)

	// 读取索引块
	var indexBlockCount int32
	binary.Read(buf, binary.LittleEndian, &indexBlockCount)
	fcb.IndexBlocks = make([]int, indexBlockCount)
	for i := 0; i < int(indexBlockCount); i++ {
		var block int32
		binary.Read(buf, binary.LittleEndian, &block)
		fcb.IndexBlocks[i] = int(block)
	}

	// 读取数据块
	var dataBlockCount int32
	binary.Read(buf, binary.LittleEndian, &dataBlockCount)
	fcb.DataBlocks = make([]int, dataBlockCount)
	for i := 0; i < int(dataBlockCount); i++ {
		var block int32
		binary.Read(buf, binary.LittleEndian, &block)
		fcb.DataBlocks[i] = int(block)
	}

	// 反序列化B树索引
	var indexLen int32
	binary.Read(buf, binary.LittleEndian, &indexLen)
	indexBytes := make([]byte, indexLen)
	buf.Read(indexBytes)
	fcb.IndexTree = NewBTree(3) // 假设B树的阶数为3
	fcb.IndexTree.Root = fcb.deserializeBTree(indexBytes)

	return nil
}

func (fcb *FCB) deserializeBTree(data []byte) *Node {
	buf := bytes.NewBuffer(data)

	// 读取节点类型
	var nodeType int32
	binary.Read(buf, binary.LittleEndian, &nodeType)
	leaf := nodeType == 1

	// 读取键的数量
	var keyCount int32
	binary.Read(buf, binary.LittleEndian, &keyCount)

	// 读取键
	keys := make([]*Key, keyCount)
	for i := 0; i < int(keyCount); i++ {
		var key, val int32
		binary.Read(buf, binary.LittleEndian, &key)
		binary.Read(buf, binary.LittleEndian, &val)
		keys[i] = NewKey(int(key), int(val))
	}

	node := &Node{
		Leaf: leaf,
		Keys: keys,
	}

	// 递归反序列化子节点
	if !leaf {
		childCount := keyCount + 1
		node.Children = make([]*Node, childCount)
		for i := 0; i < int(childCount); i++ {
			var childLen int32
			binary.Read(buf, binary.LittleEndian, &childLen)
			childBytes := make([]byte, childLen)
			buf.Read(childBytes)
			node.Children[i] = fcb.deserializeBTree(childBytes)
		}
	}

	return node
}
