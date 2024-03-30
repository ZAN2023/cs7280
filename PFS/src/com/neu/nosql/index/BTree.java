package com.neu.nosql.index;

import java.util.LinkedList;
import java.util.Queue;

public class BTree {

    private static final int MINIMUM_DEGREE = 3;

    private BTreeNode root;
    private final int t; // Minimum degree

    public BTree() {
        this.root = new BTreeNode(true);
        this.t = MINIMUM_DEGREE;
    }

    public BTreeNode getRoot() {
        return root;
    }

    public void insert(int key, int val) {
        BTreeNode r = root;
        if (r.keys.size() == (2 * t - 1)) {
            BTreeNode s = new BTreeNode(false);
            root = s;
            s.children.add(r);
            splitChild(s, 0);
            insertNonFull(s, key, val);
        } else {
            insertNonFull(r, key, val);
        }
    }

    private void insertNonFull(BTreeNode x, int key, int val) {
        int i = x.keys.size() - 1;
        if (x.leaf) {
            while (i >= 0 && key < x.keys.get(i).key) {
                i--;
            }
            x.keys.add(i + 1, new Node(key, val));
        } else {
            while (i >= 0 && key < x.keys.get(i).key) {
                i--;
            }
            i++;
            if (x.children.get(i).keys.size() == (2 * t - 1)) {
                splitChild(x, i);
                if (key > x.keys.get(i).key) {
                    i++;
                }
            }
            insertNonFull(x.children.get(i), key, val);
        }
    }

    private void splitChild(BTreeNode x, int i) {
        BTreeNode y = x.children.get(i);
        BTreeNode z = new BTreeNode(y.leaf);

        x.children.add(i + 1, z);
        x.keys.add(i, y.keys.get(t - 1));

        z.keys.addAll(y.keys.subList(t, 2 * t - 1));
        y.keys.subList(t - 1, 2 * t - 1).clear();

        if (!y.leaf) {
            z.children.addAll(y.children.subList(t, 2 * t));
            y.children.subList(t, 2 * t).clear();
        }
    }

    public int find(int key) {
        return findKey(root, key);
    }

    private int findKey(BTreeNode node, int key) {
        int i = 0;
        while (i < node.keys.size() && key > node.keys.get(i).key) {
            i++;
        }

        if (i < node.keys.size() && key == node.keys.get(i).key) {
            // Key found in the current node
            return node.keys.get(i).val;
        } else if (node.leaf) {
            // Key not found in a leaf node
            return -1;
        } else {
            // Recursively search in the appropriate children
            return findKey(node.children.get(i), key);
        }
    }

    public void print(BTreeNode node) {
        if (node == null) {
            return;
        }

        Queue<BTreeNode> queue = new LinkedList<>();
        queue.offer(node);

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                BTreeNode x = queue.poll();
                if (x != null) {
                    // Print keys
                    for (Node key : x.keys) {
                        System.out.print(key + " ");
                    }
                    System.out.print("---");

                    // Enqueue children
                    if (!x.leaf) {
                        for (BTreeNode child : x.children) {
                            queue.offer(child);
                        }
                    }
                }
            }
            System.out.println();
        }
    }
}