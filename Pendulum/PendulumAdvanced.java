package Pendulum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;

public class PendulumAdvanced extends JPanel {
    private final PendulumPanel pendulumPanel;
    private final ControlPanel controlPanel;
    private final PhysicsPanel physicsPanel;

    public PendulumAdvanced() {
        setLayout(new BorderLayout());
        pendulumPanel = new PendulumPanel();
        controlPanel = new ControlPanel(pendulumPanel);
        physicsPanel = new PhysicsPanel(pendulumPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(controlPanel, BorderLayout.CENTER);
        rightPanel.add(physicsPanel, BorderLayout.SOUTH);

        add(pendulumPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    // Inner class for the pendulum visualization
    private static class PendulumPanel extends JPanel implements ActionListener {
        // Physical parameters
        private double angle = Math.PI / 4; // Initial angle (45 degrees)
        private double angleVelocity = 0; // Angular velocity
        private double angleAcceleration; // Angular acceleration
        private double length = 200; // Length of pendulum (pixels)
        private double gravity = 9.81; // Acceleration due to gravity (m/s^2)
        private double mass = 1.0; // Mass of bob (kg)
        private double damping = 0.01; // Damping factor
        private boolean trailEnabled = false; // Show motion trail
        private Color trailColor = new Color(255, 0, 0, 50);
        private boolean showVectors = true; // Show force vectors
        private double time = 0; // Time elapsed
        private double period = 0; // Period of oscillation
        private int oscillationCount = 0; // Number of oscillations
        private double lastAngle = angle; // Previous angle for period calculation
        private double maxAngle = angle; // Maximum angle reached

        // Display parameters
        private final int pivotX;
        private final int pivotY;
        private final int bobRadius = 20;
        private boolean isDragging = false;
        private Timer timer;
        private java.util.List<Point> trail = new java.util.ArrayList<>();
        private final int maxTrailPoints = 200;
        private final DecimalFormat df = new DecimalFormat("#.##");

        public PendulumPanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(new Color(33, 33, 33)); // Dark background

            pivotX = 400;
            pivotY = 100;

            setupMouseListeners();

            timer = new Timer(1000 / 60, this);
            timer.start();
        }

        private void setupMouseListeners() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    double bobX = pivotX + length * Math.sin(angle);
                    double bobY = pivotY + length * Math.cos(angle);
                    if (Point.distance(e.getX(), e.getY(), bobX, bobY) < bobRadius) {
                        isDragging = true;
                        timer.stop();
                        trail.clear();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDragging) {
                        isDragging = false;
                        angleVelocity = 0;
                        timer.start();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging) {
                        double dx = e.getX() - pivotX;
                        double dy = e.getY() - pivotY;
                        angle = Math.atan2(dx, dy);
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw grid
            drawGrid(g2d);

            // Draw trail
            if (trailEnabled && !trail.isEmpty()) {
                g2d.setColor(trailColor);
                Point prevPoint = trail.get(0);
                for (Point p : trail) {
                    g2d.drawLine(prevPoint.x, prevPoint.y, p.x, p.y);
                    prevPoint = p;
                }
            }

            // Calculate bob position
            int bobX = pivotX + (int) (length * Math.sin(angle));
            int bobY = pivotY + (int) (length * Math.cos(angle));

            // Draw string with gradient
            GradientPaint stringGradient = new GradientPaint(
                    pivotX, pivotY, new Color(200, 200, 200),
                    bobX, bobY, new Color(150, 150, 150));
            g2d.setPaint(stringGradient);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(pivotX, pivotY, bobX, bobY);

            // Draw pivot
            g2d.setPaint(new GradientPaint(
                    pivotX - 5, pivotY - 5, Color.LIGHT_GRAY,
                    pivotX + 5, pivotY + 5, Color.DARK_GRAY));
            g2d.fillOval(pivotX - 5, pivotY - 5, 10, 10);

            // Draw bob with gradient and shadow
            drawBobWithShadow(g2d, bobX, bobY);

            // Draw force vectors if enabled
            if (showVectors) {
                drawForceVectors(g2d, bobX, bobY);
            }

            // Draw angle arc
            drawAngleArc(g2d, bobX, bobY);
        }

        private void drawGrid(Graphics2D g2d) {
            g2d.setColor(new Color(50, 50, 50));
            int gridSize = 50;
            for (int x = 0; x < getWidth(); x += gridSize) {
                g2d.drawLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += gridSize) {
                g2d.drawLine(0, y, getWidth(), y);
            }
        }

        private void drawBobWithShadow(Graphics2D g2d, int bobX, int bobY) {
            // Draw shadow
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillOval(bobX - bobRadius + 4, bobY - bobRadius + 4,
                    2 * bobRadius, 2 * bobRadius);

            // Draw bob with gradient
            GradientPaint bobGradient = new GradientPaint(
                    bobX - bobRadius, bobY - bobRadius, new Color(255, 50, 50),
                    bobX + bobRadius, bobY + bobRadius, new Color(200, 0, 0));
            g2d.setPaint(bobGradient);
            g2d.fillOval(bobX - bobRadius, bobY - bobRadius,
                    2 * bobRadius, 2 * bobRadius);

            // Add highlight
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(bobX - bobRadius / 2, bobY - bobRadius / 2,
                    bobRadius / 2, bobRadius / 2);
        }

        private void drawForceVectors(Graphics2D g2d, int bobX, int bobY) {
            double scale = 50.0; // Scale factor for vector visualization

            // Gravitational force
            g2d.setColor(Color.BLUE);
            int gravityY = (int) (mass * gravity * scale);
            drawArrow(g2d, bobX, bobY, bobX, bobY + gravityY, "Fg");

            // Tension force
            g2d.setColor(Color.GREEN);
            int tensionX = (int) (-mass * gravity * Math.sin(angle) * Math.cos(angle) * scale);
            int tensionY = (int) (-mass * gravity * Math.cos(angle) * Math.cos(angle) * scale);
            drawArrow(g2d, bobX, bobY, bobX + tensionX, bobY + tensionY, "T");

            // Velocity vector
            g2d.setColor(Color.YELLOW);
            int velX = (int) (angleVelocity * length * Math.cos(angle) * scale);
            int velY = (int) (-angleVelocity * length * Math.sin(angle) * scale);
            drawArrow(g2d, bobX, bobY, bobX + velX, bobY + velY, "v");
        }

        private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2, String label) {
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Draw main line
            g2d.drawLine(x1, y1, x2, y2);

            // Calculate arrow head
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int arrowSize = 10;
            int[] xPoints = new int[3];
            int[] yPoints = new int[3];
            xPoints[0] = x2;
            yPoints[0] = y2;
            xPoints[1] = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
            yPoints[1] = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
            xPoints[2] = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
            yPoints[2] = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));

            // Draw arrow head
            g2d.fillPolygon(xPoints, yPoints, 3);

            // Draw label
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(label, (x1 + x2) / 2 + 10, (y1 + y2) / 2 + 10);
        }

        private void drawAngleArc(Graphics2D g2d, int bobX, int bobY) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int radius = 40;
            double startAngle = -Math.PI / 2;
            double extent = angle;

            Arc2D.Double arc = new Arc2D.Double(
                    pivotX - radius, pivotY - radius,
                    2 * radius, 2 * radius,
                    Math.toDegrees(startAngle),
                    Math.toDegrees(extent),
                    Arc2D.OPEN);

            g2d.draw(arc);
            g2d.drawString(df.format(Math.toDegrees(angle)) + "°",
                    pivotX + radius + 5, pivotY);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isDragging) {
                time += 1.0 / 60.0; // Update time (60 FPS)

                // Calculate acceleration
                angleAcceleration = -(gravity / length) * Math.sin(angle);
                angleAcceleration -= damping * angleVelocity;

                // Update velocity and position
                angleVelocity += angleAcceleration;
                angle += angleVelocity;

                // Update maximum angle
                maxAngle = Math.max(maxAngle, Math.abs(angle));

                // Calculate period
                if (lastAngle < 0 && angle >= 0) {
                    if (oscillationCount > 0) {
                        period = 2 * time / oscillationCount;
                    }
                    oscillationCount++;
                }
                lastAngle = angle;

                // Update trail
                if (trailEnabled) {
                    int bobX = pivotX + (int) (length * Math.sin(angle));
                    int bobY = pivotY + (int) (length * Math.cos(angle));
                    trail.add(new Point(bobX, bobY));
                    if (trail.size() > maxTrailPoints) {
                        trail.remove(0);
                    }
                }
            }
            repaint();
        }

        // Additional getter methods for physics panel
        public double getTime() {
            return time;
        }

        public double getPeriod() {
            return period;
        }

        public double getMaxAngle() {
            return maxAngle;
        }

        public int getOscillationCount() {
            return oscillationCount;
        }

        public double getKineticEnergy() {
            return 0.5 * mass * length * length * angleVelocity * angleVelocity;
        }

        public double getPotentialEnergy() {
            return mass * gravity * length * (1 - Math.cos(angle));
        }

        public void setShowVectors(boolean show) {
            this.showVectors = show;
        }

        // Add missing methods
        public void setLength(double length) {
            this.length = length;
        }

        public void setGravity(double gravity) {
            this.gravity = gravity;
        }

        public void setDamping(double damping) {
            this.damping = damping;
        }

        public void setTrailEnabled(boolean enabled) {
            this.trailEnabled = enabled;
            if (!enabled)
                trail.clear();
        }

        public void reset() {
            angle = Math.PI / 4;
            angleVelocity = 0;
            time = 0;
            period = 0;
            oscillationCount = 0;
            maxAngle = angle;
            trail.clear();
        }
    }

    // Enhanced control panel
    private static class ControlPanel extends JPanel {
        public ControlPanel(PendulumPanel pendulum) {
            setPreferredSize(new Dimension(250, 400));
            setBackground(new Color(45, 45, 45));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            // Title
            JLabel title = new JLabel("Control Panel");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("Arial", Font.BOLD, 16));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(title);
            add(Box.createVerticalStrut(20));

            // Create sliders with custom styling
            addStyledSlider("Length (m)", 100, 300, 200,
                    value -> pendulum.setLength(value));
            addStyledSlider("Gravity (m/s²)", 0, 20, 10,
                    value -> pendulum.setGravity(value));
            addStyledSlider("Damping", 0, 100, 1,
                    value -> pendulum.setDamping(value / 1000.0));

            // Add checkboxes
            add(Box.createVerticalStrut(20));
            addStyledCheckbox("Show Trail", false,
                    pendulum::setTrailEnabled);
            addStyledCheckbox("Show Vectors", true,
                    pendulum::setShowVectors);

            // Add reset button
            add(Box.createVerticalStrut(20));
            JButton resetButton = createStyledButton("Reset");
            resetButton.addActionListener(e -> pendulum.reset());
            add(resetButton);
        }

        private void addStyledSlider(String label, int min, int max, int value,
                SliderCallback callback) {
            JLabel lblTitle = new JLabel(label);
            lblTitle.setForeground(Color.WHITE);
            add(lblTitle);

            JSlider slider = new JSlider(min, max, value);
            slider.setBackground(new Color(45, 45, 45));
            slider.setForeground(Color.WHITE);
            slider.addChangeListener(e -> callback.onValueChanged(slider.getValue()));
            add(slider);
            add(Box.createVerticalStrut(10));
        }

        private void addStyledCheckbox(String label, boolean initial,
                CheckboxCallback callback) {
            JCheckBox checkbox = new JCheckBox(label, initial);
            checkbox.setBackground(new Color(45, 45, 45));
            checkbox.setForeground(Color.WHITE);
            checkbox.addActionListener(e -> callback.onValueChanged(checkbox.isSelected()));
            add(checkbox);
        }

        private JButton createStyledButton(String text) {
            JButton button = new JButton(text);
            button.setBackground(new Color(60, 60, 60));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            return button;
        }

        @FunctionalInterface
        interface SliderCallback {
            void onValueChanged(int value);
        }

        @FunctionalInterface
        interface CheckboxCallback {
            void onValueChanged(boolean value);
        }
    }

    // New physics panel to display calculations
    private static class PhysicsPanel extends JPanel {
        private final PendulumPanel pendulum;
        private final DecimalFormat df = new DecimalFormat("#.##");
        private Timer updateTimer;

        public PhysicsPanel(PendulumPanel pendulum) {
            this.pendulum = pendulum;
            setPreferredSize(new Dimension(250, 200));
            setBackground(new Color(40, 40, 40));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            updateTimer = new Timer(100, e -> repaint());
            updateTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));

            int y = 20;
            int lineHeight = 20;

            drawValue(g2d, "Time:", df.format(pendulum.getTime()) + " s", y);
            y += lineHeight;

            drawValue(g2d, "Period:", df.format(pendulum.getPeriod()) + " s", y);
            y += lineHeight;

            drawValue(g2d, "Oscillations:",
                    String.valueOf(pendulum.getOscillationCount()), y);
            y += lineHeight;

            drawValue(g2d, "Max Angle:",
                    df.format(Math.toDegrees(pendulum.getMaxAngle())) + "°", y);
            y += lineHeight;

            drawValue(g2d, "Kinetic Energy:",
                    df.format(pendulum.getKineticEnergy()) + " J", y);
            y += lineHeight;

            drawValue(g2d, "Potential Energy:",
                    df.format(pendulum.getPotentialEnergy()) + " J", y);
            y += lineHeight;

            drawValue(g2d, "Total Energy:",
                    df.format(pendulum.getKineticEnergy() +
                            pendulum.getPotentialEnergy()) + " J",
                    y);
        }

        private void drawValue(Graphics2D g2d, String label, String value, int y) {
            g2d.drawString(label, 10, y);
            g2d.drawString(value, 120, y);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Advanced Pendulum Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new PendulumAdvanced());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}