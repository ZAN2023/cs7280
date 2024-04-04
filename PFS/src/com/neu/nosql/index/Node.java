package com.neu.nosql.index;

/**
 * This class represents a node in a B-tree.
 * Each node contains a list of keys and a list of children.
 * The keys are used to store the key-value pairs, and the children are used to store the references to the child nodes.
 */
public class Node {
    int key;
    int val;

    /**
     * Constructs a node with the specified key and value.
     *
     * @param key the key of the node
     * @param val the value of the node
     */
    public Node(int key, int val) {
        this.key = key;
        this.val = val;
    }

    /**
     * Returns the key of the node.
     *
     * @return the key of the node
     */
    @Override
    public String toString() {
        return "{" +
                "key=" + key +
                ", val=" + val +
                '}';
    }
}
