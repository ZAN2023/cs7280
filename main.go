package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
)

func main() {
	var db *DB
	scanner := bufio.NewScanner(os.Stdin)

	for scanner.Scan() {
		cmd := scanner.Text()
		tokens := strings.Fields(cmd)

		if len(tokens) == 0 {
			continue
		}

		switch tokens[0] {
		case "open":
			if len(tokens) != 2 {
				fmt.Println("Usage: open <db_name>")
				continue
			}
			db = openDB(tokens[1])
		case "put":
			if len(tokens) != 2 || db == nil {
				fmt.Println("Usage: put <local_file>")
				continue
			}
			err := db.put(tokens[1])
			if err != nil {
				fmt.Printf("Put error: %v\n", err)
			}
		case "get":
			if len(tokens) != 2 || db == nil {
				fmt.Println("Usage: get <local_file>")
				continue
			}
			data, err := db.get(tokens[1])
			if err != nil {
				fmt.Printf("Get error: %v\n", err)
			} else {
				fmt.Println(string(data))
			}
		case "dir":
			files := dir()
			for _, file := range files {
				fmt.Println(file)
			}
		case "find":
			if len(tokens) != 3 || db == nil {
				fmt.Println("Usage: find <local_file> <key>")
				continue
			}
			key, err := strconv.Atoi(tokens[2])
			if err != nil {
				fmt.Printf("Invalid key: %v\n", err)
				continue
			}
			value, blocks, err := db.find(tokens[1], key)
			if err != nil {
				fmt.Printf("Find error: %v\n", err)
			} else {
				fmt.Printf("Value: %s, Blocks Accessed: %d\n", value, blocks)
			}
		case "kill":
			if len(tokens) != 2 {
				fmt.Println("Usage: kill <db_name>")
				continue
			}
			err := kill(tokens[1])
			if err != nil {
				fmt.Printf("Kill error: %v\n", err)
			}
		case "quit":
			fmt.Println("Bye!")
			return
		default:
			fmt.Println("Unknown command")
		}
	}
}
