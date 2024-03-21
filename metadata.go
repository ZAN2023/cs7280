package main

import (
	"bytes"
	"encoding/binary"
)

type MetaData struct {
	DBName    string
	FileNames []string
}

func NewMetaData(dbName string, fileNames []string) *MetaData {
	return &MetaData{
		DBName:    dbName,
		FileNames: fileNames,
	}
}

func (meta *MetaData) Serialize() []byte {
	var buf bytes.Buffer

	// 写入数据库名
	nameBytes := []byte(meta.DBName)
	binary.Write(&buf, binary.LittleEndian, int32(len(nameBytes)))
	buf.Write(nameBytes)

	// 写入文件名
	binary.Write(&buf, binary.LittleEndian, int32(len(meta.FileNames)))
	for _, fileName := range meta.FileNames {
		nameBytes := []byte(fileName)
		binary.Write(&buf, binary.LittleEndian, int32(len(nameBytes)))
		buf.Write(nameBytes)
	}

	return buf.Bytes()
}

func (meta *MetaData) Deserialize(data []byte) error {
	buf := bytes.NewBuffer(data)

	// 读取数据库名
	var nameLen int32
	binary.Read(buf, binary.LittleEndian, &nameLen)
	nameBytes := make([]byte, nameLen)
	buf.Read(nameBytes)
	meta.DBName = string(nameBytes)

	// 读取文件名
	var fileCount int32
	binary.Read(buf, binary.LittleEndian, &fileCount)
	meta.FileNames = make([]string, fileCount)
	for i := 0; i < int(fileCount); i++ {
		var nameLen int32
		binary.Read(buf, binary.LittleEndian, &nameLen)
		nameBytes := make([]byte, nameLen)
		buf.Read(nameBytes)
		meta.FileNames[i] = string(nameBytes)
	}

	return nil
}
