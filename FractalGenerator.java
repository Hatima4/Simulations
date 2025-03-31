import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

public class FractalGenerator extends JFrame {
    private JPanel controlPanel;
    private JPanel fractalPanel;
    private BufferedImage fractalImage;
    private JComboBox<String> fractalTypeCombo;
    private JSlider maxIterationsSlider;
    private JSlider zoomSlider;
    private JLabel statusLabel;
    private double centerX = 0;
    private double centerY = 0;
    private double zoom = 1.0;
    private int maxIterations = 100;
    private boolean isRendering = false;
    private ExecutorService executor;
    private Random random;
    private double[] randomParams;
    private int fractalVariant;

    // Visual constants
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color PANEL_COLOR = new Color(255, 255, 255);
    private static final Color ACCENT_COLOR = new Color(52, 152, 219);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final int PADDING = 10;

    // Fractal parameters
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final String[] FRACTAL_TYPES = {
            "Mandelbrot Set",
            "Julia Set",
            "Burning Ship",
            "Tricorn",
            "Random Fractal"
    };

    // Enhanced visual constants
    private static final Color DARK_BG = new Color(33, 33, 33);
    private static final Color DARK_PANEL = new Color(45, 45, 45);
    private static final Color LIGHT_TEXT = new Color(240, 240, 240);
    private static final Color BUTTON_HOVER = new Color(92, 192, 249);
    private static final int CONTROL_PANEL_WIDTH = 280;

    private JProgressBar progressBar;
    private boolean isDarkMode = true;
    private int currentRow = 0;
    private Timer animationTimer;
    private BufferedImage tempImage;

    public FractalGenerator() {
        setTitle("Fractal Explorer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(PADDING, PADDING));
        getContentPane().setBackground(BACKGROUND_COLOR);

        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        random = new Random();
        randomParams = new double[6];
        generateRandomParameters();

        // Initialize status label
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(LABEL_FONT);

        setupUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void generateRandomParameters() {
        // Generate random parameters for the fractal equations
        for (int i = 0; i < randomParams.length; i++) {
            randomParams[i] = random.nextDouble() * 4 - 2; // Range: -2 to 2
        }
        fractalVariant = random.nextInt(4);
    }

    private void setupUI() {
        setBackground(isDarkMode ? DARK_BG : BACKGROUND_COLOR);

        // Create main content panel with padding
        JPanel mainContent = new JPanel(new BorderLayout(PADDING, PADDING));
        mainContent.setBackground(isDarkMode ? DARK_BG : BACKGROUND_COLOR);
        mainContent.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        // Create control panel
        createControlPanel();

        // Create fractal panel
        createFractalPanel();

        // Create status panel
        JPanel statusPanel = createStatusPanel();

        // Add panels to main content
        mainContent.add(controlPanel, BorderLayout.EAST);
        mainContent.add(fractalPanel, BorderLayout.CENTER);
        mainContent.add(statusPanel, BorderLayout.SOUTH);

        // Add main content to frame
        add(mainContent);

        // Initialize fractal images
        fractalImage = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);
        tempImage = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Setup animation timer
        setupAnimationTimer();

        generateFractal();
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout(PADDING, 0));
        statusPanel.setBackground(isDarkMode ? DARK_PANEL : PANEL_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, 0, 0, 0));

        // Create progress bar
        progressBar = new JProgressBar(0, DEFAULT_HEIGHT);
        progressBar.setStringPainted(true);
        progressBar.setForeground(ACCENT_COLOR);
        progressBar.setBackground(isDarkMode ? DARK_BG : BACKGROUND_COLOR);
        progressBar.setBorder(BorderFactory.createEmptyBorder());

        // Status label with icon
        statusLabel.setForeground(isDarkMode ? LIGHT_TEXT : Color.BLACK);
        statusLabel.setIcon(new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)));

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.setBackground(isDarkMode ? DARK_PANEL : PANEL_COLOR);
        labelPanel.add(statusLabel);

        statusPanel.add(progressBar, BorderLayout.CENTER);
        statusPanel.add(labelPanel, BorderLayout.EAST);

        return statusPanel;
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(isDarkMode ? DARK_PANEL : PANEL_COLOR);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, PADDING, 0, 0),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isDarkMode ? DARK_BG : Color.GRAY),
                        BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING))));
        controlPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH, DEFAULT_HEIGHT));

        // Title
        JLabel titleLabel = new JLabel("Fractal Controls");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(isDarkMode ? LIGHT_TEXT : Color.BLACK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(titleLabel);
        controlPanel.add(Box.createVerticalStrut(PADDING * 2));

        // Fractal type selector with custom styling
        addStyledComboBox();

        // Sliders with custom styling
        addStyledSliders();

        // Buttons panel
        addStyledButtons();

        // Theme toggle
        addThemeToggle();
    }

    private void addStyledComboBox() {
        JLabel typeLabel = createStyledLabel("Fractal Type:");
        fractalTypeCombo = new JComboBox<>(FRACTAL_TYPES);
        fractalTypeCombo.setFont(LABEL_FONT);
        fractalTypeCombo.setBackground(isDarkMode ? DARK_BG : Color.WHITE);
        fractalTypeCombo.setForeground(isDarkMode ? LIGHT_TEXT : Color.BLACK);
        fractalTypeCombo.setFocusable(false);
        fractalTypeCombo.addActionListener(e -> generateFractal());

        JPanel typePanel = createStyledPanel();
        typePanel.add(typeLabel);
        typePanel.add(fractalTypeCombo);
        controlPanel.add(typePanel);
        controlPanel.add(Box.createVerticalStrut(PADDING));
    }

    private void addStyledSliders() {
        // Max iterations slider
        JLabel iterLabel = createStyledLabel("Max Iterations:");
        maxIterationsSlider = createStyledSlider(10, 1000, 100, 200);
        maxIterationsSlider.addChangeListener(e -> {
            if (!maxIterationsSlider.getValueIsAdjusting()) {
                maxIterations = maxIterationsSlider.getValue();
                generateFractal();
            }
        });

        JPanel iterPanel = createStyledPanel();
        iterPanel.add(iterLabel);
        iterPanel.add(maxIterationsSlider);
        controlPanel.add(iterPanel);
        controlPanel.add(Box.createVerticalStrut(PADDING));

        // Zoom slider
        JLabel zoomLabel = createStyledLabel("Zoom Level:");
        zoomSlider = createStyledSlider(-100, 100, 0, 50);
        zoomSlider.addChangeListener(e -> {
            if (!zoomSlider.getValueIsAdjusting()) {
                zoom = Math.pow(2, zoomSlider.getValue() / 20.0);
                generateFractal();
            }
        });

        JPanel zoomPanel = createStyledPanel();
        zoomPanel.add(zoomLabel);
        zoomPanel.add(zoomSlider);
        controlPanel.add(zoomPanel);
        controlPanel.add(Box.createVerticalStrut(PADDING));
    }

    private void addStyledButtons() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, PADDING, PADDING));
        buttonPanel.setBackground(isDarkMode ? DARK_PANEL : PANEL_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton randomizeButton = createStyledButton("New Random Fractal");
        randomizeButton.addActionListener(e -> {
            generateRandomParameters();
            generateFractal();
        });

        JButton resetButton = createStyledButton("Reset View");
        resetButton.addActionListener(e -> {
            centerX = 0;
            centerY = 0;
            zoom = 1.0;
            zoomSlider.setValue(0);
            generateFractal();
        });

        buttonPanel.add(randomizeButton);
        buttonPanel.add(resetButton);
        controlPanel.add(buttonPanel);
        controlPanel.add(Box.createVerticalStrut(PADDING));
    }

    private void addThemeToggle() {
        JButton themeToggle = createStyledButton(isDarkMode ? "Light Mode" : "Dark Mode");
        themeToggle.addActionListener(e -> {
            isDarkMode = !isDarkMode;
            themeToggle.setText(isDarkMode ? "Light Mode" : "Dark Mode");
            updateTheme();
        });

        controlPanel.add(themeToggle);
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(isDarkMode ? LIGHT_TEXT : Color.BLACK);
        return label;
    }

    private JPanel createStyledPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(isDarkMode ? DARK_PANEL : PANEL_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JSlider createStyledSlider(int min, int max, int value, int majorTick) {
        JSlider slider = new JSlider(min, max, value);
        slider.setFont(LABEL_FONT);
        slider.setForeground(isDarkMode ? LIGHT_TEXT : Color.BLACK);
        slider.setBackground(isDarkMode ? DARK_PANEL : PANEL_COLOR);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(majorTick);
        return slider;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(LABEL_FONT);
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_HOVER);
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
        });

        return button;
    }

    private void updateTheme() {
        SwingUtilities.invokeLater(() -> {
            // Update all components with new theme
            controlPanel.setBackground(isDarkMode ? DARK_PANEL : PANEL_COLOR);
            fractalPanel.setBackground(isDarkMode ? DARK_BG : BACKGROUND_COLOR);
            statusLabel.setForeground(isDarkMode ? LIGHT_TEXT : Color.BLACK);
            progressBar.setBackground(isDarkMode ? DARK_BG : BACKGROUND_COLOR);

            // Refresh the UI
            SwingUtilities.updateComponentTreeUI(this);
        });
    }

    private void setupAnimationTimer() {
        animationTimer = new Timer(16, e -> { // ~60 FPS
            if (tempImage != null) {
                Graphics g = fractalPanel.getGraphics();
                if (g != null) {
                    g.drawImage(tempImage, 0, 0, null);
                    g.dispose();
                }
            }
        });
    }

    private void createFractalPanel() {
        fractalPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (fractalImage != null) {
                    g.drawImage(fractalImage, 0, 0, this);
                }
            }
        };
        fractalPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        fractalPanel.setBackground(Color.BLACK);

        // Add mouse listeners for interaction
        fractalPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Update center point based on click location
                double dx = (e.getX() - DEFAULT_WIDTH / 2.0) / (DEFAULT_WIDTH / 4.0) / zoom;
                double dy = (e.getY() - DEFAULT_HEIGHT / 2.0) / (DEFAULT_HEIGHT / 4.0) / zoom;
                centerX += dx;
                centerY += dy;
                generateFractal();
            }
        });
    }

    private void generateFractal() {
        if (isRendering)
            return;

        isRendering = true;
        currentRow = 0;
        statusLabel.setText("Rendering...");
        progressBar.setValue(0);

        // Start animation
        animationTimer.start();

        executor.submit(() -> {
            try {
                String fractalType = (String) fractalTypeCombo.getSelectedItem();

                // Create a temporary image for animation
                Graphics2D g2d = tempImage.createGraphics();
                g2d.setColor(isDarkMode ? DARK_BG : BACKGROUND_COLOR);
                g2d.fillRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
                g2d.dispose();

                for (int y = 0; y < DEFAULT_HEIGHT; y++) {
                    for (int x = 0; x < DEFAULT_WIDTH; x++) {
                        double zx = (x - DEFAULT_WIDTH / 2.0) / (DEFAULT_WIDTH / 4.0) / zoom + centerX;
                        double zy = (y - DEFAULT_HEIGHT / 2.0) / (DEFAULT_HEIGHT / 4.0) / zoom + centerY;

                        int color;
                        switch (fractalType) {
                            case "Julia Set":
                                color = computeJuliaSet(zx, zy);
                                break;
                            case "Burning Ship":
                                color = computeBurningShip(zx, zy);
                                break;
                            case "Tricorn":
                                color = computeTricorn(zx, zy);
                                break;
                            case "Random Fractal":
                                color = computeRandomFractal(zx, zy);
                                break;
                            default:
                                color = computeMandelbrot(zx, zy);
                        }

                        tempImage.setRGB(x, y, color);
                        fractalImage.setRGB(x, y, color);
                    }

                    final int progress = y;
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(progress);
                        progressBar.setString(String.format("%.1f%%", (progress * 100.0) / DEFAULT_HEIGHT));
                    });
                }

                SwingUtilities.invokeLater(() -> {
                    fractalPanel.repaint();
                    if (fractalTypeCombo.getSelectedItem().equals("Random Fractal")) {
                        statusLabel.setText("Random Fractal Type " + (fractalVariant + 1));
                    } else {
                        statusLabel.setText("Ready");
                    }
                    progressBar.setValue(DEFAULT_HEIGHT);
                    progressBar.setString("100%");
                    isRendering = false;
                    animationTimer.stop();
                });
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error occurred");
                isRendering = false;
                animationTimer.stop();
            }
        });
    }

    private int computeMandelbrot(double zx, double zy) {
        double x = 0;
        double y = 0;
        int iteration = 0;
        double smooth = 0;

        while (iteration < maxIterations) {
            double xtemp = x * x - y * y + zx;
            y = 2 * x * y + zy;
            x = xtemp;

            // Smooth iteration count for continuous coloring
            smooth = iteration + 1 - Math.log(Math.log(Math.sqrt(x * x + y * y))) / Math.log(2);

            // Use periodic boundary instead of escape condition
            if (x * x + y * y > 4) {
                x = Math.sin(x);
                y = Math.sin(y);
            }

            iteration++;
        }

        return getColor(smooth);
    }

    private int computeJuliaSet(double zx, double zy) {
        double cx = -0.4;
        double cy = 0.6;
        int iteration = 0;
        double smooth = 0;

        while (iteration < maxIterations) {
            double xtemp = zx * zx - zy * zy + cx;
            zy = 2 * zx * zy + cy;
            zx = xtemp;

            smooth = iteration + 1 - Math.log(Math.log(Math.sqrt(zx * zx + zy * zy))) / Math.log(2);

            // Periodic wrapping
            if (zx * zx + zy * zy > 4) {
                zx = Math.tan(zx) / 2;
                zy = Math.tan(zy) / 2;
            }

            iteration++;
        }

        return getColor(smooth);
    }

    private int computeBurningShip(double zx, double zy) {
        double x = 0;
        double y = 0;
        int iteration = 0;
        double smooth = 0;

        while (iteration < maxIterations) {
            double xtemp = x * x - y * y + zx;
            y = Math.abs(2 * x * y) + zy;
            x = xtemp;

            smooth = iteration + 1 - Math.log(Math.log(Math.sqrt(x * x + y * y))) / Math.log(2);

            // Periodic transformation
            if (x * x + y * y > 4) {
                x = Math.cos(x);
                y = Math.sin(y);
            }

            iteration++;
        }

        return getColor(smooth);
    }

    private int computeTricorn(double zx, double zy) {
        double x = 0;
        double y = 0;
        int iteration = 0;
        double smooth = 0;

        while (iteration < maxIterations) {
            double xtemp = x * x - y * y + zx;
            y = -2 * x * y + zy;
            x = xtemp;

            smooth = iteration + 1 - Math.log(Math.log(Math.sqrt(x * x + y * y))) / Math.log(2);

            // Periodic boundary
            if (x * x + y * y > 4) {
                x = Math.sin(x * Math.PI) / 2;
                y = Math.cos(y * Math.PI) / 2;
            }

            iteration++;
        }

        return getColor(smooth);
    }

    private int computeRandomFractal(double zx, double zy) {
        double x = zx;
        double y = zy;
        int iteration = 0;
        double smooth = 0;

        while (iteration < maxIterations) {
            double xtemp = 0;
            double ytemp = 0;

            switch (fractalVariant) {
                case 0: // Periodic polynomial variation
                    xtemp = Math.sin(x * x * x - 3 * x * y * y + randomParams[0] * x);
                    ytemp = Math.cos(3 * x * x * y - y * y * y + randomParams[2] * y);
                    break;

                case 1: // Periodic trigonometric variation
                    xtemp = Math.sin(x * randomParams[0]) * Math.tanh(y * randomParams[1]);
                    ytemp = Math.cos(x * randomParams[2]) * Math.tanh(y * randomParams[3]);
                    break;

                case 2: // Periodic exponential variation
                    xtemp = Math.sin(Math.exp(x * randomParams[0])) * Math.cos(y * randomParams[1]);
                    ytemp = Math.cos(Math.exp(x * randomParams[2])) * Math.sin(y * randomParams[3]);
                    break;

                case 3: // Periodic hybrid variation
                    xtemp = Math.sin(x * x - y * y) * Math.cos(x * randomParams[0]);
                    ytemp = Math.cos(2 * x * y) * Math.sin(y * randomParams[2]);
                    break;
            }

            x = xtemp;
            y = ytemp;

            smooth = iteration + 1 - Math.log(Math.log(Math.sqrt(x * x + y * y) + 1)) / Math.log(2);
            iteration++;
        }

        return getColor(smooth);
    }

    private int getColor(double iteration) {
        // Continuous coloring using smooth iteration count
        double t = iteration / maxIterations;

        // Enhanced color palette generation
        float hue, saturation, brightness;

        if (fractalTypeCombo.getSelectedItem().equals("Random Fractal")) {
            // Enhanced random fractal coloring with better color harmony
            switch (fractalVariant) {
                case 0: // Warm colors (reds, oranges, yellows)
                    hue = (float) ((0.95 + 0.15 * Math.sin(t * Math.PI * 2 + randomParams[4])) % 1.0);
                    saturation = 0.8f + 0.2f * (float) Math.sin(t * Math.PI * 3);
                    brightness = 0.9f + 0.1f * (float) Math.cos(t * Math.PI * 4);
                    break;

                case 1: // Cool colors (blues, purples)
                    hue = (float) ((0.5 + 0.2 * Math.sin(t * Math.PI * 3 + randomParams[4])) % 1.0);
                    saturation = 0.85f + 0.15f * (float) Math.sin(t * Math.PI * 2);
                    brightness = 0.85f + 0.15f * (float) Math.cos(t * Math.PI * 3);
                    break;

                case 2: // Nature colors (greens, teals)
                    hue = (float) ((0.25 + 0.15 * Math.sin(t * Math.PI * 2 + randomParams[4])) % 1.0);
                    saturation = 0.75f + 0.25f * (float) Math.sin(t * Math.PI * 4);
                    brightness = 0.8f + 0.2f * (float) Math.cos(t * Math.PI * 3);
                    break;

                case 3: // Sunset colors (purples, pinks, oranges)
                    hue = (float) ((0.75 + 0.25 * Math.sin(t * Math.PI * 3 + randomParams[4])) % 1.0);
                    saturation = 0.7f + 0.3f * (float) Math.sin(t * Math.PI * 2);
                    brightness = 0.85f + 0.15f * (float) Math.cos(t * Math.PI * 4);
                    break;

                default: // Fallback to vibrant spectrum
                    hue = (float) (t * 3 % 1.0);
                    saturation = 0.8f;
                    brightness = 0.9f;
            }

            // Apply randomParams[5] for subtle variation while maintaining color harmony
            hue = (hue + (float) (randomParams[5] * 0.1)) % 1.0f;

            // Ensure good contrast
            brightness = Math.max(0.7f, brightness);
            saturation = Math.max(0.6f, saturation);
        } else {
            // Standard fractal coloring with enhanced contrast
            double repeats = 3.0; // Number of color cycles
            hue = (float) (t * repeats % 1.0);

            // Enhance contrast based on iteration count
            double contrastFactor = Math.sin(t * Math.PI);
            saturation = 0.9f;
            brightness = Math.max(0.0f, Math.min(1.0f, (float) (0.7f + 0.3f * contrastFactor)));

            // Adjust colors based on fractal type
            switch ((String) fractalTypeCombo.getSelectedItem()) {
                case "Mandelbrot Set":
                    // Deep blues to purples to golds
                    hue = (float) ((0.6 + 0.2 * Math.sin(t * Math.PI * 4)) % 1.0);
                    break;
                case "Julia Set":
                    // Cyan to magenta to yellow
                    hue = (float) ((0.3 + 0.3 * Math.sin(t * Math.PI * 3)) % 1.0);
                    break;
                case "Burning Ship":
                    // Reds to oranges to yellows
                    hue = (float) ((0.05 + 0.15 * Math.sin(t * Math.PI * 2)) % 1.0);
                    saturation = 0.85f + 0.15f * (float) Math.sin(t * Math.PI * 4);
                    break;
                case "Tricorn":
                    // Blues to greens to teals
                    hue = (float) ((0.45 + 0.25 * Math.sin(t * Math.PI * 3)) % 1.0);
                    break;
            }
        }

        // Convert to RGB
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FractalGenerator().setVisible(true);
        });
    }
}