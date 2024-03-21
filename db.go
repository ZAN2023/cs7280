package main

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strconv"
)

const (
	BlockSize      = 256
	MaxValueSize   = 40
	FileNameSuffix = ".db"
	MetaDataBlocks = 1
	BitmapBlocks   = 2
)

type Entry struct {
	Key   int
	Value string
}

type DB struct {
	Name   string
	Num    int
	Bitmap []bool
	FCBs   []*FCB
	Blocks []Block
}

type Block struct {
	Data      [BlockSize]byte
	NextBlock int
}

func openDB(dbName string) *DB {
	dbPath := dbName + FileNameSuffix + "0"
	if _, err := os.Stat(dbPath); os.IsNotExist(err) {
		return newDB(dbName, 0)
	} else {
		data, err := ioutil.ReadFile(dbPath)
		if err != nil {
			panic(err)
		}

		db := &DB{
			Name: dbName,
		}

		db.Blocks = make([]Block, 4096)
		for i := 0; i < 4096; i++ {
			copy(db.Blocks[i].Data[:], data[i*BlockSize:(i+1)*BlockSize])
		}

		db.Bitmap = make([]bool, 4096)
		for i := 0; i < 4096; i++ {
			if i < 2048 {
				db.Bitmap[i] = (db.Blocks[MetaDataBlocks].Data[i/8] & (1 << (i % 8))) != 0
			} else {
				db.Bitmap[i] = (db.Blocks[MetaDataBlocks+1].Data[(i-2048)/8] & (1 << ((i - 2048) % 8))) != 0
			}
		}

		metaDataBytes := db.Blocks[0].Data
		metaData := &MetaData{}
		metaData.Deserialize(metaDataBytes[:])

		db.Name = metaData.DBName
		db.FCBs = make([]*FCB, len(metaData.FileNames))
		for i, fileName := range metaData.FileNames {
			fcb := &FCB{}
			err := fcb.Deserialize(db.Blocks[MetaDataBlocks+BitmapBlocks+i].Data[:])
			if err != nil {
				panic(err)
			}
			if fcb.FileName != fileName {
				panic("FCB file name does not match metadata")
			}
			db.FCBs[i] = fcb
		}

		return db
	}
}

func (db *DB) put(fileName string) error {
	db := newDB(db.Name)
	data, err := ioutil.ReadFile(fileName)
	if err != nil {
		return err
	}

	entries := make([]Entry, 0)
	lines := bytes.Split(data, []byte{'\n'})
	for i, line := range lines {
		entries = append(entries, Entry{
			Key:   i,
			Value: string(line[:min(len(line), MaxValueSize)]),
		})
	}
	// TODO 这里有问题，计算需要的block数量要用entry数量计算，不是data的大小
	indexTree := NewBTree(3)
	indexBlocks := make([]int, 0)
	for _, entry := range entries {
		indexTree.Insert(entry.Key, entry.Key)
	}

	emptyBlocks := db.countEmptyBlock()

	// ------------
	// 计算需要的数据块数量
	dataBlocks := make([]int, 0)
	remainingBytes := len(data)
	for remainingBytes > 0 {
		blockNo := db.allocBlock()
		if blockNo == -1 {
			// 当前数据库文件已满,创建新的数据库文件
			db.writeDBToDisk()
			newDBName := fmt.Sprintf("%s%s%d", db.Name, FileNameSuffix, len(db.Blocks)/4096)
			newDB := &DB{
				Name:   newDBName,
				Bitmap: make([]bool, 4096),
				FCBs:   make([]*FCB, 0),
				Blocks: make([]Block, 4096),
			}
			for i := 0; i < MetaDataBlocks+BitmapBlocks; i++ {
				newDB.Bitmap[i] = true
			}
			db = newDB
			blockNo = db.allocBlock()
		}
		dataBlocks = append(dataBlocks, blockNo)
		remainingBytes -= BlockSize
	}

	// 写入数据块
	remainingBytes = len(data)
	for _, blockNo := range dataBlocks {
		if remainingBytes <= 0 {
			break
		}
		copy(db.Blocks[blockNo].Data[:], data[len(data)-remainingBytes:])
		remainingBytes -= BlockSize
	}

	// 创建索引
	indexTree := NewBTree(3)
	indexBlocks := make([]int, 0)
	for _, entry := range entries {
		indexTree.Insert(entry.Key, entry.Key)
	}

	// 序列化索引树
	indexData := indexTree.Serialize()

	// 计算索引块数量并写入
	remainingBytes = len(indexData)
	for remainingBytes > 0 {
		blockNo := db.allocBlock()
		if blockNo == -1 {
			// 当前数据库文件已满,创建新的数据库文件
			db.writeDBToDisk()
			newDBName := fmt.Sprintf("%s%s%d", db.Name, FileNameSuffix, len(db.Blocks)/4096)
			newDB := &DB{
				Name:   newDBName,
				Bitmap: make([]bool, 4096),
				FCBs:   make([]*FCB, 0),
				Blocks: make([]Block, 4096),
			}
			for i := 0; i < MetaDataBlocks+BitmapBlocks; i++ {
				newDB.Bitmap[i] = true
			}
			db = newDB
			blockNo = db.allocBlock()
		}
		indexBlocks = append(indexBlocks, blockNo)
		if remainingBytes >= BlockSize {
			copy(db.Blocks[blockNo].Data[:], indexData[len(indexData)-remainingBytes:len(indexData)-remainingBytes+BlockSize])
		} else {
			copy(db.Blocks[blockNo].Data[:], indexData[len(indexData)-remainingBytes:])
		}
		remainingBytes -= BlockSize
	}

	fcb := NewFCB(fileName, indexBlocks, dataBlocks)
	fcb.IndexTree = indexTree
	db.FCBs = append(db.FCBs, fcb)

	err = db.writeDBToDisk()
	if err != nil {
		return err
	}

	return nil
}

func (db *DB) get(fileName string) ([]byte, error) {
	for _, fcb := range db.FCBs {
		if fcb.FileName == fileName {
			data := make([]byte, 0)
			for _, block := range fcb.DataBlocks {
				data = append(data, db.Blocks[block].Data[:]...)
				if db.Blocks[block].NextBlock == 0 {
					break
				}
				block = db.Blocks[block].NextBlock
			}
			return data, nil
		}
	}
	return nil, fmt.Errorf("File not found: %s", fileName)
}

func (db *DB) find(fileName string, key int) (string, int, error) {
	for _, fcb := range db.FCBs {
		if fcb.FileName == fileName {
			block := fcb.IndexTree.Find(key)
			if block == -1 {
				return "", 0, fmt.Errorf("Key not found: %d", key)
			}
			for i := 0; i < len(fcb.DataBlocks); i++ {
				blockData := db.Blocks[fcb.DataBlocks[i]].Data
				for j := 0; j < BlockSize; j += MaxValueSize + 4 {
					if bytesToInt(blockData[j:j+4]) == key {
						return string(blockData[j+4 : j+4+MaxValueSize]), i + 1, nil
					}
				}
			}
		}
	}
	return "", 0, fmt.Errorf("File not found: %s", fileName)
}

func dir() []string {
	files, _ := filepath.Glob("*" + FileNameSuffix + "*")
	return files
}

func kill(dbName string) error {
	files, _ := filepath.Glob(dbName + FileNameSuffix + "*")
	for _, file := range files {
		err := os.Remove(file)
		if err != nil {
			return err
		}
	}
	return nil
}

func newDB(dbName string, num int) *DB {
	db := &DB{
		Name:   dbName,
		Num:    num,
		Bitmap: make([]bool, 4096),
		FCBs:   make([]*FCB, 0),
		Blocks: make([]Block, 4096),
	}
	for i := 0; i < MetaDataBlocks+BitmapBlocks; i++ {
		db.Bitmap[i] = true
	}
	err = db.writeDBToDisk(num)
	if err != nil {
		panic(err)
	}
	return db
}

func (db *DB) allocBlock() int {
	for i := 0; i < len(db.Bitmap); i++ {
		if !db.Bitmap[i] {
			db.Bitmap[i] = true
			return i
		}
	}
	return -1
}

func (db *DB) countEmptyBlock() int {
	cnt := 0
	for i := 0; i < len(db.Bitmap); i++ {
		if !db.Bitmap[i] {
			cnt++
		}
	}
	return cnt
}

func (db *DB) freeBlock(block int) {
	db.Bitmap[block] = false
}

func intToBytes(i int) []byte {
	return []byte{
		byte(i >> 24),
		byte(i >> 16),
		byte(i >> 8),
		byte(i),
	}
}

func bytesToInt(b []byte) int {
	return int(b[0])<<24 | int(b[1])<<16 | int(b[2])<<8 | int(b[3])
}

func (db *DB) writeDBToDisk(num int) error {
	bitmap := make([]byte, 4096/8)
	for i := 0; i < 4096; i++ {
		if db.Bitmap[i] {
			bitmap[i/8] |= 1 << (i % 8)
		}
	}

	metaData := &MetaData{
		DBName:    db.Name,
		FileNames: make([]string, len(db.FCBs)),
	}
	for i, fcb := range db.FCBs {
		metaData.FileNames[i] = fcb.FileName
	}
	metaDataBytes := metaData.Serialize()

	data := make([]byte, 0)
	data = append(data, metaDataBytes...)
	data = append(data, bitmap...)
	for _, fcb := range db.FCBs {
		data = append(data, fcb.Serialize()...)
	}
	for _, block := range db.Blocks {
		data = append(data, block.Data[:]...)
	}

	dbPath := db.Name + FileNameSuffix + strconv.Itoa(num)
	err := ioutil.WriteFile(dbPath, data, 0644)
	if err != nil {
		return err
	}

	return nil
}
