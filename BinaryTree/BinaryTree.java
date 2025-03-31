package BinaryTree;
public class BinaryTree {
    // Node class to represent each node in the tree
    public static class Node {
        int data;
        Node left;
        Node right;

        Node(int data) {
            this.data = data;
            this.left = null;
            this.right = null;
        }
    }

    private Node root;

    // Constructor
    public BinaryTree() {
        root = null;
    }

    // Getter for root node
    public Node getRoot() {
        return root;
    }

    // Insert a node into the tree
    public void insert(int data) {
        root = insertRec(root, data);
    }

    private Node insertRec(Node root, int data) {
        if (root == null) {
            root = new Node(data);
            return root;
        }

        if (data < root.data) {
            root.left = insertRec(root.left, data);
        } else if (data > root.data) {
            root.right = insertRec(root.right, data);
        }

        return root;
    }

    // Delete a node from the tree
    public void delete(int data) {
        root = deleteRec(root, data);
    }

    private Node deleteRec(Node root, int data) {
        if (root == null) {
            return root;
        }

        if (data < root.data) {
            root.left = deleteRec(root.left, data);
        } else if (data > root.data) {
            root.right = deleteRec(root.right, data);
        } else {
            // Node with only one child or no child
            if (root.left == null) {
                return root.right;
            } else if (root.right == null) {
                return root.left;
            }

            // Node with two children
            root.data = minValue(root.right);
            root.right = deleteRec(root.right, root.data);
        }

        return root;
    }

    private int minValue(Node root) {
        int minv = root.data;
        while (root.left != null) {
            minv = root.left.data;
            root = root.left;
        }
        return minv;
    }

    // Get height of the tree
    public int getHeight() {
        return getHeightRec(root);
    }

    private int getHeightRec(Node root) {
        if (root == null)
            return 0;
        return Math.max(getHeightRec(root.left), getHeightRec(root.right)) + 1;
    }

    // Get size of the tree (number of nodes)
    public int getSize() {
        return getSizeRec(root);
    }

    private int getSizeRec(Node root) {
        if (root == null)
            return 0;
        return getSizeRec(root.left) + getSizeRec(root.right) + 1;
    }

    // Inorder Traversal (Left -> Root -> Right)
    public void inorderTraversal() {
        System.out.println("Inorder Traversal:");
        inorderRec(root);
        System.out.println();
    }

    private void inorderRec(Node root) {
        if (root != null) {
            inorderRec(root.left);
            System.out.print(root.data + " ");
            inorderRec(root.right);
        }
    }

    // Preorder Traversal (Root -> Left -> Right)
    public void preorderTraversal() {
        System.out.println("Preorder Traversal:");
        preorderRec(root);
        System.out.println();
    }

    private void preorderRec(Node root) {
        if (root != null) {
            System.out.print(root.data + " ");
            preorderRec(root.left);
            preorderRec(root.right);
        }
    }

    // Postorder Traversal (Left -> Right -> Root)
    public void postorderTraversal() {
        System.out.println("Postorder Traversal:");
        postorderRec(root);
        System.out.println();
    }

    private void postorderRec(Node root) {
        if (root != null) {
            postorderRec(root.left);
            postorderRec(root.right);
            System.out.print(root.data + " ");
        }
    }

    // Level Order Traversal (Breadth-First)
    public void levelOrderTraversal() {
        System.out.println("Level Order Traversal:");
        if (root == null)
            return;

        java.util.Queue<Node> queue = new java.util.LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node tempNode = queue.poll();
            System.out.print(tempNode.data + " ");

            if (tempNode.left != null) {
                queue.add(tempNode.left);
            }
            if (tempNode.right != null) {
                queue.add(tempNode.right);
            }
        }
        System.out.println();
    }

    // Depth-First Search (DFS)
    public boolean dfs(int target) {
        System.out.println("DFS Search for " + target + ":");
        return dfsRec(root, target);
    }

    private boolean dfsRec(Node root, int target) {
        if (root == null)
            return false;

        if (root.data == target) {
            System.out.println("Found " + target + "!");
            return true;
        }

        System.out.print(root.data + " -> ");
        return dfsRec(root.left, target) || dfsRec(root.right, target);
    }

    // Breadth-First Search (BFS)
    public boolean bfs(int target) {
        System.out.println("BFS Search for " + target + ":");
        if (root == null)
            return false;

        java.util.Queue<Node> queue = new java.util.LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node tempNode = queue.poll();
            System.out.print(tempNode.data + " -> ");

            if (tempNode.data == target) {
                System.out.println("Found " + target + "!");
                return true;
            }

            if (tempNode.left != null) {
                queue.add(tempNode.left);
            }
            if (tempNode.right != null) {
                queue.add(tempNode.right);
            }
        }
        System.out.println("Not found!");
        return false;
    }
}