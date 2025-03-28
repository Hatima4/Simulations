package Freefall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;

public class FreefallSimulation extends JFrame {
    private static final double GRAVITY = 9.81; // m/s²
    private static final double SCALE = 50; // pixels per meter
    private static final int WIDTH = 1000; // Increased width for info panel
    private static final int HEIGHT = 600;

    private SimulationPanel simulationPanel;
    private List<Force> forces;
    private double position;
    private double velocity;
    private double acceleration;
    private double mass = 1.0; // kg
    private boolean isSimulating;
    private Timer animationTimer;
    private JLabel infoLabel;
    private JSlider massSlider;
    private JPanel infoPanel;

    public FreefallSimulation() {
        // Initialize simulation variables
        position = 100;
        velocity = 0;
        acceleration = 0;
        forces = new ArrayList<>();
        forces.add(new Force("Gravity", mass * GRAVITY, 90)); // Gravity pointing downward

        // Create main frame
        setTitle("Freefall Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create simulation panel
        simulationPanel = new SimulationPanel();
        add(simulationPanel, BorderLayout.CENTER);

        // Create info panel
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Physics Info"));
        infoLabel = new JLabel("<html>Position: 0.0 m<br>Velocity: 0.0 m/s<br>Acceleration: 0.0 m/s²</html>");
        infoPanel.add(infoLabel);

        // Add mass control
        JPanel massPanel = new JPanel();
        massPanel.setLayout(new BoxLayout(massPanel, BoxLayout.Y_AXIS));
        massPanel.setBorder(BorderFactory.createTitledBorder("Mass Control"));
        massSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);
        massSlider.setMajorTickSpacing(20);
        massSlider.setMinorTickSpacing(5);
        massSlider.setPaintTicks(true);
        massSlider.setPaintLabels(true);
        massSlider.addChangeListener(e -> {
            mass = massSlider.getValue() / 10.0;
            updateForces();
        });
        JLabel massLabel = new JLabel("Mass: 1.0 kg");
        massPanel.add(massLabel);
        massPanel.add(massSlider);
        infoPanel.add(massPanel);

        // Create control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Forces"));

        JButton addAirResistanceBtn = new JButton("Add Air Resistance");
        JButton addBuoyancyBtn = new JButton("Add Buoyancy");
        JButton clearForcesBtn = new JButton("Clear Forces");
        JButton startStopBtn = new JButton("Start/Stop");
        JButton resetBtn = new JButton("Reset");

        // Add control handlers
        addAirResistanceBtn.addActionListener(e -> {
            forces.add(new Force("Air Resistance", 0.5 * velocity * velocity * 0.1, 270)); // Dynamic air resistance
            simulationPanel.repaint();
        });

        addBuoyancyBtn.addActionListener(e -> {
            forces.add(new Force("Buoyancy", mass * GRAVITY * 0.5, 90)); // Proportional to mass
            simulationPanel.repaint();
        });

        clearForcesBtn.addActionListener(e -> {
            updateForces();
        });

        startStopBtn.addActionListener(e -> {
            isSimulating = !isSimulating;
            if (isSimulating) {
                animationTimer.start();
            } else {
                animationTimer.stop();
            }
        });

        resetBtn.addActionListener(e -> {
            position = 100;
            velocity = 0;
            acceleration = 0;
            updateForces();
            simulationPanel.repaint();
        });

        controlPanel.add(new JLabel("Controls:"));
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(addAirResistanceBtn);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(addBuoyancyBtn);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(clearForcesBtn);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(startStopBtn);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(resetBtn);

        // Add info and control panels to EAST
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
        eastPanel.add(infoPanel);
        eastPanel.add(controlPanel);
        add(eastPanel, BorderLayout.EAST);

        // Create animation timer
        animationTimer = new Timer(16, e -> {
            if (!isSimulating)
                return;

            // Calculate net force
            double netForce = 0;
            for (Force force : forces) {
                netForce += force.getYComponent();
            }

            // Update acceleration, velocity and position
            acceleration = netForce / mass;
            velocity += acceleration * 0.016; // 16ms = 0.016s
            position += velocity * 0.016 * SCALE;

            // Update air resistance if present
            forces.stream()
                    .filter(f -> f.getName().equals("Air Resistance"))
                    .findFirst()
                    .ifPresent(f -> f.setMagnitude(0.5 * velocity * velocity * 0.1));

            // Bounce off ground and ceiling
            if (position > HEIGHT - 50) {
                position = HEIGHT - 50;
                velocity = -velocity * 0.8;
            }
            if (position < 50) {
                position = 50;
                velocity = -velocity * 0.8;
            }

            // Update info display
            updateInfoLabel();
            simulationPanel.repaint();
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void updateForces() {
        forces.clear();
        forces.add(new Force("Gravity", mass * GRAVITY, 90));
        simulationPanel.repaint();
    }

    private void updateInfoLabel() {
        String info = String.format("<html>Position: %.2f m<br>Velocity: %.2f m/s<br>Acceleration: %.2f m/s²</html>",
                (position - HEIGHT / 2) / SCALE,
                velocity,
                acceleration);
        infoLabel.setText(info);
    }

    private class SimulationPanel extends JPanel {
        public SimulationPanel() {
            setPreferredSize(new Dimension(WIDTH - 150, HEIGHT)); // Adjust width to account for control panel
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw ground
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, HEIGHT - 20, getWidth(), 20);

            // Draw ceiling
            g2d.fillRect(0, 0, getWidth(), 20);

            // Draw object
            g2d.setColor(Color.BLUE);
            g2d.fillOval(getWidth() / 2 - 25, (int) position - 25, 50, 50);

            // Draw free body diagram
            drawFreeBodyDiagram(g2d, getWidth() / 2, position);
        }

        private void drawFreeBodyDiagram(Graphics2D g2d, double centerX, double centerY) {
            for (Force force : forces) {
                double x = force.getXComponent() * 20; // Scale factor for visualization
                double y = force.getYComponent() * 20;

                // Draw force arrow
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine((int) centerX, (int) centerY,
                        (int) (centerX + x), (int) (centerY + y));

                // Draw arrow head
                double angle = Math.atan2(y, x);
                double arrowLength = 10;
                double arrowAngle = Math.PI / 6;

                int[] xPoints = new int[3];
                int[] yPoints = new int[3];
                xPoints[0] = (int) (centerX + x);
                yPoints[0] = (int) (centerY + y);
                xPoints[1] = (int) (centerX + x - arrowLength * Math.cos(angle - arrowAngle));
                yPoints[1] = (int) (centerY + y - arrowLength * Math.sin(angle - arrowAngle));
                xPoints[2] = (int) (centerX + x - arrowLength * Math.cos(angle + arrowAngle));
                yPoints[2] = (int) (centerY + y - arrowLength * Math.sin(angle + arrowAngle));

                g2d.fillPolygon(xPoints, yPoints, 3);

                // Draw force label
                g2d.drawString(force.getName(),
                        (int) (centerX + x / 2),
                        (int) (centerY + y / 2));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FreefallSimulation().setVisible(true);
        });
    }
}

class Force {
    private String name;
    private double magnitude;
    private double angle; // in degrees

    public Force(String name, double magnitude, double angle) {
        this.name = name;
        this.magnitude = magnitude;
        this.angle = angle;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public double getXComponent() {
        return magnitude * Math.cos(Math.toRadians(angle));
    }

    public double getYComponent() {
        return magnitude * Math.sin(Math.toRadians(angle));
    }

    public String getName() {
        return name;
    }
}