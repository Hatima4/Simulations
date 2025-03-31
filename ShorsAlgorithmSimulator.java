import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShorsAlgorithmSimulator extends JFrame {
    private JPanel mainPanel;
    private JPanel quantumPanel;
    private JPanel controlPanel;
    private JPanel infoPanel;
    private JTextField numberField;
    private JButton startButton;
    private JButton stepButton;
    private JLabel statusLabel;
    private JLabel phaseLabel;
    private JLabel resultLabel;
    private JTextArea quantumStateArea;
    private JTextArea measurementArea;

    private int numberToFactor;
    private int N;
    private int a;
    private List<Integer> quantumRegister;
    private List<Double> phaseEstimation;
    private int currentStep;
    private boolean isRunning;
    private Random random;

    // Visual constants
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color PANEL_COLOR = new Color(255, 255, 255);
    private static final Color ACCENT_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font MONOSPACE_FONT = new Font("Consolas", Font.PLAIN, 14);
    private static final int PADDING = 15;
    private static final int BORDER_RADIUS = 10;
    private static final int ANIMATION_DELAY = 1000; // 1 second delay for animations

    private Timer animationTimer;
    private static final String[] simpleExplanations = {
            "<html>Step 1: Initialization<br>Preparing the quantum register in initial state |1⟩</html>",
            "<html>Step 2: Hadamard Transform<br>Applying Hadamard gates to create quantum superposition</html>",
            "<html>Step 3: Modular Exponentiation<br>Computing modular arithmetic in quantum superposition</html>",
            "<html>Step 4: Quantum Fourier Transform<br>Applying inverse QFT to extract phase information</html>",
            "<html>Step 5: Measurement<br>Measuring quantum state and computing prime factors</html>"
    };

    public ShorsAlgorithmSimulator() {
        setTitle("Number Factory - Quantum Style!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(BACKGROUND_COLOR);

        random = new Random();
        currentStep = 0;
        isRunning = false;

        setupUI();
        setupAnimation();
    }

    private void setupUI() {
        // Create main panel with modern padding
        mainPanel = new JPanel(new BorderLayout(PADDING, PADDING));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Create left panel for visualization
        JPanel leftPanel = new JPanel(new BorderLayout(PADDING, PADDING));
        leftPanel.setBackground(BACKGROUND_COLOR);

        // Create quantum visualization panel with modern styling
        createQuantumPanel();

        leftPanel.add(quantumPanel, BorderLayout.CENTER);
        leftPanel.add(createStepPanel(), BorderLayout.SOUTH);

        // Create right panel for controls and info
        JPanel rightPanel = new JPanel(new BorderLayout(PADDING, PADDING));
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.setPreferredSize(new Dimension(350, 0));

        // Create control and info panels
        createControlPanel();
        createInfoPanel();

        rightPanel.add(controlPanel, BorderLayout.NORTH);
        rightPanel.add(infoPanel, BorderLayout.CENTER);

        // Add panels to main panel
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        add(mainPanel);
    }

    private void createQuantumPanel() {
        quantumPanel = new JPanel(new BorderLayout(PADDING, PADDING));
        quantumPanel.setBackground(PANEL_COLOR);
        quantumPanel.setBorder(createRoundedBorder("Quantum State Visualization", TITLE_FONT));

        quantumStateArea = new JTextArea();
        quantumStateArea.setEditable(false);
        quantumStateArea.setFont(MONOSPACE_FONT);
        quantumStateArea.setBackground(PANEL_COLOR);
        quantumStateArea.setForeground(TEXT_COLOR);
        quantumStateArea.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        quantumStateArea.setLineWrap(true);
        quantumStateArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(quantumStateArea);
        scrollPane.setBorder(null);
        quantumPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createStepPanel() {
        JPanel stepPanel = new JPanel(new BorderLayout(5, 5));
        stepPanel.setBackground(BACKGROUND_COLOR);
        stepPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, 0, 0, 0));

        // Progress bar panel
        JPanel progressPanel = new JPanel(new GridLayout(1, 5, 5, 0));
        progressPanel.setBackground(BACKGROUND_COLOR);

        String[] steps = { "Initialize", "Hadamard", "Mod Exp", "QFT", "Measure" };
        for (int i = 0; i < steps.length; i++) {
            JPanel stepContainer = new JPanel(new BorderLayout());
            stepContainer.setBackground(BACKGROUND_COLOR);

            JLabel stepLabel = new JLabel(steps[i], SwingConstants.CENTER);
            stepLabel.setFont(LABEL_FONT);
            stepLabel.setForeground(i <= currentStep ? ACCENT_COLOR : Color.GRAY);

            // Create a more sophisticated step indicator
            JPanel indicator = new JPanel();
            indicator.setPreferredSize(new Dimension(100, 40));
            indicator.setBackground(i <= currentStep ? ACCENT_COLOR : new Color(220, 220, 220));
            indicator.setBorder(
                    BorderFactory.createLineBorder(i <= currentStep ? ACCENT_COLOR.darker() : Color.GRAY, 1));

            // Add step number
            JLabel numberLabel = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            numberLabel.setFont(TITLE_FONT);
            numberLabel.setForeground(i <= currentStep ? Color.WHITE : Color.GRAY);
            indicator.add(numberLabel);

            stepContainer.add(indicator, BorderLayout.CENTER);
            stepContainer.add(stepLabel, BorderLayout.SOUTH);
            progressPanel.add(stepContainer);
        }

        // Explanation label with technical details
        JLabel explanationLabel = new JLabel(simpleExplanations[Math.min(currentStep, 4)]);
        explanationLabel.setFont(LABEL_FONT);
        explanationLabel.setForeground(TEXT_COLOR);
        explanationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        explanationLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        stepPanel.add(progressPanel, BorderLayout.CENTER);
        stepPanel.add(explanationLabel, BorderLayout.SOUTH);

        return stepPanel;
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(PANEL_COLOR);
        controlPanel.setBorder(createRoundedBorder("Controls", TITLE_FONT));

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING, PADDING));
        inputPanel.setBackground(PANEL_COLOR);

        JLabel numberLabel = new JLabel("Number to factor:");
        numberLabel.setFont(LABEL_FONT);
        numberField = createStyledTextField(8);

        inputPanel.add(numberLabel);
        inputPanel.add(numberField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING, PADDING));
        buttonPanel.setBackground(PANEL_COLOR);

        startButton = createStyledButton("Start", ACCENT_COLOR);
        stepButton = createStyledButton("Next Step", SECONDARY_COLOR);
        stepButton.setEnabled(false);

        // Add action listeners
        startButton.addActionListener(e -> startSimulation());
        stepButton.addActionListener(e -> performNextStep());

        buttonPanel.add(startButton);
        buttonPanel.add(stepButton);

        controlPanel.add(inputPanel);
        controlPanel.add(buttonPanel);
    }

    private void createInfoPanel() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(PANEL_COLOR);
        infoPanel.setBorder(createRoundedBorder("Information", TITLE_FONT));

        // Status section
        JPanel statusPanel = new JPanel(new BorderLayout(PADDING, PADDING));
        statusPanel.setBackground(PANEL_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(LABEL_FONT);
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        // Phase section
        JPanel phasePanel = new JPanel(new BorderLayout(PADDING, PADDING));
        phasePanel.setBackground(PANEL_COLOR);
        phasePanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        phaseLabel = new JLabel("Phase: ");
        phaseLabel.setFont(LABEL_FONT);
        phasePanel.add(phaseLabel, BorderLayout.CENTER);

        // Result section
        JPanel resultPanel = new JPanel(new BorderLayout(PADDING, PADDING));
        resultPanel.setBackground(PANEL_COLOR);
        resultPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        resultLabel = new JLabel("Result: ");
        resultLabel.setFont(LABEL_FONT);
        resultPanel.add(resultLabel, BorderLayout.CENTER);

        // Measurements section
        JPanel measurementPanel = new JPanel(new BorderLayout(PADDING, PADDING));
        measurementPanel.setBackground(PANEL_COLOR);
        measurementPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        measurementArea = new JTextArea(12, 25);
        measurementArea.setEditable(false);
        measurementArea.setFont(MONOSPACE_FONT);
        measurementArea.setBackground(new Color(250, 250, 250));
        measurementArea.setForeground(TEXT_COLOR);
        measurementArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(measurementArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        measurementPanel.add(scrollPane, BorderLayout.CENTER);

        infoPanel.add(statusPanel);
        infoPanel.add(phasePanel);
        infoPanel.add(resultPanel);
        infoPanel.add(measurementPanel);
    }

    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        return field;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 35));
        button.setFont(LABEL_FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private Border createRoundedBorder(String title, Font font) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199)),
                        title,
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        font,
                        TEXT_COLOR),
                BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void startSimulation() {
        try {
            numberToFactor = Integer.parseInt(numberField.getText());
            if (numberToFactor <= 1) {
                throw new NumberFormatException("Number must be greater than 1");
            }

            // Initialize simulation
            N = numberToFactor;
            a = findCoprime(N);
            quantumRegister = new ArrayList<>();
            phaseEstimation = new ArrayList<>();
            currentStep = 0;
            isRunning = true;

            // Clear previous measurements
            measurementArea.setText("");
            quantumStateArea.setText("");
            statusLabel.setText("Ready to start...");
            phaseLabel.setText("Phase: ");
            resultLabel.setText("Result: ");

            // Initialize quantum register with zeros
            for (int i = 0; i < 8; i++) {
                quantumRegister.add(0);
            }

            // Update UI
            stepButton.setEnabled(true);
            updateQuantumState();
            measurementArea.append("Number to factor: " + N + "\n");
            measurementArea.append("Coprime number: " + a + "\n\n");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number greater than 1",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeQuantumRegister() {
        statusLabel.setText("Initializing quantum register...");
        // Set initial state to |1⟩
        quantumRegister.set(0, 1);
        for (int i = 1; i < quantumRegister.size(); i++) {
            quantumRegister.set(i, 0);
        }
        measurementArea.append("Initialized quantum register with |1⟩\n");
        measurementArea.append("Register state: |" + getQuantumStateString() + "⟩\n\n");
    }

    private String getQuantumStateString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < quantumRegister.size(); i++) {
            sb.append(quantumRegister.get(i));
        }
        return sb.toString();
    }

    private void updateQuantumState() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Quantum State:\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("Register: |").append(getQuantumStateString()).append("⟩\n\n");

        if (!phaseEstimation.isEmpty()) {
            sb.append("Phase Estimation:\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            for (double phase : phaseEstimation) {
                sb.append(String.format("%.4f ", phase));
            }
        }

        quantumStateArea.setText(sb.toString());
    }

    private void setupAnimation() {
        animationTimer = new Timer(ANIMATION_DELAY, e -> {
            updateStepAnimation();
        });
        animationTimer.setRepeats(false);
    }

    private void updateStepAnimation() {
        createStepPanel();
        updateQuantumState();
    }

    private void performNextStep() {
        if (!isRunning) {
            statusLabel.setText("Simulation completed");
            return;
        }

        switch (currentStep) {
            case 0:
                statusLabel.setText("Initializing quantum register to |1⟩");
                initializeQuantumRegister();
                break;
            case 1:
                statusLabel.setText("Applying Hadamard transform H⊗n");
                applyHadamardGates();
                break;
            case 2:
                statusLabel.setText("Computing modular exponentiation f(x) = ax mod N");
                applyModularExponentiation();
                break;
            case 3:
                statusLabel.setText("Applying inverse Quantum Fourier Transform");
                applyInverseQFT();
                break;
            case 4:
                statusLabel.setText("Measuring quantum state and computing factors");
                measureAndAnalyze();
                break;
            default:
                isRunning = false;
                stepButton.setEnabled(false);
                statusLabel.setText("Simulation completed");
                return;
        }

        currentStep++;
        animationTimer.restart();
    }

    private void applyHadamardGates() {
        statusLabel.setText("Applying Hadamard gates...");
        // Apply Hadamard gates to create superposition
        for (int i = 0; i < quantumRegister.size(); i++) {
            quantumRegister.set(i, random.nextInt(2));
        }
        measurementArea.append("Applied Hadamard gates\n");
        measurementArea.append("New state: |" + getQuantumStateString() + "⟩\n\n");
    }

    private void applyModularExponentiation() {
        statusLabel.setText("Applying modular exponentiation...");
        // Simulate modular exponentiation
        int result = 1;
        for (int i = 0; i < quantumRegister.size(); i++) {
            if (quantumRegister.get(i) == 1) {
                result = (result * a) % N;
            }
        }
        phaseEstimation.add((double) result / N);
        measurementArea.append("Applied modular exponentiation with a = " + a + "\n");
        measurementArea.append("Result: " + result + "\n\n");
    }

    private void applyInverseQFT() {
        statusLabel.setText("Applying inverse Quantum Fourier Transform...");
        // Simulate phase estimation
        double phase = 0.0;
        for (int i = 0; i < phaseEstimation.size(); i++) {
            phase += phaseEstimation.get(i) * Math.pow(2, -i);
        }
        phaseLabel.setText(String.format("Phase: %.4f", phase));
        measurementArea.append("Applied inverse QFT\n");
        measurementArea.append("Estimated phase: " + String.format("%.4f", phase) + "\n\n");
    }

    private void measureAndAnalyze() {
        statusLabel.setText("Finding the secret numbers...");
        measurementArea.append("Looking for factors...\n");

        List<Integer> factors = findPrimeFactors(N);
        if (!factors.isEmpty()) {
            // Count factor frequencies
            java.util.Map<Integer, Integer> factorCount = new java.util.HashMap<>();
            for (int factor : factors) {
                factorCount.put(factor, factorCount.getOrDefault(factor, 0) + 1);
            }

            // Build pretty factorization string
            StringBuilder factorization = new StringBuilder();
            boolean first = true;
            for (java.util.Map.Entry<Integer, Integer> entry : factorCount.entrySet()) {
                if (!first) {
                    factorization.append(" × ");
                }
                if (entry.getValue() > 1) {
                    factorization.append(entry.getKey()).append("^").append(entry.getValue());
                } else {
                    factorization.append(entry.getKey());
                }
                first = false;
            }

            resultLabel.setText("Found the answer! " + factorization.toString());
            measurementArea.append("The secret numbers are: " + factorization.toString() + "\n");
            measurementArea.append(N + " = " + factorization.toString() + "\n");
            measurementArea.append("Great job! These are all prime numbers!\n");
        } else {
            resultLabel.setText("This is a special number - it's prime!");
            measurementArea.append("This number is prime - it can't be broken down further!\n");
        }
    }

    private List<Integer> findPrimeFactors(int n) {
        List<Integer> factors = new ArrayList<>();

        // Try small prime factors first
        int[] smallPrimes = { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43 };
        for (int prime : smallPrimes) {
            while (n % prime == 0) {
                factors.add(prime);
                n /= prime;
            }
        }

        // If there are remaining factors, find them
        if (n > 1) {
            // Check if the remaining number is prime
            boolean isPrime = true;
            for (int i = 2; i <= Math.sqrt(n); i++) {
                if (n % i == 0) {
                    isPrime = false;
                    break;
                }
            }

            if (isPrime) {
                factors.add(n);
            } else {
                // Find remaining factors
                int i = smallPrimes[smallPrimes.length - 1] + 2;
                while (n > 1 && i <= Math.sqrt(n)) {
                    while (n % i == 0) {
                        factors.add(i);
                        n /= i;
                    }
                    i += 2; // Skip even numbers
                }
                if (n > 1) {
                    factors.add(n);
                }
            }
        }

        return factors;
    }

    private boolean isPrime(int n) {
        if (n <= 1)
            return false;
        if (n <= 3)
            return true;
        if (n % 2 == 0 || n % 3 == 0)
            return false;

        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }

    private int findCoprime(int n) {
        int a = 2;
        while (gcd(a, n) != 1) {
            a++;
        }
        return a;
    }

    private int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ShorsAlgorithmSimulator simulator = new ShorsAlgorithmSimulator();
            simulator.setLocationRelativeTo(null);
            simulator.setVisible(true);
        });
    }
}