package com.neu.nosql.index;

import java.util.ArrayList;
import java.util.List;

public class BTreeNode {
    boolean leaf;
    List<Node> keys;
    List<BTreeNode> children;

    public BTreeNode(boolean leaf) {
        this.leaf = leaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
    }
}
