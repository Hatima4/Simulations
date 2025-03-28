package Pendulum;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

public class DoublePendulum extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 700; // Increased height for better layout
    private static final double GRAVITY = 100.0;
    private static final double DT = 1.0 / 240.0;
    private static final Color ACCENT_COLOR = new Color(41, 128, 185);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color PENDULUM_COLOR = new Color(50, 50, 50);
    private static final Color MASS_COLOR_1 = new Color(231, 76, 60);
    private static final Color MASS_COLOR_2 = new Color(192, 57, 43);

    private double theta1 = Math.PI / 4;
    private double theta2 = Math.PI / 4;
    private double omega1 = 0;
    private double omega2 = 0;
    private double alpha1 = 0; // Store acceleration for display
    private double alpha2 = 0;
    private double l1 = 100;
    private double l2 = 100;
    private double m1 = 2.0;
    private double m2 = 2.0;

    private JPanel pendulumPanel;
    private Timer timer;
    private JPanel controlPanel;
    private JLabel infoLabel;
    private boolean isPaused = false;
    private DecimalFormat df = new DecimalFormat("#.##");

    // Add slider declarations
    private JSlider l1Slider;
    private JSlider l2Slider;
    private JSlider m1Slider;
    private JSlider m2Slider;

    public DoublePendulum() {
        setTitle("Double Pendulum Simulation");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 5));
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create info panel with modern style
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        infoLabel = new JLabel();
        infoLabel.setFont(new Font("Consolas", Font.PLAIN, 13));
        infoPanel.add(infoLabel);
        add(infoPanel, BorderLayout.NORTH);

        // Create simulation panel
        pendulumPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setStroke(new BasicStroke(2.5f));

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 3;

                // Draw pivot point with shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillOval(centerX - 4, centerY - 3, 10, 10);
                g2d.setColor(ACCENT_COLOR);
                g2d.fillOval(centerX - 5, centerY - 5, 10, 10);

                int x1 = (int) (centerX + l1 * Math.sin(theta1));
                int y1 = (int) (centerY + l1 * Math.cos(theta1));
                int x2 = (int) (x1 + l2 * Math.sin(theta2));
                int y2 = (int) (y1 + l2 * Math.cos(theta2));

                // Draw pendulum rods with shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.drawLine(centerX + 1, centerY + 1, x1 + 1, y1 + 1);
                g2d.drawLine(x1 + 1, y1 + 1, x2 + 1, y2 + 1);

                g2d.setColor(PENDULUM_COLOR);
                g2d.drawLine(centerX, centerY, x1, y1);
                g2d.drawLine(x1, y1, x2, y2);

                // Draw masses with 3D effect
                drawMass(g2d, x1, y1, MASS_COLOR_1);
                drawMass(g2d, x2, y2, MASS_COLOR_2);

                updateInfoLabel();
            }
        };
        pendulumPanel.setBackground(Color.WHITE);
        pendulumPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(pendulumPanel, BorderLayout.CENTER);

        // Create modern control panel
        controlPanel = new JPanel();
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        controlPanel.setLayout(new GridLayout(0, 2, 20, 10));

        // Add styled controls
        addStyledControl(controlPanel, "Length 1", l1Slider = createStyledSlider(50, 200, (int) l1));
        addStyledControl(controlPanel, "Length 2", l2Slider = createStyledSlider(50, 200, (int) l2));
        addStyledControl(controlPanel, "Mass 1", m1Slider = createStyledSlider(1, 10, (int) m1));
        addStyledControl(controlPanel, "Mass 2", m2Slider = createStyledSlider(1, 10, (int) m2));

        // Add button panel with modern styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton resetButton = createStyledButton("Reset");
        JButton pauseButton = createStyledButton("Pause/Resume");

        resetButton.addActionListener(e -> {
            theta1 = Math.PI / 4;
            theta2 = Math.PI / 4;
            omega1 = omega2 = 0;
        });

        pauseButton.addActionListener(e -> isPaused = !isPaused);

        buttonPanel.add(resetButton);
        buttonPanel.add(pauseButton);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(buttonPanel);
        controlPanel.add(buttonWrapper);

        add(controlPanel, BorderLayout.SOUTH);

        // Add mouse listener for initial conditions
        pendulumPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int centerX = pendulumPanel.getWidth() / 2;
                int centerY = pendulumPanel.getHeight() / 3;
                double dx = e.getX() - centerX;
                double dy = e.getY() - centerY;
                theta1 = Math.atan2(dx, dy);
                omega1 = omega2 = 0;
            }
        });

        // Setup animation timer
        timer = new Timer(8, e -> {
            if (!isPaused)
                updatePendulum();
            pendulumPanel.repaint();
        });
        timer.setCoalesce(true);
        timer.start();

        // Add tooltips
        pendulumPanel.setToolTipText("Click anywhere to set initial position");
        resetButton.setToolTipText("Reset pendulum to starting position");
        pauseButton.setToolTipText("Pause or resume the simulation");
    }

    private JSlider createStyledSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setBackground(Color.WHITE);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing((max - min) / 4);
        return slider;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    private void addStyledControl(JPanel panel, String labelText, JSlider slider) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(label);
        panel.add(slider);
    }

    private void drawMass(Graphics2D g2d, int x, int y, Color baseColor) {
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(x - 11, y - 11, 24, 24);

        // Draw mass with gradient
        GradientPaint gradient = new GradientPaint(
                x - 12, y - 12, baseColor,
                x + 12, y + 12, baseColor.darker());
        g2d.setPaint(gradient);
        g2d.fillOval(x - 12, y - 12, 24, 24);

        // Add highlight
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillOval(x - 10, y - 10, 8, 8);
    }

    private void updateInfoLabel() {
        String info = String.format(
                "<html><pre>" +
                        "Pendulum 1:  θ=%-6s°  ω=%-6s rad/s  α=%-6s rad/s²%n" +
                        "Pendulum 2:  θ=%-6s°  ω=%-6s rad/s  α=%-6s rad/s²" +
                        "</pre></html>",
                df.format(Math.toDegrees(theta1)),
                df.format(omega1),
                df.format(alpha1),
                df.format(Math.toDegrees(theta2)),
                df.format(omega2),
                df.format(alpha2));
        infoLabel.setText(info);
    }

    private void updatePendulum() {
        // Multiple physics steps per frame for smoother motion
        for (int i = 0; i < 6; i++) { // Increased physics steps
            // Calculate accelerations
            double num1 = -GRAVITY * (2 * m1 + m2) * Math.sin(theta1);
            double num2 = -m2 * GRAVITY * Math.sin(theta1 - 2 * theta2);
            double num3 = -2 * Math.sin(theta1 - theta2) * m2
                    * (omega2 * omega2 * l2 + omega1 * omega1 * l1 * Math.cos(theta1 - theta2));
            double den = l1 * (2 * m1 + m2 - m2 * Math.cos(2 * theta1 - 2 * theta2));
            alpha1 = (num1 + num2 + num3) / den;

            num1 = 2 * Math.sin(theta1 - theta2);
            num2 = omega1 * omega1 * l1 * (m1 + m2);
            num3 = GRAVITY * (m1 + m2) * Math.cos(theta1);
            double num4 = omega2 * omega2 * l2 * m2 * Math.cos(theta1 - theta2);
            den = l2 * (2 * m1 + m2 - m2 * Math.cos(2 * theta1 - 2 * theta2));
            alpha2 = (num1 * (num2 + num3 + num4)) / den;

            // Update velocities and angles
            omega1 += alpha1 * DT;
            omega2 += alpha2 * DT;
            theta1 += omega1 * DT;
            theta2 += omega2 * DT;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new DoublePendulum().setVisible(true);
        });
    }
}