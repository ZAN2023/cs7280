package com.neu.nosql.index;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a node in a B-tree data structure.
 * Each node contains a flag indicating whether it is a leaf node,
 * a list of keys (Node objects), and a list of child nodes (BTreeNode objects).
 */
public class BTreeNode {
    boolean leaf;
    List<Node> keys;
    List<BTreeNode> children;

    /**
     * Constructs a new BTreeNode with the specified leaf flag.
     *
     * @param leaf true if the node is a leaf node, false otherwise
     */
    public BTreeNode(boolean leaf) {
        this.leaf = leaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
    }
}
