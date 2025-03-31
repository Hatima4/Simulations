package BinaryTree;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class BinaryTreeVisualizer extends JFrame {
    private BinaryTree tree;
    private JPanel treePanel;
    private JPanel controlPanel;
    private JPanel infoPanel;
    private JPanel statsPanel;
    private JButton inorderButton;
    private JButton preorderButton;
    private JButton postorderButton;
    private JButton levelOrderButton;
    private JButton dfsButton;
    private JButton bfsButton;
    private JButton insertButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton randomButton;
    private JTextField searchField;
    private JTextField insertField;
    private JLabel statusLabel;
    private JLabel pathLabel;
    private JLabel heightLabel;
    private JLabel sizeLabel;
    private JLabel balanceLabel;
    private JSlider speedSlider;
    private List<Integer> traversalPath;
    private int currentTraversalIndex = 0;
    private Timer traversalTimer;

    // Visual constants
    private static final int NODE_RADIUS = 25;
    private static final int VERTICAL_SPACING = 70;
    private static final int HORIZONTAL_SPACING = 50;
    private static final int DEFAULT_ANIMATION_DELAY = 1000;
    private static final Color NODE_COLOR = new Color(52, 152, 219); // Modern Blue
    private static final Color HIGHLIGHT_COLOR = new Color(231, 76, 60); // Modern Red
    private static final Color CONNECTION_COLOR = new Color(149, 165, 166); // Modern Gray
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241); // Light Gray
    private static final Color PANEL_COLOR = new Color(255, 255, 255); // White
    private static final Color BUTTON_HOVER_COLOR = new Color(41, 128, 185); // Darker Blue
    private static final Font NODE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);

    public BinaryTreeVisualizer(BinaryTree tree) {
        this.tree = tree;
        this.traversalPath = new ArrayList<>();
        setupUI();
        updateStats();
    }

    private void setupUI() {
        setTitle("Binary Tree Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create main panels
        createTreePanel();
        createControlPanel();
        createInfoPanel();
        createStatsPanel();

        // Add panels to frame with proper spacing
        add(treePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.EAST);

        // Setup animation timer
        setupAnimationTimer();

        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                traversalTimer.stop();
            }
        });
    }

    private void createTreePanel() {
        treePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                drawTree(g2d, getWidth() / 2, 100);
            }
        };
        treePanel.setBackground(PANEL_COLOR);
        treePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(BACKGROUND_COLOR);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create main control panel
        JPanel mainControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        mainControls.setBackground(BACKGROUND_COLOR);

        // Create traversal buttons panel
        JPanel traversalPanel = createGroupPanel("Traversals");
        inorderButton = createStyledButton("Inorder");
        preorderButton = createStyledButton("Preorder");
        postorderButton = createStyledButton("Postorder");
        levelOrderButton = createStyledButton("Level Order");
        traversalPanel.add(inorderButton);
        traversalPanel.add(preorderButton);
        traversalPanel.add(postorderButton);
        traversalPanel.add(levelOrderButton);

        // Create search panel
        JPanel searchPanel = createGroupPanel("Search");
        searchField = createStyledTextField(5);
        dfsButton = createStyledButton("DFS");
        bfsButton = createStyledButton("BFS");
        searchPanel.add(new JLabel("Value:"));
        searchPanel.add(searchField);
        searchPanel.add(dfsButton);
        searchPanel.add(bfsButton);

        // Create modification panel
        JPanel modifyPanel = createGroupPanel("Modify Tree");
        insertField = createStyledTextField(5);
        insertButton = createStyledButton("Insert");
        deleteButton = createStyledButton("Delete");
        clearButton = createStyledButton("Clear");
        randomButton = createStyledButton("Random");
        modifyPanel.add(new JLabel("Value:"));
        modifyPanel.add(insertField);
        modifyPanel.add(insertButton);
        modifyPanel.add(deleteButton);
        modifyPanel.add(clearButton);
        modifyPanel.add(randomButton);

        // Create speed control panel
        JPanel speedPanel = createGroupPanel("Animation Speed");
        speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 2000, DEFAULT_ANIMATION_DELAY);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setMinorTickSpacing(100);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setBackground(BACKGROUND_COLOR);
        speedPanel.add(speedSlider);

        // Add all panels to main control panel
        mainControls.add(traversalPanel);
        mainControls.add(searchPanel);
        mainControls.add(modifyPanel);
        mainControls.add(speedPanel);

        // Add action listeners
        inorderButton.addActionListener(e -> startTraversal("inorder"));
        preorderButton.addActionListener(e -> startTraversal("preorder"));
        postorderButton.addActionListener(e -> startTraversal("postorder"));
        levelOrderButton.addActionListener(e -> startTraversal("levelorder"));
        dfsButton.addActionListener(e -> startSearch("dfs"));
        bfsButton.addActionListener(e -> startSearch("bfs"));
        insertButton.addActionListener(e -> insertNode());
        deleteButton.addActionListener(e -> deleteNode());
        clearButton.addActionListener(e -> clearTree());
        randomButton.addActionListener(e -> generateRandomTree());
        speedSlider.addChangeListener(e -> updateAnimationSpeed());

        controlPanel.add(mainControls);
    }

    private JPanel createGroupPanel(String title) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return panel;
    }

    private void createInfoPanel() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(BACKGROUND_COLOR);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        statusLabel = new JLabel("Ready");
        pathLabel = new JLabel("Path: ");
        statusLabel.setFont(LABEL_FONT);
        pathLabel.setFont(LABEL_FONT);

        infoPanel.add(statusLabel);
        infoPanel.add(pathLabel);
    }

    private void createStatsPanel() {
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Tree Statistics");
        titleLabel.setFont(HEADER_FONT);
        heightLabel = new JLabel("Height: ");
        sizeLabel = new JLabel("Size: ");
        balanceLabel = new JLabel("Balance: ");

        heightLabel.setFont(LABEL_FONT);
        sizeLabel.setFont(LABEL_FONT);
        balanceLabel.setFont(LABEL_FONT);

        statsPanel.add(titleLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(heightLabel);
        statsPanel.add(sizeLabel);
        statsPanel.add(balanceLabel);
    }

    private void setupAnimationTimer() {
        traversalTimer = new Timer(DEFAULT_ANIMATION_DELAY, e -> {
            if (currentTraversalIndex < traversalPath.size()) {
                currentTraversalIndex++;
                updatePathLabel();
                treePanel.repaint();
            } else {
                traversalTimer.stop();
                statusLabel.setText("Traversal Complete");
            }
        });
    }

    private void updateAnimationSpeed() {
        traversalTimer.setDelay(speedSlider.getValue());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(90, 35));
        button.setFont(LABEL_FONT);
        button.setBackground(NODE_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER_COLOR);
                button.repaint();
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(NODE_COLOR);
                button.repaint();
            }
        });

        return button;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setPreferredSize(new Dimension(60, 30));
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return field;
    }

    private void clearTree() {
        tree = new BinaryTree();
        updateStats();
        treePanel.repaint();
        statusLabel.setText("Tree cleared");
    }

    private void generateRandomTree() {
        tree = new BinaryTree();
        for (int i = 0; i < 10; i++) {
            tree.insert((int) (Math.random() * 100));
        }
        updateStats();
        treePanel.repaint();
        statusLabel.setText("Random tree generated");
    }

    private void insertNode() {
        try {
            int value = Integer.parseInt(insertField.getText());
            tree.insert(value);
            insertField.setText("");
            updateStats();
            treePanel.repaint();
            statusLabel.setText("Inserted node: " + value);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteNode() {
        try {
            int value = Integer.parseInt(insertField.getText());
            tree.delete(value);
            insertField.setText("");
            updateStats();
            treePanel.repaint();
            statusLabel.setText("Deleted node: " + value);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStats() {
        heightLabel.setText("Height: " + tree.getHeight());
        sizeLabel.setText("Size: " + tree.getSize());
        // Add balance calculation if needed
        balanceLabel.setText("Balance: " + calculateBalance());
    }

    private String calculateBalance() {
        // Simple balance calculation based on height difference
        BinaryTree.Node root = tree.getRoot();
        if (root == null)
            return "Empty";
        int leftHeight = getHeightRec(root.left);
        int rightHeight = getHeightRec(root.right);
        int diff = Math.abs(leftHeight - rightHeight);
        if (diff <= 1)
            return "Balanced";
        return "Unbalanced";
    }

    private int getHeightRec(BinaryTree.Node node) {
        if (node == null)
            return 0;
        return Math.max(getHeightRec(node.left), getHeightRec(node.right)) + 1;
    }

    private void startTraversal(String type) {
        traversalPath.clear();
        currentTraversalIndex = 0;
        statusLabel.setText("Running " + type + " traversal...");
        pathLabel.setText("Path: ");

        switch (type) {
            case "inorder":
                collectInorderTraversal(tree.getRoot());
                break;
            case "preorder":
                collectPreorderTraversal(tree.getRoot());
                break;
            case "postorder":
                collectPostorderTraversal(tree.getRoot());
                break;
            case "levelorder":
                collectLevelOrderTraversal(tree.getRoot());
                break;
        }

        traversalTimer.start();
    }

    private void startSearch(String type) {
        try {
            int target = Integer.parseInt(searchField.getText());
            traversalPath.clear();
            currentTraversalIndex = 0;
            statusLabel.setText("Running " + type + " search for " + target + "...");
            pathLabel.setText("Path: ");

            if (type.equals("dfs")) {
                collectDFSPath(tree.getRoot(), target);
            } else {
                collectBFSPath(tree.getRoot(), target);
            }

            traversalTimer.start();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePathLabel() {
        StringBuilder path = new StringBuilder("Path: ");
        for (int i = 0; i < currentTraversalIndex; i++) {
            path.append(traversalPath.get(i));
            if (i < currentTraversalIndex - 1) {
                path.append(" → ");
            }
        }
        pathLabel.setText(path.toString());
    }

    private void collectInorderTraversal(BinaryTree.Node node) {
        if (node != null) {
            collectInorderTraversal(node.left);
            traversalPath.add(node.data);
            collectInorderTraversal(node.right);
        }
    }

    private void collectPreorderTraversal(BinaryTree.Node node) {
        if (node != null) {
            traversalPath.add(node.data);
            collectPreorderTraversal(node.left);
            collectPreorderTraversal(node.right);
        }
    }

    private void collectPostorderTraversal(BinaryTree.Node node) {
        if (node != null) {
            collectPostorderTraversal(node.left);
            collectPostorderTraversal(node.right);
            traversalPath.add(node.data);
        }
    }

    private void collectLevelOrderTraversal(BinaryTree.Node root) {
        if (root == null)
            return;
        java.util.Queue<BinaryTree.Node> queue = new java.util.LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            BinaryTree.Node node = queue.poll();
            traversalPath.add(node.data);

            if (node.left != null)
                queue.add(node.left);
            if (node.right != null)
                queue.add(node.right);
        }
    }

    private boolean collectDFSPath(BinaryTree.Node node, int target) {
        if (node == null)
            return false;

        traversalPath.add(node.data);
        if (node.data == target)
            return true;

        if (collectDFSPath(node.left, target) || collectDFSPath(node.right, target)) {
            return true;
        }

        traversalPath.remove(traversalPath.size() - 1);
        return false;
    }

    private boolean collectBFSPath(BinaryTree.Node root, int target) {
        if (root == null)
            return false;

        java.util.Queue<BinaryTree.Node> queue = new java.util.LinkedList<>();
        java.util.Map<BinaryTree.Node, BinaryTree.Node> parent = new java.util.HashMap<>();

        queue.add(root);
        parent.put(root, null);

        while (!queue.isEmpty()) {
            BinaryTree.Node current = queue.poll();
            traversalPath.add(current.data);

            if (current.data == target)
                return true;

            if (current.left != null) {
                queue.add(current.left);
                parent.put(current.left, current);
            }
            if (current.right != null) {
                queue.add(current.right);
                parent.put(current.right, current);
            }
        }

        return false;
    }

    private void drawTree(Graphics2D g2d, int x, int y) {
        drawTreeRecursive(g2d, tree.getRoot(), x, y, 0);
    }

    private void drawTreeRecursive(Graphics2D g2d, BinaryTree.Node node, int x, int y, int level) {
        if (node == null)
            return;

        // Calculate positions for children
        int horizontalOffset = (int) (HORIZONTAL_SPACING * Math.pow(2, tree.getHeight() - level - 1));
        int childY = y + VERTICAL_SPACING;

        // Draw connections to children
        if (node.left != null) {
            g2d.setColor(CONNECTION_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x, y + NODE_RADIUS, x - horizontalOffset, childY - NODE_RADIUS);
        }
        if (node.right != null) {
            g2d.setColor(CONNECTION_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(x, y + NODE_RADIUS, x + horizontalOffset, childY - NODE_RADIUS);
        }

        // Draw current node
        boolean isInTraversalPath = currentTraversalIndex > 0 &&
                traversalPath.get(currentTraversalIndex - 1) == node.data;

        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(x - NODE_RADIUS + 2, y - NODE_RADIUS + 2,
                NODE_RADIUS * 2, NODE_RADIUS * 2);

        // Draw node
        g2d.setColor(isInTraversalPath ? HIGHLIGHT_COLOR : NODE_COLOR);
        g2d.fillOval(x - NODE_RADIUS, y - NODE_RADIUS,
                NODE_RADIUS * 2, NODE_RADIUS * 2);

        // Draw node border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - NODE_RADIUS, y - NODE_RADIUS,
                NODE_RADIUS * 2, NODE_RADIUS * 2);

        // Draw node value
        g2d.setColor(Color.WHITE);
        g2d.setFont(NODE_FONT);
        String text = String.valueOf(node.data);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x - fm.stringWidth(text) / 2;
        int textY = y + fm.getAscent() / 2;
        g2d.drawString(text, textX, textY);

        // Recursively draw children
        drawTreeRecursive(g2d, node.left, x - horizontalOffset, childY, level + 1);
        drawTreeRecursive(g2d, node.right, x + horizontalOffset, childY, level + 1);
    }

    public static void main(String[] args) {
        // Create and populate the tree
        BinaryTree tree = new BinaryTree();
        tree.insert(50);
        tree.insert(30);
        tree.insert(70);
        tree.insert(20);
        tree.insert(40);
        tree.insert(60);
        tree.insert(80);

        // Create and show the visualizer
        SwingUtilities.invokeLater(() -> {
            BinaryTreeVisualizer visualizer = new BinaryTreeVisualizer(tree);
            visualizer.setLocationRelativeTo(null);
            visualizer.setVisible(true);
        });
    }
}