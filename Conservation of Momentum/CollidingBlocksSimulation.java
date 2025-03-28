import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CollidingBlocksSimulation extends JFrame {
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final int FLOOR_Y = WINDOW_HEIGHT - 100;
    private static final double SCALE = 100.0; // pixels per meter
    private static final double TIME_STEP = 0.01; // seconds

    private static class Block {
        double x, y;
        double width, height;
        double mass;
        double velocity;
        Color color;
        boolean isMoving;
        double initialVelocity;
        double initialMomentum;
        double currentMomentum;

        Block(double x, double y, double width, double height, double mass, double velocity, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.mass = mass;
            this.velocity = velocity;
            this.color = color;
            this.isMoving = true;
            this.initialVelocity = velocity;
            this.initialMomentum = mass * velocity;
            this.currentMomentum = initialMomentum;
        }

        void update() {
            if (isMoving) {
                x += velocity * TIME_STEP;
                currentMomentum = mass * velocity;
            }
        }

        void draw(Graphics2D g) {
            // Draw block shadow
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRect((int) (x * SCALE) + 5, (int) (y * SCALE) + 5,
                    (int) (width * SCALE), (int) (height * SCALE));

            // Draw block
            g.setColor(color);
            g.fillRect((int) (x * SCALE), (int) (y * SCALE),
                    (int) (width * SCALE), (int) (height * SCALE));

            // Draw block border
            g.setColor(color.brighter());
            g.drawRect((int) (x * SCALE), (int) (y * SCALE),
                    (int) (width * SCALE), (int) (height * SCALE));

            // Draw velocity vector
            if (isMoving) {
                g.setColor(Color.WHITE);
                int arrowLength = (int) (Math.abs(velocity) * 5);
                int startX = (int) ((x + width / 2) * SCALE);
                int startY = (int) ((y + height / 2) * SCALE);
                int endX = startX + (int) (Math.signum(velocity) * arrowLength);
                g.drawLine(startX, startY, endX, startY);

                // Draw arrow head
                int[] xPoints = { endX, endX - 10, endX - 10 };
                int[] yPoints = { startY, startY - 5, startY + 5 };
                g.fillPolygon(xPoints, yPoints, 3);
            }
        }
    }

    private static class SimulationPanel extends JPanel {
        private List<Block> blocks;
        private boolean isRunning;
        private int collisionCount;
        private double totalEnergy;
        private double totalMomentum;
        private JLabel collisionLabel;
        private JLabel energyLabel;
        private JLabel momentumLabel;
        private JLabel massRatioLabel;
        private double massRatio;
        private JSlider massRatioSlider;
        private JSlider velocitySlider1;
        private JSlider velocitySlider2;
        private JSlider elasticitySlider;
        private double initialVelocity1;
        private double initialVelocity2;
        private double elasticity;
        private JPanel infoPanel;
        private JPanel controlPanel;
        private Color backgroundColor = new Color(30, 30, 30);
        private Color panelColor = new Color(45, 45, 45);
        private JLabel block1MomentumLabel;
        private JLabel block2MomentumLabel;
        private JLabel block1InitialMomentumLabel;
        private JLabel block2InitialMomentumLabel;

        public SimulationPanel() {
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            setBackground(backgroundColor);
            setFocusable(true);

            // Create main layout
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Create control panel
            controlPanel = new JPanel();
            controlPanel.setBackground(panelColor);
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Create simulation controls
            JPanel simulationControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
            simulationControls.setBackground(panelColor);
            JButton resetButton = createStyledButton("Reset");
            JButton pauseButton = createStyledButton("Pause");
            simulationControls.add(resetButton);
            simulationControls.add(pauseButton);
            controlPanel.add(simulationControls);

            // Create sliders panel
            JPanel slidersPanel = new JPanel(new GridLayout(4, 2, 10, 5));
            slidersPanel.setBackground(panelColor);

            // Mass ratio slider
            massRatioSlider = createStyledSlider(0, 200, 100, "Mass Ratio");
            slidersPanel.add(new JLabel("Mass Ratio:"));
            slidersPanel.add(massRatioSlider);

            // Velocity sliders
            velocitySlider1 = createStyledSlider(-100, 100, 100, "Block 1 Velocity");
            slidersPanel.add(new JLabel("Block 1 Velocity:"));
            slidersPanel.add(velocitySlider1);

            velocitySlider2 = createStyledSlider(-100, 100, 0, "Block 2 Velocity");
            slidersPanel.add(new JLabel("Block 2 Velocity:"));
            slidersPanel.add(velocitySlider2);

            // Elasticity slider
            elasticitySlider = createStyledSlider(0, 100, 100, "Elasticity");
            slidersPanel.add(new JLabel("Elasticity:"));
            slidersPanel.add(elasticitySlider);

            controlPanel.add(slidersPanel);

            // Create momentum info panel
            JPanel momentumPanel = new JPanel(new GridLayout(2, 2, 10, 5));
            momentumPanel.setBackground(panelColor);

            block1InitialMomentumLabel = createStyledLabel("Block 1 Initial Momentum: 0.00 kg⋅m/s");
            block2InitialMomentumLabel = createStyledLabel("Block 2 Initial Momentum: 0.00 kg⋅m/s");
            block1MomentumLabel = createStyledLabel("Block 1 Current Momentum: 0.00 kg⋅m/s");
            block2MomentumLabel = createStyledLabel("Block 2 Current Momentum: 0.00 kg⋅m/s");

            momentumPanel.add(block1InitialMomentumLabel);
            momentumPanel.add(block2InitialMomentumLabel);
            momentumPanel.add(block1MomentumLabel);
            momentumPanel.add(block2MomentumLabel);

            controlPanel.add(momentumPanel);

            // Create info panel
            infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            infoPanel.setBackground(panelColor);
            collisionLabel = createStyledLabel("Collisions: 0");
            energyLabel = createStyledLabel("Energy: 0.00 J");
            momentumLabel = createStyledLabel("Total Momentum: 0.00 kg⋅m/s");
            infoPanel.add(collisionLabel);
            infoPanel.add(energyLabel);
            infoPanel.add(momentumLabel);
            controlPanel.add(infoPanel);

            // Add control panel to main panel
            add(controlPanel, BorderLayout.NORTH);

            // Initialize simulation
            blocks = new ArrayList<>();
            isRunning = true;
            collisionCount = 0;
            massRatio = 1.0;
            initialVelocity1 = 2.0;
            initialVelocity2 = 0.0;
            elasticity = 1.0;
            initializeSimulation();

            // Add button and slider listeners
            resetButton.addActionListener(e -> {
                collisionCount = 0;
                massRatio = massRatioSlider.getValue() / 100.0;
                initialVelocity1 = velocitySlider1.getValue() / 50.0;
                initialVelocity2 = velocitySlider2.getValue() / 50.0;
                elasticity = elasticitySlider.getValue() / 100.0;
                initializeSimulation();
                updateLabels();
            });

            pauseButton.addActionListener(e -> {
                isRunning = !isRunning;
                pauseButton.setText(isRunning ? "Pause" : "Resume");
            });

            massRatioSlider.addChangeListener(e -> {
                massRatio = massRatioSlider.getValue() / 100.0;
                updateLabels();
            });

            velocitySlider1.addChangeListener(e -> {
                initialVelocity1 = velocitySlider1.getValue() / 50.0;
                updateLabels();
            });

            velocitySlider2.addChangeListener(e -> {
                initialVelocity2 = velocitySlider2.getValue() / 50.0;
                updateLabels();
            });

            elasticitySlider.addChangeListener(e -> {
                elasticity = elasticitySlider.getValue() / 100.0;
                updateLabels();
            });

            // Start animation
            Timer timer = new Timer(16, e -> {
                if (isRunning) {
                    updateSimulation();
                    repaint();
                }
            });
            timer.start();
        }

        private JButton createStyledButton(String text) {
            JButton button = new JButton(text);
            button.setBackground(new Color(60, 60, 60));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createRaisedBevelBorder());
            return button;
        }

        private JSlider createStyledSlider(int min, int max, int value, String tooltip) {
            JSlider slider = new JSlider(min, max, value);
            slider.setBackground(panelColor);
            slider.setForeground(Color.WHITE);
            slider.setMajorTickSpacing(50);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setToolTipText(tooltip);
            return slider;
        }

        private JLabel createStyledLabel(String text) {
            JLabel label = new JLabel(text);
            label.setForeground(Color.WHITE);
            return label;
        }

        private void initializeSimulation() {
            blocks.clear();

            // Create blocks with different masses
            double smallMass = 1.0; // kg
            double largeMass = smallMass * massRatio; // kg

            // Create small block (moving)
            blocks.add(new Block(2.0, FLOOR_Y / SCALE, 0.5, 0.5,
                    smallMass, initialVelocity1, new Color(65, 105, 225)));

            // Create large block (stationary)
            blocks.add(new Block(5.0, FLOOR_Y / SCALE, 1.0, 1.0,
                    largeMass, initialVelocity2, new Color(220, 20, 60)));
        }

        private void updateSimulation() {
            // Update positions
            for (Block block : blocks) {
                block.update();
            }

            // Check collisions
            for (int i = 0; i < blocks.size(); i++) {
                Block block1 = blocks.get(i);

                // Check wall collisions
                if (block1.x <= 0) {
                    block1.x = 0;
                    block1.velocity = -block1.velocity * elasticity;
                    collisionCount++;
                }

                // Check block collisions
                for (int j = i + 1; j < blocks.size(); j++) {
                    Block block2 = blocks.get(j);

                    if (block1.x + block1.width > block2.x &&
                            block1.x < block2.x + block2.width) {

                        // Collision with elasticity
                        double v1 = block1.velocity;
                        double v2 = block2.velocity;
                        double m1 = block1.mass;
                        double m2 = block2.mass;

                        // Calculate velocities after collision
                        double v1f = ((m1 - m2) * v1 + 2 * m2 * v2) / (m1 + m2);
                        double v2f = ((m2 - m1) * v2 + 2 * m1 * v1) / (m1 + m2);

                        // Apply elasticity
                        block1.velocity = v1 + (v1f - v1) * elasticity;
                        block2.velocity = v2 + (v2f - v2) * elasticity;

                        // Prevent sticking
                        block1.x = block2.x - block1.width;

                        collisionCount++;
                    }
                }
            }

            // Calculate total energy and momentum
            totalEnergy = 0;
            totalMomentum = 0;
            for (Block block : blocks) {
                totalEnergy += 0.5 * block.mass * block.velocity * block.velocity;
                totalMomentum += block.mass * block.velocity;
            }

            updateLabels();
        }

        private void updateLabels() {
            collisionLabel.setText(String.format("Collisions: %d", collisionCount));
            energyLabel.setText(String.format("Energy: %.2f J", totalEnergy));
            momentumLabel.setText(String.format("Total Momentum: %.2f kg⋅m/s", totalMomentum));

            if (blocks.size() >= 2) {
                Block block1 = blocks.get(0);
                Block block2 = blocks.get(1);

                block1InitialMomentumLabel
                        .setText(String.format("Block 1 Initial Momentum: %.2f kg⋅m/s", block1.initialMomentum));
                block2InitialMomentumLabel
                        .setText(String.format("Block 2 Initial Momentum: %.2f kg⋅m/s", block2.initialMomentum));
                block1MomentumLabel
                        .setText(String.format("Block 1 Current Momentum: %.2f kg⋅m/s", block1.currentMomentum));
                block2MomentumLabel
                        .setText(String.format("Block 2 Current Momentum: %.2f kg⋅m/s", block2.currentMomentum));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw floor
            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect(0, FLOOR_Y, WINDOW_WIDTH, WINDOW_HEIGHT - FLOOR_Y);

            // Draw floor shadow
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillRect(0, FLOOR_Y + 2, WINDOW_WIDTH, WINDOW_HEIGHT - FLOOR_Y - 2);

            // Draw blocks
            for (Block block : blocks) {
                block.draw(g2d);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CollidingBlocksSimulation simulation = new CollidingBlocksSimulation();
            simulation.setTitle("Conservation of Momentum Simulation");
            simulation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            simulation.add(new SimulationPanel());
            simulation.pack();
            simulation.setLocationRelativeTo(null);
            simulation.setVisible(true);
        });
    }
}