import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

public class ParticleGravitySimulation extends JPanel {
    private ArrayList<Particle> particles;
    private ArrayList<Field> fields;
    private Point mousePos;
    private boolean isAttracting;
    private boolean isRepelling;
    private boolean isFieldPlacement = false;
    private boolean isOrbitalMode = false;
    private boolean showVelocityVectors = true;
    private boolean showForceVectors = true;
    private final double G = 1000; // Gravity constant (adjusted for screen space)
    private final double dampening = 0.995; // Velocity dampening
    private final int PARTICLE_RADIUS = 5;
    private final double MAX_FORCE = 50.0; // Maximum force limit
    private final double SOFT_RADIUS = 20.0; // Soft radius for force calculation
    private double timeScale = 1.0;
    private Color particleColor = new Color(0, 255, 255); // Cyan color for particles
    private Color orbitTrailColor = new Color(255, 255, 255, 30);
    private int maxTrailPoints = 50;

    public ParticleGravitySimulation() {
        particles = new ArrayList<>();
        fields = new ArrayList<>();
        setPreferredSize(new Dimension(1200, 800));
        setBackground(new Color(10, 10, 20)); // Darker blue background
        setupInteraction();
        setupUI();
        startSimulation();
    }

    private void setupUI() {
        // Create control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(20, 20, 30));
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        // Time scale slider
        JSlider timeSlider = new JSlider(0, 200, 100);
        timeSlider.setBackground(new Color(20, 20, 30));
        timeSlider.setForeground(Color.WHITE);
        timeSlider.addChangeListener(e -> timeScale = timeSlider.getValue() / 100.0);

        JLabel timeLabel = new JLabel("Time Scale: 1.0x");
        timeLabel.setForeground(Color.WHITE);
        timeSlider.addChangeListener(
                e -> timeLabel.setText(String.format("Time Scale: %.1fx", timeSlider.getValue() / 100.0)));

        // Checkboxes
        JCheckBox orbitalModeBox = new JCheckBox("Orbital Mode", isOrbitalMode);
        JCheckBox fieldPlacementBox = new JCheckBox("Field Placement Mode", isFieldPlacement);

        // Style checkboxes
        orbitalModeBox.setForeground(Color.WHITE);
        orbitalModeBox.setBackground(new Color(20, 20, 30));
        fieldPlacementBox.setForeground(Color.WHITE);
        fieldPlacementBox.setBackground(new Color(20, 20, 30));

        // Add mode change listeners
        orbitalModeBox.addActionListener(e -> updateModes(true, orbitalModeBox.isSelected(), fieldPlacementBox));
        fieldPlacementBox.addActionListener(e -> updateModes(false, fieldPlacementBox.isSelected(), orbitalModeBox));

        JCheckBox velocityVectorsBox = new JCheckBox("Show Velocity Vectors", showVelocityVectors);
        velocityVectorsBox.setForeground(Color.WHITE);
        velocityVectorsBox.setBackground(new Color(20, 20, 30));
        velocityVectorsBox.addActionListener(e -> showVelocityVectors = velocityVectorsBox.isSelected());

        JCheckBox forceVectorsBox = new JCheckBox("Show Force Vectors", showForceVectors);
        forceVectorsBox.setForeground(Color.WHITE);
        forceVectorsBox.setBackground(new Color(20, 20, 30));
        forceVectorsBox.addActionListener(e -> showForceVectors = forceVectorsBox.isSelected());

        // Add clear fields button
        JButton clearFieldsButton = new JButton("Clear Fields");
        clearFieldsButton.setBackground(new Color(50, 50, 60));
        clearFieldsButton.setForeground(Color.WHITE);
        clearFieldsButton.setFocusPainted(false);
        clearFieldsButton.addActionListener(e -> fields.clear());
        clearFieldsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to control panel
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(timeLabel);
        controlPanel.add(timeSlider);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(orbitalModeBox);
        controlPanel.add(fieldPlacementBox);
        controlPanel.add(velocityVectorsBox);
        controlPanel.add(forceVectorsBox);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(clearFieldsButton);
        controlPanel.add(Box.createVerticalStrut(10));

        // Add control panel to main panel
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.EAST);
    }

    private void updateModes(boolean isOrbitalModeChange, boolean newValue, JCheckBox otherBox) {
        if (isOrbitalModeChange) {
            isOrbitalMode = newValue;
            if (newValue) {
                isFieldPlacement = false;
                otherBox.setSelected(false);
            }
        } else {
            isFieldPlacement = newValue;
            if (newValue) {
                isOrbitalMode = false;
                otherBox.setSelected(false);
            }
        }
    }

    private void setupInteraction() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Point dragStart;

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (isFieldPlacement) {
                        fields.add(new Field(e.getX(), e.getY(), true));
                    } else if (isOrbitalMode) {
                        dragStart = e.getPoint();
                    } else {
                        isAttracting = true;
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (isFieldPlacement) {
                        fields.add(new Field(e.getX(), e.getY(), false));
                    } else {
                        isRepelling = true;
                    }
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    addParticle(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (isOrbitalMode && dragStart != null) {
                        // Calculate velocity based on drag vector
                        double dx = e.getX() - dragStart.x;
                        double dy = e.getY() - dragStart.y;
                        double speed = Math.sqrt(dx * dx + dy * dy) * 0.1;
                        addOrbitalParticle(dragStart.x, dragStart.y, dx * 0.1, dy * 0.1);
                        dragStart = null;
                    }
                    isAttracting = false;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    isRepelling = false;
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mousePos = e.getPoint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        for (int i = 0; i < 10; i++)
                            addRandomParticle();
                        break;
                    case KeyEvent.VK_C:
                        particles.clear();
                        break;
                    case KeyEvent.VK_O:
                        isOrbitalMode = !isOrbitalMode;
                        break;
                }
            }
        };

        setFocusable(true);
        addKeyListener(keyAdapter);
    }

    private void addOrbitalParticle(double x, double y, double vx, double vy) {
        Particle p = new Particle(x, y, vx, vy);
        p.trail = new ArrayList<>();
        particles.add(p);
    }

    private void addParticle(double x, double y) {
        Particle p = new Particle(x, y,
                (Math.random() - 0.5) * 2,
                (Math.random() - 0.5) * 2);
        p.trail = new ArrayList<>();
        particles.add(p);
    }

    private void addRandomParticle() {
        double x = Math.random() * getWidth();
        double y = Math.random() * getHeight();
        addParticle(x, y);
    }

    private void startSimulation() {
        javax.swing.Timer timer = new javax.swing.Timer(16, e -> {
            updateParticles();
            repaint();
        });
        timer.start();
    }

    private void updateParticles() {
        for (Particle p : particles) {
            // Store previous position for trail
            p.trail.add(new Point2D.Double(p.x, p.y));
            if (p.trail.size() > maxTrailPoints) {
                p.trail.remove(0);
            }

            double totalFx = 0, totalFy = 0;

            // Mouse interaction
            if ((isAttracting || isRepelling) && mousePos != null) {
                addFieldForce(p, mousePos.x, mousePos.y, isAttracting, totalFx, totalFy);
            }

            // Permanent fields interaction
            for (Field field : fields) {
                double[] forces = addFieldForce(p, field.x, field.y, field.isAttracting, totalFx, totalFy);
                totalFx = forces[0];
                totalFy = forces[1];
            }

            // Particle interactions
            for (Particle other : particles) {
                if (other != p) {
                    double dx = other.x - p.x;
                    double dy = other.y - p.y;
                    double distSq = dx * dx + dy * dy + SOFT_RADIUS * SOFT_RADIUS;
                    double dist = Math.sqrt(distSq);

                    if (dist > PARTICLE_RADIUS * 2) {
                        double force = (G * 0.1) / distSq;

                        // Limit the maximum force
                        force = Math.min(Math.abs(force), MAX_FORCE * 0.1) * Math.signum(force);

                        totalFx += (dx / dist) * force;
                        totalFy += (dy / dist) * force;
                    }
                }
            }

            // Apply velocity-dependent dampening
            double speed = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
            double dampFactor = Math.pow(dampening, 1.0 + speed * 0.01);

            // Update velocity and position with time scaling
            p.vx += totalFx * timeScale;
            p.vy += totalFy * timeScale;

            // Apply dampening
            p.vx *= dampFactor;
            p.vy *= dampFactor;

            // Additional speed limit
            speed = Math.sqrt(p.vx * p.vx + p.vy * p.vy);
            if (speed > MAX_FORCE) {
                double scale = MAX_FORCE / speed;
                p.vx *= scale;
                p.vy *= scale;
            }

            p.x += p.vx * timeScale;
            p.y += p.vy * timeScale;

            // Bounce off walls with energy loss
            if (p.x < 0) {
                p.x = 0;
                p.vx *= -0.8;
            }
            if (p.x > getWidth()) {
                p.x = getWidth();
                p.vx *= -0.8;
            }
            if (p.y < 0) {
                p.y = 0;
                p.vy *= -0.8;
            }
            if (p.y > getHeight()) {
                p.y = getHeight();
                p.vy *= -0.8;
            }

            // Store forces for visualization
            p.lastFx = totalFx;
            p.lastFy = totalFy;
        }
    }

    private double[] addFieldForce(Particle p, double fieldX, double fieldY, boolean isAttracting, double totalFx,
            double totalFy) {
        double dx = fieldX - p.x;
        double dy = fieldY - p.y;
        double distSq = dx * dx + dy * dy + SOFT_RADIUS * SOFT_RADIUS;
        double dist = Math.sqrt(distSq);

        if (dist > 1) {
            double force = G / distSq;
            if (!isAttracting)
                force = -force;

            // Limit the maximum force
            force = Math.min(Math.abs(force), MAX_FORCE) * Math.signum(force);

            // Apply velocity-dependent dampening when close to field
            double proximityFactor = Math.min(1.0, dist / 100.0);
            force *= proximityFactor;

            totalFx += (dx / dist) * force;
            totalFy += (dy / dist) * force;
        }
        return new double[] { totalFx, totalFy };
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw grid
        drawGrid(g2d);

        // Draw particles and their effects
        for (Particle p : particles) {
            // Draw trail
            if (p.trail.size() > 1) {
                g2d.setColor(orbitTrailColor);
                Point2D prev = null;
                for (Point2D point : p.trail) {
                    if (prev != null) {
                        g2d.draw(new Line2D.Double(prev, point));
                    }
                    prev = point;
                }
            }

            // Draw force vector
            if (showForceVectors) {
                drawVector(g2d, p.x, p.y, p.lastFx * 20, p.lastFy * 20, Color.RED);
            }

            // Draw velocity vector
            if (showVelocityVectors) {
                drawVector(g2d, p.x, p.y, p.vx * 10, p.vy * 10, Color.GREEN);
            }

            // Draw particle glow
            AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
            g2d.setComposite(composite);
            g2d.setColor(new Color(0, 150, 255, 50));
            g2d.fill(new Ellipse2D.Double(
                    p.x - PARTICLE_RADIUS * 2,
                    p.y - PARTICLE_RADIUS * 2,
                    PARTICLE_RADIUS * 4,
                    PARTICLE_RADIUS * 4));

            // Draw particle
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.setColor(particleColor);
            g2d.fill(new Ellipse2D.Double(
                    p.x - PARTICLE_RADIUS,
                    p.y - PARTICLE_RADIUS,
                    PARTICLE_RADIUS * 2,
                    PARTICLE_RADIUS * 2));
        }

        // Draw permanent fields
        for (Field field : fields) {
            Color fieldColor = field.isAttracting ? new Color(0, 255, 0, 50) : new Color(255, 0, 0, 50);
            g2d.setColor(fieldColor);
            int fieldRadius = 100;
            g2d.fill(new Ellipse2D.Double(
                    field.x - fieldRadius,
                    field.y - fieldRadius,
                    fieldRadius * 2,
                    fieldRadius * 2));

            // Draw field center
            g2d.setColor(field.isAttracting ? Color.GREEN : Color.RED);
            g2d.fill(new Ellipse2D.Double(
                    field.x - 5,
                    field.y - 5,
                    10,
                    10));
        }

        // Draw mouse influence area
        if ((isAttracting || isRepelling) && mousePos != null) {
            Color fieldColor = isAttracting ? new Color(0, 255, 0, 50) : new Color(255, 0, 0, 50);
            g2d.setColor(fieldColor);
            int fieldRadius = 100;
            g2d.fill(new Ellipse2D.Double(
                    mousePos.x - fieldRadius,
                    mousePos.y - fieldRadius,
                    fieldRadius * 2,
                    fieldRadius * 2));
        }

        // Draw orbital mode drag line
        if (isOrbitalMode && mousePos != null && isAttracting) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(mousePos.x, mousePos.y,
                    (int) mousePos.getX(), (int) mousePos.getY());
        }

        // Draw instructions
        drawInstructions(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(30, 30, 40));
        int gridSize = 50;

        for (int x = 0; x < getWidth(); x += gridSize) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += gridSize) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawVector(Graphics2D g2d, double x, double y, double dx, double dy, Color color) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        double angle = Math.atan2(dy, dx);
        int arrowSize = 8;

        g2d.draw(new Line2D.Double(x, y, x + dx, y + dy));
        g2d.draw(new Line2D.Double(
                x + dx,
                y + dy,
                x + dx - arrowSize * Math.cos(angle - Math.PI / 6),
                y + dy - arrowSize * Math.sin(angle - Math.PI / 6)));
        g2d.draw(new Line2D.Double(
                x + dx,
                y + dy,
                x + dx - arrowSize * Math.cos(angle + Math.PI / 6),
                y + dy - arrowSize * Math.sin(angle + Math.PI / 6)));
    }

    private void drawInstructions(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        int y = 25;
        int lineHeight = 20;

        g2d.drawString("Controls:", 10, y);
        y += lineHeight;
        if (isFieldPlacement) {
            g2d.drawString("Left Click: Place Attraction Field", 10, y);
            y += lineHeight;
            g2d.drawString("Right Click: Place Repulsion Field", 10, y);
        } else {
            g2d.drawString("Left Click: " + (isOrbitalMode ? "Create orbital particle (drag)" : "Attract"), 10, y);
            y += lineHeight;
            g2d.drawString("Right Click: Repel", 10, y);
        }
        y += lineHeight;
        g2d.drawString("Middle Click: Add Particle", 10, y);
        y += lineHeight;
        g2d.drawString("Space: Add 10 Random Particles", 10, y);
        y += lineHeight;
        g2d.drawString("O: Toggle Orbital Mode", 10, y);
        y += lineHeight;
        g2d.drawString("C: Clear All", 10, y);
        y += lineHeight;
        g2d.drawString("Particles: " + particles.size(), 10, y);
        y += lineHeight;
        g2d.drawString("Mode: " + (isOrbitalMode ? "Orbital" : "Interactive"), 10, y);
    }

    private static class Particle {
        double x, y;
        double vx, vy;
        double lastFx, lastFy;
        ArrayList<Point2D> trail;

        public Particle(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.trail = new ArrayList<>();
        }
    }

    private static class Field {
        double x, y;
        boolean isAttracting;

        public Field(double x, double y, boolean isAttracting) {
            this.x = x;
            this.y = y;
            this.isAttracting = isAttracting;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Interactive Orbital Particle Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new ParticleGravitySimulation());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}