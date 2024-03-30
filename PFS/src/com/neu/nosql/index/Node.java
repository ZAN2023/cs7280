package com.neu.nosql.index;

public class Node {
    int key;
    int val;

    public Node(int key, int val) {
        this.key = key;
        this.val = val;
    }

    @Override
    public String toString() {
        return "{" +
                "key=" + key +
                ", val=" + val +
                '}';
    }
}
