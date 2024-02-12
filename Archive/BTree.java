import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

class BTreeNode {
    boolean leaf;
    List<Integer> keys;
    List<BTreeNode> children;

    public BTreeNode(boolean leaf) {
        this.leaf = leaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
    }
}

public class BTree {
    private BTreeNode root;
    private int t; // Minimum degree

    public BTree(int t) {
        this.root = new BTreeNode(true);
        this.t = t;
    }

    public void insert(int key) {
        BTreeNode r = root;
        if (r.keys.size() == (2 * t - 1)) {
            BTreeNode s = new BTreeNode(false);
            root = s;
            s.children.add(r);
            splitChild(s, 0);
            insertNonFull(s, key);
        } else {
            insertNonFull(r, key);
        }
    }

    private void insertNonFull(BTreeNode x, int key) {
        int i = x.keys.size() - 1;
        if (x.leaf) {
            while (i >= 0 && key < x.keys.get(i)) {
                i--;
            }
            x.keys.add(i + 1, key);
        } else {
            while (i >= 0 && key < x.keys.get(i)) {
                i--;
            }
            i++;
            if (x.children.get(i).keys.size() == (2 * t - 1)) {
                splitChild(x, i);
                if (key > x.keys.get(i)) {
                    i++;
                }
            }
            insertNonFull(x.children.get(i), key);
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

    public void print() {
        print(root, 0);
    }

    private void print(BTreeNode node, int level) {
        System.out.println("Level " + level + ": " + node.keys);
        if (!node.leaf) {
            for (BTreeNode child : node.children) {
                print(child, level + 1);
            }
        }
    }
    public void printTree(BTreeNode node) {
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
                    for (int key : x.keys) {
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

    public static void main(String[] args) {
        BTree bTree = new BTree(3);

        int[] keys = {34, 11, 76, 53, 29, 48, 65, 95, 81, 92, 68, 59, 87, 20, 45, 26, 83, 70, 37, 7, 17, 73, 42, 96, 23, 58, 8, 50, 94, 61};
//        int[] keys = {29, 41, 44, 62, 46};

        for (int key : keys) {
            bTree.insert(key);
        }

        System.out.println("B-tree structure:");
        bTree.printTree(bTree.root);
    }
}
