package ProjecticleMotion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class AngryBirdsSimulation extends JFrame {
    // Constants
    private static final int WINDOW_WIDTH = 1600; // Increased window size
    private static final int WINDOW_HEIGHT = 1000; // Increased window size
    private static final double GRAVITY = 0.5;
    private static final int BIRD_RADIUS = 25; // Slightly larger bird
    private static final int GROUND_HEIGHT = 150; // Taller ground
    private static final int TRAJECTORY_POINTS = 100; // More points for smoother trajectory
    private static final double BOUNCE_DAMPENING = 0.7;
    private static final double FRICTION = 0.99;

    // Slingshot constants
    private static final int SLINGSHOT_X = 150; // Moved slingshot right
    private static final int SLINGSHOT_Y = WINDOW_HEIGHT - GROUND_HEIGHT - 250; // Higher slingshot
    private static final int SLINGSHOT_WIDTH = 30; // Wider slingshot
    private static final int SLINGSHOT_HEIGHT = 250; // Taller slingshot
    private static final int RUBBER_BAND_LENGTH = 150; // Longer rubber band

    // Colors
    private static final Color SKY_BLUE = new Color(135, 206, 235);
    private static final Color GRASS_GREEN = new Color(34, 139, 34);
    private static final Color WOOD_BROWN = new Color(139, 69, 19);
    private static final Color BIRD_RED = new Color(220, 20, 60);

    // Add new constants for trajectory and angle display
    private static final int MAX_TRAJECTORY_POINTS = 100; // More points for smoother trajectory
    private static final Color TRAJECTORY_COLOR = new Color(0, 0, 255, 150);
    private static final Color TRAJECTORY_DOT_COLOR = new Color(0, 0, 255, 100);
    private static final Color ANGLE_COLOR = new Color(255, 165, 0, 200);
    private static final int TRAJECTORY_DOT_SIZE = 4;

    // Add new constants for control panel
    private static final int CONTROL_PANEL_WIDTH = 200;
    private static final int CONTROL_PANEL_HEIGHT = 150;
    private static final Color PANEL_BACKGROUND = new Color(240, 240, 240);
    private static final Font INFO_FONT = new Font("Arial", Font.PLAIN, 12);

    // Add physics constants
    private static final double AIR_RESISTANCE = 0.001; // Reduced air resistance coefficient
    private static final double AIR_DENSITY = 1.225; // kg/m³ (air density at sea level)
    private static boolean USE_AIR_RESISTANCE = false; // Toggle for air resistance
    private static final double BIRD_MASS = 1.0; // kg
    private static final double SCALE = 20.0; // pixels per meter

    // Add UI elements for physics
    private JLabel maxHeightLabel;
    private JLabel rangeLabel;
    private JCheckBox airResistanceToggle;
    private JLabel energyLabel;

    private GamePanel gamePanel;
    private JPanel controlPanel;
    private JLabel velocityLabel;
    private JLabel accelerationLabel;
    private JLabel timeLabel;
    private Bird bird;
    private boolean isDragging = false;
    private Point dragStart;
    private List<Point> trajectoryPoints;
    private boolean isInSlingshot = true;
    private long startTime;
    private boolean isSimulationRunning = false;

    public AngryBirdsSimulation() {
        setTitle("Angry Birds Physics Simulation");
        setSize(WINDOW_WIDTH + CONTROL_PANEL_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // Create main panel with game and control panels
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create game panel
        gamePanel = new GamePanel();
        mainPanel.add(gamePanel, BorderLayout.CENTER);

        // Create control panel
        controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.EAST);

        add(mainPanel);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    bird.reset();
                    isInSlingshot = true;
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
            }
        });
        setFocusable(true);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH, CONTROL_PANEL_HEIGHT * 2));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Reset button
        JButton resetButton = new JButton("Reset Simulation");
        resetButton.addActionListener(e -> resetSimulation());
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(resetButton);
        panel.add(Box.createVerticalStrut(20));

        // Air resistance toggle
        airResistanceToggle = new JCheckBox("Air Resistance", USE_AIR_RESISTANCE);
        airResistanceToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        airResistanceToggle.addActionListener(e -> toggleAirResistance());
        panel.add(airResistanceToggle);
        panel.add(Box.createVerticalStrut(10));

        // Physics information labels
        velocityLabel = new JLabel("Velocity: 0.0 m/s");
        accelerationLabel = new JLabel("Acceleration: 0.0 m/s²");
        timeLabel = new JLabel("Time: 0.0s");
        maxHeightLabel = new JLabel("Max Height: 0.0 m");
        rangeLabel = new JLabel("Range: 0.0 m");
        energyLabel = new JLabel("Energy: 0.0 J");

        velocityLabel.setFont(INFO_FONT);
        accelerationLabel.setFont(INFO_FONT);
        timeLabel.setFont(INFO_FONT);
        maxHeightLabel.setFont(INFO_FONT);
        rangeLabel.setFont(INFO_FONT);
        energyLabel.setFont(INFO_FONT);

        panel.add(velocityLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(accelerationLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(timeLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(maxHeightLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(rangeLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(energyLabel);

        return panel;
    }

    private void resetSimulation() {
        bird.reset();
        isInSlingshot = true;
        isSimulationRunning = false;
        startTime = 0;
        updatePhysicsInfo();
    }

    private void toggleAirResistance() {
        USE_AIR_RESISTANCE = airResistanceToggle.isSelected();
        // Update trajectory when air resistance is toggled
        if (!bird.isLaunched) {
            trajectoryPoints = bird.getTrajectory();
        }
    }

    private void updatePhysicsInfo() {
        if (bird.isLaunched) {
            double velocity = Math.sqrt(bird.velocityX * bird.velocityX + bird.velocityY * bird.velocityY);
            double maxHeightMeters = (WINDOW_HEIGHT - GROUND_HEIGHT - bird.maxHeight) / SCALE;
            double rangeMeters = bird.maxRange / SCALE;

            velocityLabel.setText(String.format("Velocity: %.2f m/s", velocity));
            accelerationLabel.setText(String.format("Acceleration: %.2f m/s²", GRAVITY));
            maxHeightLabel.setText(String.format("Max Height: %.2f m", maxHeightMeters));
            rangeLabel.setText(String.format("Range: %.2f m", rangeMeters));
            energyLabel.setText(String.format("Energy: %.2f J", bird.getTotalEnergy()));

            if (!isSimulationRunning) {
                startTime = System.currentTimeMillis();
                isSimulationRunning = true;
            }

            double timeInSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            timeLabel.setText(String.format("Time: %.1fs", timeInSeconds));
        } else {
            velocityLabel.setText("Velocity: 0.0 m/s");
            accelerationLabel.setText("Acceleration: 0.0 m/s²");
            timeLabel.setText("Time: 0.0s");
            maxHeightLabel.setText("Max Height: 0.0 m");
            rangeLabel.setText("Range: 0.0 m");
            energyLabel.setText("Energy: 0.0 J");
        }
    }

    private class Bird {
        private double x;
        private double y;
        private double velocityX;
        private double velocityY;
        private boolean isLaunched;
        private boolean isOnGround;
        private List<Point> flightPath;
        private double maxHeight;
        private double initialHeight;
        private double maxRange;

        public Bird() {
            flightPath = new ArrayList<>();
            reset();
        }

        public void reset() {
            x = SLINGSHOT_X;
            y = SLINGSHOT_Y;
            velocityX = 0;
            velocityY = 0;
            isLaunched = false;
            isOnGround = false;
            flightPath.clear();
            maxHeight = 0;
            initialHeight = SLINGSHOT_Y;
            maxRange = 0;
        }

        private void applyAirResistance() {
            if (USE_AIR_RESISTANCE) {
                double velocity = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
                if (velocity > 0) { // Prevent division by zero
                    // Calculate drag force using more realistic formula
                    double dragForce = 0.5 * AIR_DENSITY * velocity * velocity * AIR_RESISTANCE;
                    double ax = -(dragForce * velocityX) / (velocity * BIRD_MASS);
                    double ay = -(dragForce * velocityY) / (velocity * BIRD_MASS);
                    velocityX += ax;
                    velocityY += ay;
                }
            }
        }

        public void draw(Graphics2D g) {
            // Draw slingshot
            g.setColor(WOOD_BROWN);
            // Draw slingshot base
            g.fillRect(SLINGSHOT_X - SLINGSHOT_WIDTH / 2, SLINGSHOT_Y,
                    SLINGSHOT_WIDTH, SLINGSHOT_HEIGHT);
            // Draw slingshot top
            g.fillRect(SLINGSHOT_X - SLINGSHOT_WIDTH / 2, SLINGSHOT_Y,
                    SLINGSHOT_WIDTH, 20);

            // Draw rubber band
            if (isInSlingshot) {
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(3));
                g.drawLine(SLINGSHOT_X, SLINGSHOT_Y, (int) x, (int) y);
                g.drawLine(SLINGSHOT_X, SLINGSHOT_Y + SLINGSHOT_HEIGHT, (int) x, (int) y);
            }

            // Draw bird
            g.setColor(BIRD_RED);
            // Draw bird body
            g.fillOval((int) (x - BIRD_RADIUS), (int) (y - BIRD_RADIUS),
                    BIRD_RADIUS * 2, BIRD_RADIUS * 2);
            // Draw bird eye
            g.setColor(Color.WHITE);
            g.fillOval((int) (x + BIRD_RADIUS / 2), (int) (y - BIRD_RADIUS / 2),
                    BIRD_RADIUS / 2, BIRD_RADIUS / 2);
            // Draw bird pupil
            g.setColor(Color.BLACK);
            g.fillOval((int) (x + BIRD_RADIUS / 2 + 2), (int) (y - BIRD_RADIUS / 2 + 2),
                    BIRD_RADIUS / 4, BIRD_RADIUS / 4);
        }

        public void update() {
            if (isLaunched) {
                // Apply air resistance before gravity
                applyAirResistance();

                // Update velocity and position
                velocityY += GRAVITY;
                x += velocityX;
                y += velocityY;

                // Update max height and range
                maxHeight = Math.min(y, maxHeight);
                maxRange = Math.max(x - SLINGSHOT_X, maxRange);

                // Record flight path
                if (flightPath.size() < MAX_TRAJECTORY_POINTS) {
                    flightPath.add(new Point((int) x, (int) y));
                }

                // Ground collision
                if (y >= WINDOW_HEIGHT - GROUND_HEIGHT - BIRD_RADIUS) {
                    y = WINDOW_HEIGHT - GROUND_HEIGHT - BIRD_RADIUS;
                    velocityY = -velocityY * BOUNCE_DAMPENING;
                    velocityX *= FRICTION;
                    isOnGround = true;

                    if (Math.abs(velocityX) < 0.1 && Math.abs(velocityY) < 0.1) {
                        velocityX = 0;
                        velocityY = 0;
                    }
                } else {
                    isOnGround = false;
                }
            }
        }

        public List<Point> getTrajectory() {
            if (!isLaunched) {
                List<Point> points = new ArrayList<>();

                // Calculate initial velocity based on current position
                double dx = x - SLINGSHOT_X;
                double dy = y - SLINGSHOT_Y;
                double initialVelocityX = -dx * 0.1;
                double initialVelocityY = -dy * 0.1;

                // Start from the current bird position
                double currentX = x;
                double currentY = y;
                double currentVX = initialVelocityX;
                double currentVY = initialVelocityY;

                // Add initial position
                points.add(new Point((int) currentX, (int) currentY));

                // Calculate trajectory points until first bounce
                double t = 0;
                double step = 0.1;
                boolean hasBounced = false;

                while (t < 10.0 && !hasBounced) {
                    // Apply air resistance if enabled
                    if (USE_AIR_RESISTANCE) {
                        double velocity = Math.sqrt(currentVX * currentVX + currentVY * currentVY);
                        if (velocity > 0) {
                            double dragForce = 0.5 * AIR_DENSITY * velocity * velocity * AIR_RESISTANCE;
                            double ax = -(dragForce * currentVX) / (velocity * BIRD_MASS);
                            double ay = -(dragForce * currentVY) / (velocity * BIRD_MASS);
                            currentVX += ax;
                            currentVY += ay;
                        }
                    }

                    // Update position using the same physics as the actual flight
                    currentVY += GRAVITY;
                    currentX += currentVX;
                    currentY += currentVY;

                    // Check for ground collision
                    if (currentY >= WINDOW_HEIGHT - GROUND_HEIGHT - BIRD_RADIUS) {
                        currentY = WINDOW_HEIGHT - GROUND_HEIGHT - BIRD_RADIUS;
                        hasBounced = true;
                    }

                    points.add(new Point((int) currentX, (int) currentY));
                    t += step;
                }
                return points;
            }
            return new ArrayList<>();
        }

        public boolean contains(Point p) {
            double dx = p.x - x;
            double dy = p.y - y;
            return Math.sqrt(dx * dx + dy * dy) <= BIRD_RADIUS;
        }

        public void setPosition(int x, int y) {
            if (isInSlingshot) {
                // Limit dragging to rubber band length
                double dx = x - SLINGSHOT_X;
                double dy = y - SLINGSHOT_Y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance > RUBBER_BAND_LENGTH) {
                    double angle = Math.atan2(dy, dx);
                    this.x = SLINGSHOT_X + (int) (Math.cos(angle) * RUBBER_BAND_LENGTH);
                    this.y = SLINGSHOT_Y + (int) (Math.sin(angle) * RUBBER_BAND_LENGTH);
                } else {
                    this.x = x;
                    this.y = y;
                }
            } else {
                this.x = x;
                this.y = y;
            }
        }

        public void launch(Point start) {
            if (isInSlingshot) {
                // Use the same velocity calculation as in getTrajectory
                double dx = x - SLINGSHOT_X;
                double dy = y - SLINGSHOT_Y;
                velocityX = -dx * 0.1;
                velocityY = -dy * 0.1;
                isLaunched = true;
                isInSlingshot = false;
                startTime = System.currentTimeMillis();
                isSimulationRunning = true;
                flightPath.clear();
                flightPath.add(new Point((int) x, (int) y)); // Add initial position
            }
        }

        public double getLaunchAngle() {
            if (!isLaunched) {
                double dx = x - SLINGSHOT_X;
                double dy = y - SLINGSHOT_Y;
                return Math.toDegrees(Math.atan2(-dy, -dx));
            }
            return 0;
        }

        public List<Point> getFlightPath() {
            return flightPath;
        }

        public double getKineticEnergy() {
            return 0.5 * BIRD_MASS * (velocityX * velocityX + velocityY * velocityY);
        }

        public double getPotentialEnergy() {
            return BIRD_MASS * GRAVITY * (WINDOW_HEIGHT - GROUND_HEIGHT - y) / SCALE;
        }

        public double getTotalEnergy() {
            return getKineticEnergy() + getPotentialEnergy();
        }
    }

    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(SKY_BLUE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (bird.contains(e.getPoint())) {
                        isDragging = true;
                        dragStart = e.getPoint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDragging) {
                        bird.launch(dragStart);
                        isDragging = false;
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging) {
                        bird.setPosition(e.getX(), e.getY());
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw ground
            g2d.setColor(GRASS_GREEN);
            g2d.fillRect(0, WINDOW_HEIGHT - GROUND_HEIGHT,
                    WINDOW_WIDTH, GROUND_HEIGHT);

            // Draw flight path
            if (bird.isLaunched && bird.getFlightPath().size() > 1) {
                g2d.setColor(TRAJECTORY_COLOR);
                g2d.setStroke(new BasicStroke(2));
                List<Point> flightPath = bird.getFlightPath();
                for (int i = 0; i < flightPath.size() - 1; i++) {
                    Point p1 = flightPath.get(i);
                    Point p2 = flightPath.get(i + 1);
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }

                // Draw dots on flight path
                g2d.setColor(TRAJECTORY_DOT_COLOR);
                for (Point p : flightPath) {
                    g2d.fillOval(p.x - TRAJECTORY_DOT_SIZE / 2, p.y - TRAJECTORY_DOT_SIZE / 2,
                            TRAJECTORY_DOT_SIZE, TRAJECTORY_DOT_SIZE);
                }
            }

            // Draw trajectory prediction
            if (!bird.isLaunched && trajectoryPoints.size() > 1) {
                // Draw trajectory line
                g2d.setColor(TRAJECTORY_COLOR);
                g2d.setStroke(new BasicStroke(2));
                for (int i = 0; i < trajectoryPoints.size() - 1; i++) {
                    Point p1 = trajectoryPoints.get(i);
                    Point p2 = trajectoryPoints.get(i + 1);
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }

                // Draw dots on trajectory
                g2d.setColor(TRAJECTORY_DOT_COLOR);
                for (Point p : trajectoryPoints) {
                    g2d.fillOval(p.x - TRAJECTORY_DOT_SIZE / 2, p.y - TRAJECTORY_DOT_SIZE / 2,
                            TRAJECTORY_DOT_SIZE, TRAJECTORY_DOT_SIZE);
                }

                // Draw angle arc
                double angle = bird.getLaunchAngle();
                g2d.setColor(ANGLE_COLOR);
                g2d.setStroke(new BasicStroke(2));
                int arcRadius = 40;
                g2d.drawArc(SLINGSHOT_X - arcRadius, SLINGSHOT_Y - arcRadius,
                        arcRadius * 2, arcRadius * 2, 0, (int) angle);

                // Draw angle text
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String angleText = String.format("%.1f°", angle);
                g2d.drawString(angleText, SLINGSHOT_X + 10, SLINGSHOT_Y - 10);

                // Draw velocity vector
                if (trajectoryPoints.size() > 1) {
                    Point firstPoint = trajectoryPoints.get(0);
                    Point secondPoint = trajectoryPoints.get(1);
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawLine(firstPoint.x, firstPoint.y,
                            firstPoint.x + (secondPoint.x - firstPoint.x) * 2,
                            firstPoint.y + (secondPoint.y - firstPoint.y) * 2);
                }
            }

            // Draw bird and slingshot
            bird.draw(g2d);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AngryBirdsSimulation game = new AngryBirdsSimulation();
            game.bird = game.new Bird();
            game.trajectoryPoints = new ArrayList<>();
            game.setVisible(true);

            // Game loop
            Timer timer = new Timer(16, e -> {
                game.bird.update();
                if (!game.bird.isLaunched) {
                    game.trajectoryPoints = game.bird.getTrajectory();
                }
                game.updatePhysicsInfo();
                game.gamePanel.repaint();
            });
            timer.start();
        });
    }
}