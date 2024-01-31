#include <iostream>
#include <vector>
#include <algorithm>

class BTree {
private:
    static const int NODE_SIZE = 5;  // size of each node

    struct Node {
        std::vector<int> keyvalues;     // arraylist of keys
        std::vector<int> children;    // children pointers
        int size = 0;                 // number of entries

        Node(bool isLeaf = false) {
            keyvalues.resize(NODE_SIZE, -1);
            if (!isLeaf) {
                children.resize(NODE_SIZE + 1, -1);
            }
        }
    };

    std::vector<Node> nodes;  // dynamic array of nodes
    int root;                 // pointer to the root node
    int cntValues = 0;        // number of currently used values

    bool lookup(int value, int pointer) {
        if (pointer == -1) {        
            return false;
        }

        Node &node = nodes[pointer];
        if (node.children[0] == -1) { // Leaf node
            return std::find(node.keyvalues.begin(), node.keyvalues.begin() + node.size, value) != node.keyvalues.end();
        }

        // Internal node: Find appropriate child
        for (int i = 0; i < node.size; ++i) {
            if (value < node.keyvalues[i]) {
                return lookup(value, node.children[i]);
            }
        }
        return lookup(value, node.children[node.size]);
    }

    int splitNode(int pointer, int extraChild = -1) {
        Node &oldNode = nodes[pointer];
        int middleIndex = NODE_SIZE / 2;

        // Create a new node and distribute the second half of the values to it.
        int newNodeIndex = initNode();
        Node &newNode = nodes[newNodeIndex];
        newNode.size = NODE_SIZE - middleIndex;

        std::move(oldNode.keyvalues.begin() + middleIndex, oldNode.keyvalues.end(), newNode.keyvalues.begin());
        std::fill(oldNode.keyvalues.begin() + middleIndex, oldNode.keyvalues.end(), -1);
        oldNode.size = middleIndex;

        // If the node is not a leaf, redistribute the children.
        if (!oldNode.children.empty()) {
            if (extraChild != -1 && extraChild > oldNode.children[middleIndex]) {
                // Extra child belongs to the new node.
                std::move(oldNode.children.begin() + middleIndex + 1, oldNode.children.end(), newNode.children.begin());
                newNode.children[NODE_SIZE - middleIndex - 1] = extraChild;
            } else {
                // Extra child belongs to the old node or there is no extra child.
                std::move(oldNode.children.begin() + middleIndex, oldNode.children.end(), newNode.children.begin());
                if (extraChild != -1) {
                    oldNode.children[middleIndex] = extraChild;
                }
            }
            std::fill(oldNode.children.begin() + middleIndex, oldNode.children.end(), -1);
        }

        return newNodeIndex;
    }

    void insertIntoNode(int value, int pointer, int childPointer = -1) {
        Node &node = nodes[pointer];
        auto it = std::upper_bound(node.keyvalues.begin(), node.keyvalues.begin() + node.size, value);
        int index = it - node.keyvalues.begin();

        std::move_backward(it, node.keyvalues.begin() + node.size, node.keyvalues.begin() + node.size + 1);
        node.keyvalues[index] = value;
        node.size++;

        // If it's an internal node, insert the child pointer.
        if (childPointer != -1) {
            std::move_backward(node.children.begin() + index + 1, node.children.begin() + node.size, node.children.begin() + node.size + 1);
            node.children[index + 1] = childPointer;
        }
    }

    int createNewRoot(int oldRootIndex, int newNodeIndex) {
        int newRootIndex = initNode();
        Node &newRoot = nodes[newRootIndex];
        newRoot.keyvalues[0] = nodes[newNodeIndex].keyvalues[0];
        newRoot.children[0] = oldRootIndex;
        newRoot.children[1] = newNodeIndex;
        newRoot.size = 1;

        root = newRootIndex;
        return newRootIndex;
    }


    int insert(int value, int pointer) {
        Node &node = nodes[pointer];

        // if the current node is a leaf node.
        if (node.children[0] == -1) { 
            // Check if value already exists in the leaf node.
            auto it = std::find(node.keyvalues.begin(), node.keyvalues.begin() + node.size, value);
            if (it != node.keyvalues.begin() + node.size) {
                return -2; // Value already exists.
            }

            // Check if there is space left in the leaf node.
            if (node.size < NODE_SIZE) {
                node.keyvalues[node.size++] = value;
                std::sort(node.keyvalues.begin(), node.keyvalues.begin() + node.size);
                return -1; // Value inserted, everything is done.
            } else {
                // Handle full leaf node: splitting and promotion.
                int middle = splitNode(pointer);
                if (value < nodes[middle].keyvalues[0]) {
                    insertIntoNode(value, pointer);
                } else {
                    insertIntoNode(value, middle);
                }

                // Handle promotion if necessary.
                if (pointer == root) {
                    int newRoot = createNewRoot(pointer, middle);
                    return newRoot;
                }

                return middle; // Return the pointer to the new node after splitting.
            }
        } else {
            // The current node is not a leaf node.

            // Find the child node for the current node.
            int childIndex = 0;
            for (; childIndex < node.size && value > node.keyvalues[childIndex]; ++childIndex);

            int child = insert(value, node.children[childIndex]);
            if (child == -2 || child == -1) {
                return child; // Value exists already or everything is done.
            }

            // Check if there is space left for the new child pointer in the current node.
            if (node.size < NODE_SIZE) {
                insertIntoNode(nodes[child].keyvalues[0], pointer, child);
                return -1;
            } else {
                // Handle full internal node: Splitting and promotion.
                int middle = splitNode(pointer, child);
                if (nodes[child].keyvalues[0] < nodes[middle].keyvalues[0]) {
                    insertIntoNode(nodes[child].keyvalues[0], pointer, child);
                } else {
                    insertIntoNode(nodes[child].keyvalues[0], middle, child);
                }

                // Handle promotion if necessary.
                if (pointer == root) {
                    int newRoot = createNewRoot(pointer, middle);
                    return newRoot;
                }

                return middle; // Return the pointer to the new node after splitting.
            }
        }
    }


    void checkSize() {
        if (nodes.size() <= static_cast<size_t>(root)) {
            nodes.resize(nodes.size() * 2);
        }
    }

    int initNode() {
        nodes.emplace_back();
        checkSize();
        return nodes.size() - 1;
    }

    int createLeaf() {
        nodes.emplace_back(true);
        checkSize();
        return nodes.size() - 1;
    }

    void display(int nodeIndex, int level) {
        if (nodeIndex == -1) {
            return;
        }

        const Node &node = nodes[nodeIndex];
        std::cout << "Level " << level << " [";
        for (int i = 0; i < node.size; ++i) {
            std::cout << node.keyvalues[i] << (i < node.size - 1 ? ", " : "");
        }
        std::cout << "]\n";

        if (node.children[0] != -1) {
            for (int i = 0; i <= node.size; ++i) {
                display(node.children[i], level + 1);
            }
        }
    }

public:
    BTree() {
        root = initNode();
        nodes[root].children[0] = createLeaf();
    }

    bool Lookup(int value) {
        return lookup(value, root);
    }

    void Insert(int value) {
        if (insert(value, root) == -1) {
            cntValues++;
        }
    }

    int CntValues() const {
        return cntValues;
    }

    void Display() {
        display(root, 0);
    }
};
