import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

public class NeuralNetworkVisualization extends JFrame {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color NEURON_COLOR = new Color(41, 128, 185);
    private static final Color ACTIVE_NEURON_COLOR = new Color(231, 76, 60);
    private static final Color CONNECTION_COLOR = new Color(189, 195, 199);
    private static final Color POSITIVE_WEIGHT_COLOR = new Color(46, 204, 113);
    private static final Color NEGATIVE_WEIGHT_COLOR = new Color(231, 76, 60);

    private final int[] layers = { 4, 6, 5, 3 }; // Network architecture
    private final ArrayList<ArrayList<Neuron>> neurons;
    private final ArrayList<ArrayList<ArrayList<Connection>>> connections;
    private final Random random = new Random();
    private final DecimalFormat df = new DecimalFormat("#.##");

    private boolean isTraining = false;
    private javax.swing.Timer animationTimer;
    private int epoch = 0;
    private double learningRate = 0.1;
    private double momentum = 0.9;
    private double errorRate = 0.0;
    private ArrayList<Double> errorHistory = new ArrayList<>();
    private final int MAX_ERROR_HISTORY = 100;

    // XOR training data
    private final double[][] trainingInputs = {
            { 0, 0, 0, 0 }, { 0, 0, 0, 1 }, { 0, 0, 1, 0 }, { 0, 0, 1, 1 },
            { 0, 1, 0, 0 }, { 0, 1, 0, 1 }, { 0, 1, 1, 0 }, { 0, 1, 1, 1 },
            { 1, 0, 0, 0 }, { 1, 0, 0, 1 }, { 1, 0, 1, 0 }, { 1, 0, 1, 1 },
            { 1, 1, 0, 0 }, { 1, 1, 0, 1 }, { 1, 1, 1, 0 }, { 1, 1, 1, 1 }
    };

    private final double[][] trainingOutputs = {
            { 0, 0, 0 }, { 0, 0, 1 }, { 0, 1, 0 }, { 0, 1, 1 },
            { 1, 0, 0 }, { 1, 0, 1 }, { 1, 1, 0 }, { 1, 1, 1 },
            { 1, 0, 0 }, { 1, 0, 1 }, { 1, 1, 0 }, { 1, 1, 1 },
            { 0, 0, 0 }, { 0, 0, 1 }, { 0, 1, 0 }, { 0, 1, 1 }
    };

    private class Neuron {
        double x, y;
        double activation = 0.0;
        double bias = random.nextDouble() * 2 - 1;
        double error = 0.0;
        double deltaBias = 0.0;

        Neuron(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private class Connection {
        Neuron from, to;
        double weight = random.nextDouble() * 2 - 1;
        double deltaWeight = 0.0;
        double signalStrength = 0.0;

        Connection(Neuron from, Neuron to) {
            this.from = from;
            this.to = to;
        }
    }

    public NeuralNetworkVisualization() {
        setTitle("Neural Network Visualization - Deep Learning Simulator");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        neurons = new ArrayList<>();
        connections = new ArrayList<>();

        initializeNetwork();
        createGUI();

        // Animation timer
        animationTimer = new javax.swing.Timer(16, e -> {
            if (isTraining) {
                trainNetwork();
            }
            repaint();
        });
        animationTimer.start();
    }

    private void initializeNetwork() {
        // Initialize neurons with proper spacing
        int maxLayer = Arrays.stream(layers).max().getAsInt();
        double verticalSpacing = (HEIGHT * 0.6) / maxLayer;
        double horizontalSpacing = (WIDTH * 0.6) / (layers.length + 1);
        double startX = WIDTH * 0.2;

        for (int i = 0; i < layers.length; i++) {
            ArrayList<Neuron> layerNeurons = new ArrayList<>();
            double startY = (HEIGHT - (layers[i] * verticalSpacing)) / 2;

            for (int j = 0; j < layers[i]; j++) {
                layerNeurons.add(new Neuron(
                        startX + horizontalSpacing * i,
                        startY + j * verticalSpacing));
            }
            neurons.add(layerNeurons);
        }

        // Initialize connections
        for (int i = 0; i < layers.length - 1; i++) {
            ArrayList<ArrayList<Connection>> layerConnections = new ArrayList<>();
            for (Neuron from : neurons.get(i)) {
                ArrayList<Connection> neuronConnections = new ArrayList<>();
                for (Neuron to : neurons.get(i + 1)) {
                    neuronConnections.add(new Connection(from, to));
                }
                layerConnections.add(neuronConnections);
            }
            connections.add(layerConnections);
        }
    }

    private void createGUI() {
        setLayout(new BorderLayout());

        // Create main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(900);
        splitPane.setResizeWeight(0.75);

        // Network visualization panel
        JPanel networkPanel = createNetworkPanel();
        splitPane.setLeftComponent(networkPanel);

        // Control panel
        JPanel controlPanel = createControlPanel();
        splitPane.setRightComponent(controlPanel);

        add(splitPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createNetworkPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setBackground(BACKGROUND_COLOR);
                g2d.clearRect(0, 0, getWidth(), getHeight());

                // Draw connections
                drawConnections(g2d);

                // Draw neurons
                drawNeurons(g2d);

                // Draw error graph
                drawErrorGraph(g2d);
            }
        };
    }

    private void drawConnections(Graphics2D g2d) {
        for (ArrayList<ArrayList<Connection>> layerConns : connections) {
            for (ArrayList<Connection> neuronConns : layerConns) {
                for (Connection conn : neuronConns) {
                    // Determine connection color based on weight
                    Color weightColor = conn.weight > 0 ? POSITIVE_WEIGHT_COLOR : NEGATIVE_WEIGHT_COLOR;
                    float alpha = Math.min(1.0f, Math.max(0.1f, Math.abs((float) conn.weight)));
                    Color color = new Color(
                            weightColor.getRed(),
                            weightColor.getGreen(),
                            weightColor.getBlue(),
                            (int) (alpha * 255));

                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(1 + Math.abs((float) conn.weight)));
                    g2d.drawLine(
                            (int) conn.from.x, (int) conn.from.y,
                            (int) conn.to.x, (int) conn.to.y);
                }
            }
        }
    }

    private void drawNeurons(Graphics2D g2d) {
        for (ArrayList<Neuron> layer : neurons) {
            for (Neuron neuron : layer) {
                // Draw neuron shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillOval((int) neuron.x - 14, (int) neuron.y - 14, 28, 28);

                // Draw neuron body
                Color neuronColor = interpolateColor(NEURON_COLOR, ACTIVE_NEURON_COLOR, neuron.activation);
                g2d.setColor(neuronColor);
                g2d.fillOval((int) neuron.x - 15, (int) neuron.y - 15, 30, 30);

                // Draw neuron highlight
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.fillOval((int) neuron.x - 12, (int) neuron.y - 12, 10, 10);

                // Draw activation value
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
                String value = df.format(neuron.activation);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(value,
                        (int) neuron.x - fm.stringWidth(value) / 2,
                        (int) neuron.y + 25);
            }
        }
    }

    private void drawErrorGraph(Graphics2D g2d) {
        if (errorHistory.isEmpty())
            return;

        int graphHeight = 100;
        int graphWidth = 200;
        int x = 50;
        int y = HEIGHT - graphHeight - 50;

        // Draw graph background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x, y, graphWidth, graphHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, graphWidth, graphHeight);

        // Draw error line
        g2d.setColor(NEGATIVE_WEIGHT_COLOR);
        int numPoints = errorHistory.size();
        double xStep = (double) graphWidth / (numPoints - 1);

        for (int i = 0; i < numPoints - 1; i++) {
            int x1 = x + (int) (i * xStep);
            int x2 = x + (int) ((i + 1) * xStep);
            int y1 = y + graphHeight - (int) (errorHistory.get(i) * graphHeight);
            int y2 = y + graphHeight - (int) (errorHistory.get(i + 1) * graphHeight);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw labels
        g2d.setColor(Color.BLACK);
        g2d.drawString("Error Rate", x, y - 5);
        g2d.drawString("Epoch: " + epoch, x, y + graphHeight + 15);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Network controls
        JPanel networkControls = new JPanel(new GridLayout(0, 1, 5, 5));
        networkControls.setBorder(BorderFactory.createTitledBorder("Network Controls"));

        JButton trainButton = new JButton("Start Training");
        trainButton.addActionListener(e -> {
            isTraining = !isTraining;
            trainButton.setText(isTraining ? "Stop Training" : "Start Training");
        });

        JButton resetButton = new JButton("Reset Network");
        resetButton.addActionListener(e -> resetNetwork());

        // Learning parameters
        JPanel paramsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        paramsPanel.add(new JLabel("Learning Rate:"));
        JSlider lrSlider = new JSlider(1, 100, (int) (learningRate * 100));
        lrSlider.addChangeListener(e -> learningRate = lrSlider.getValue() / 100.0);
        paramsPanel.add(lrSlider);

        paramsPanel.add(new JLabel("Momentum:"));
        JSlider momentumSlider = new JSlider(0, 100, (int) (momentum * 100));
        momentumSlider.addChangeListener(e -> momentum = momentumSlider.getValue() / 100.0);
        paramsPanel.add(momentumSlider);

        networkControls.add(trainButton);
        networkControls.add(resetButton);
        networkControls.add(paramsPanel);

        // Statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Network Statistics"));
        statsPanel.add(new JLabel("Epochs:"));
        JLabel epochLabel = new JLabel("0");
        statsPanel.add(epochLabel);
        statsPanel.add(new JLabel("Error Rate:"));
        JLabel errorLabel = new JLabel("0.0");
        statsPanel.add(errorLabel);

        // Add all components
        panel.add(networkControls);
        panel.add(Box.createVerticalStrut(10));
        panel.add(statsPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.setPreferredSize(new Dimension(WIDTH, 25));
        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        return statusBar;
    }

    private void trainNetwork() {
        int sampleIndex = epoch % trainingInputs.length;

        // Set input layer activations
        for (int i = 0; i < layers[0]; i++) {
            neurons.get(0).get(i).activation = trainingInputs[sampleIndex][i];
        }

        // Forward propagation
        forwardPropagate();

        // Backward propagation
        backPropagate(trainingOutputs[sampleIndex]);

        // Update weights and biases
        updateWeights();

        // Calculate error
        errorRate = calculateError(trainingOutputs[sampleIndex]);
        errorHistory.add(errorRate);
        if (errorHistory.size() > MAX_ERROR_HISTORY) {
            errorHistory.remove(0);
        }

        epoch++;
    }

    private void forwardPropagate() {
        for (int i = 0; i < connections.size(); i++) {
            ArrayList<Neuron> nextLayer = neurons.get(i + 1);

            // Reset next layer activations
            for (Neuron neuron : nextLayer) {
                neuron.activation = 0.0;
            }

            // Propagate signals
            for (int j = 0; j < connections.get(i).size(); j++) {
                Neuron from = neurons.get(i).get(j);
                ArrayList<Connection> neuronConns = connections.get(i).get(j);

                for (int k = 0; k < neuronConns.size(); k++) {
                    Connection conn = neuronConns.get(k);
                    Neuron to = nextLayer.get(k);

                    conn.signalStrength = from.activation * conn.weight;
                    to.activation += conn.signalStrength;
                }
            }

            // Apply activation function
            for (Neuron neuron : nextLayer) {
                neuron.activation = sigmoid(neuron.activation + neuron.bias);
            }
        }
    }

    private void backPropagate(double[] targetOutput) {
        // Calculate output layer errors
        ArrayList<Neuron> outputLayer = neurons.get(neurons.size() - 1);
        for (int i = 0; i < outputLayer.size(); i++) {
            Neuron neuron = outputLayer.get(i);
            double output = neuron.activation;
            double target = targetOutput[i];
            neuron.error = output * (1 - output) * (target - output);
        }

        // Calculate hidden layer errors
        for (int i = neurons.size() - 2; i >= 0; i--) {
            ArrayList<Neuron> layer = neurons.get(i);
            ArrayList<ArrayList<Connection>> nextLayerConns = connections.get(i);

            for (int j = 0; j < layer.size(); j++) {
                Neuron neuron = layer.get(j);
                double error = 0.0;

                for (int k = 0; k < nextLayerConns.get(j).size(); k++) {
                    Connection conn = nextLayerConns.get(j).get(k);
                    error += conn.weight * conn.to.error;
                }

                neuron.error = neuron.activation * (1 - neuron.activation) * error;
            }
        }
    }

    private void updateWeights() {
        for (int i = 0; i < connections.size(); i++) {
            for (int j = 0; j < connections.get(i).size(); j++) {
                ArrayList<Connection> neuronConns = connections.get(i).get(j);
                Neuron from = neurons.get(i).get(j);

                for (Connection conn : neuronConns) {
                    double delta = learningRate * conn.to.error * from.activation;
                    conn.weight += delta + momentum * conn.deltaWeight;
                    conn.deltaWeight = delta;
                }
            }
        }

        // Update biases
        for (ArrayList<Neuron> layer : neurons) {
            for (Neuron neuron : layer) {
                double delta = learningRate * neuron.error;
                neuron.bias += delta + momentum * neuron.deltaBias;
                neuron.deltaBias = delta;
            }
        }
    }

    private double calculateError(double[] targetOutput) {
        double error = 0.0;
        ArrayList<Neuron> outputLayer = neurons.get(neurons.size() - 1);

        for (int i = 0; i < outputLayer.size(); i++) {
            double diff = targetOutput[i] - outputLayer.get(i).activation;
            error += diff * diff;
        }

        return error / outputLayer.size();
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private Color interpolateColor(Color c1, Color c2, double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int red = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int green = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int blue = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(red, green, blue);
    }

    private void resetNetwork() {
        epoch = 0;
        errorHistory.clear();

        for (ArrayList<Neuron> layer : neurons) {
            for (Neuron neuron : layer) {
                neuron.activation = 0.0;
                neuron.bias = random.nextDouble() * 2 - 1;
                neuron.error = 0.0;
                neuron.deltaBias = 0.0;
            }
        }

        for (ArrayList<ArrayList<Connection>> layerConns : connections) {
            for (ArrayList<Connection> neuronConns : layerConns) {
                for (Connection conn : neuronConns) {
                    conn.weight = random.nextDouble() * 2 - 1;
                    conn.deltaWeight = 0.0;
                    conn.signalStrength = 0.0;
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new NeuralNetworkVisualization().setVisible(true);
        });
    }
}