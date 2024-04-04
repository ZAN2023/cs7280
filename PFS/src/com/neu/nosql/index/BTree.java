package com.neu.nosql.index;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This class represents a B-tree data structure.
 * A B-tree is a self-balancing tree data structure that maintains sorted data and allows searches,
 * sequential access, insertions, and deletions in logarithmic time.
 *
 * The BTree class provides methods for inserting keys into the tree and searching for keys.
 * The tree is implemented as a collection of B-tree nodes, each containing a list of keys and children.
 * The keys are stored in sorted order within each node, and the children represent the subtrees of the node.
 *
 * The B-tree has a minimum degree t, which determines the minimum number of keys in each node.
 * The tree is balanced such that all leaf nodes are at the same level, and each non-leaf node (except the root)
 * contains at least t-1 keys and at most 2t-1 keys.
 *
 * The insert() method inserts a key into the tree, maintaining the properties of the B-tree.
 * The find() method searches for a key in the tree and returns the associated value.
 * The print() method prints the keys of the tree level by level.
 */
public class BTree {

    private static final int MINIMUM_DEGREE = 3;

    private BTreeNode root;
    private final int t; // Minimum degree

    /**
     * Constructs a new B-tree with the default minimum degree.
     */
    public BTree() {
        this.root = new BTreeNode(true);
        this.t = MINIMUM_DEGREE;
    }


    /**
     * Gets the root node of the B-tree.
     */
    public BTreeNode getRoot() {
        return root;
    }

    /**
     * Inserts a key-value pair into the B-tree.
     *
     * @param key the key to be inserted
     * @param val the value associated with the key
     */
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

    /**
     * Inserts a key-value pair into a non-full node.
     *
     * @param x   the node to insert the key into
     * @param key the key to be inserted
     * @param val the value associated with the key
     */
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

    /**
     * Splits a child node of a given node.
     *
     * @param x the parent node
     * @param i the index of the child to be split
     */
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

    /**
     * Searches for a key in the B-tree and returns the associated value.
     *
     * @param key the key to search for
     * @return the value associated with the key, or -1 if the key is not found
     */
    public int find(int key) {
        return findKey(root, key);
    }

    /**
     * Recursively searches for a key in a B-tree node and its children.
     *
     * @param node the node to search in
     * @param key  the key to search for
     * @return the value associated with the key, or -1 if the key is not found
     */
    public static int findKey(BTreeNode node, int key) {
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

    /**
     * Prints the keys of the B-tree level by level.
     *
     * @param node the root node of the B-tree
     */
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