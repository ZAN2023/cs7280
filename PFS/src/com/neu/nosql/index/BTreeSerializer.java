package com.neu.nosql.index;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class provides methods for serializing and deserializing a B-tree.
 * The serialization process converts the B-tree into a string representation,
 * while the deserialization process reconstructs the B-tree from the string representation.
 */
public class BTreeSerializer {

    /**
     * Serializes a B-tree into a string representation.
     *
     * @param root the root node of the B-tree
     * @return the string representation of the B-tree
     */
    // 序列化方法
    public String serialize(BTreeNode root) {
        if (root == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        serializeHelper(root, sb);
        return sb.toString();
    }

    /**
     * Helper method for serializing a B-tree node and its children recursively.
     *
     * @param node the current node being serialized
     * @param sb   the StringBuilder to append the serialized data
     */
    private void serializeHelper(BTreeNode node, StringBuilder sb) {
        sb.append(node.leaf).append(",");
        sb.append(node.keys.size()).append(",");
        for (Node key : node.keys) {
            sb.append(key.key).append(",").append(key.val).append(",");
        }
        if (!node.leaf) {
            for (BTreeNode child : node.children) {
                serializeHelper(child, sb);
            }
        }
    }

    /**
     * Deserializes a string representation of a B-tree into a B-tree object.
     *
     * @param data the string representation of the B-tree
     * @return the root node of the reconstructed B-tree
     */
    // 反序列化方法
    public BTreeNode deserialize(String data) {
        if (data == null || data.length() == 0) {
            return null;
        }

        Queue<String> queue = new LinkedList<>(Arrays.asList(data.split(",")));
        return deserializeHelper(queue);
    }

    /**
     * Helper method for deserializing a B-tree node and its children recursively.
     *
     * @param queue the queue containing the serialized data
     * @return the reconstructed B-tree node
     */
    private BTreeNode deserializeHelper(Queue<String> queue) {
        boolean leaf = Boolean.parseBoolean(queue.poll());
        int size = Integer.parseInt(queue.poll());
        BTreeNode node = new BTreeNode(leaf);

        for (int i = 0; i < size; i++) {
            int key = Integer.parseInt(queue.poll());
            int val = Integer.parseInt(queue.poll());
            node.keys.add(new Node(key, val));
        }

        if (!leaf) {
            for (int i = 0; i < size + 1; i++) {
                node.children.add(deserializeHelper(queue));
            }
        }

        return node;
    }
}