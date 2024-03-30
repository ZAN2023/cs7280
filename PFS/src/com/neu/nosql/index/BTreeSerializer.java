package com.neu.nosql.index;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class BTreeSerializer {

    // 序列化方法
    public String serialize(BTreeNode root) {
        if (root == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        serializeHelper(root, sb);
        return sb.toString();
    }

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

    // 反序列化方法
    public BTreeNode deserialize(String data) {
        if (data == null || data.length() == 0) {
            return null;
        }

        Queue<String> queue = new LinkedList<>(Arrays.asList(data.split(",")));
        return deserializeHelper(queue);
    }

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