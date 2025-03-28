package FluidResistance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class FluidResistanceSimulation extends JFrame {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final double GRAVITY = 9.81;
    private static final double PIXELS_PER_METER = 100.0; // 100 pixels = 1 meter
    private static final double SCALE_FACTOR = 1.0 / PIXELS_PER_METER;

    // Fluid properties (density in kg/m³, viscosity in Pa·s)
    private static final Map<String, FluidProperties> FLUIDS = new HashMap<>();
    static {
        FLUIDS.put("Air", new FluidProperties(1.225, 1.81e-5, 0.47, "Air at sea level"));
        FLUIDS.put("Water", new FluidProperties(997, 8.90e-4, 0.47, "Water at 25°C"));
        FLUIDS.put("Oil", new FluidProperties(850, 0.1, 0.47, "Typical oil"));
        FLUIDS.put("Honey", new FluidProperties(1420, 10.0, 0.47, "Honey at 20°C"));
        FLUIDS.put("Glycerin", new FluidProperties(1261, 1.41, 0.47, "Glycerin at 20°C"));
    }

    private ArrayList<FallingObject> objects;
    private ArrayList<FluidParticle> fluidParticles;
    private Timer timer;
    private Random random;
    private FluidProperties currentFluid;
    private boolean isPaused = false;
    private JLabel infoLabel;
    private JLabel reynoldsLabel;
    private JLabel velocityLabel;
    private JLabel dragForceLabel;
    private static final int NUM_PARTICLES = 200;
    private static final double PARTICLE_SIZE = 4.0;

    private static class FluidProperties {
        final double density;
        final double viscosity;
        final double dragCoefficient;
        final String description;

        FluidProperties(double density, double viscosity, double dragCoefficient, String description) {
            this.density = density;
            this.viscosity = viscosity;
            this.dragCoefficient = dragCoefficient;
            this.description = description;
        }
    }

    private class FluidParticle {
        double x, y;
        double velocityX, velocityY;
        Color color;

        FluidParticle(double x, double y) {
            this.x = x;
            this.y = y;
            this.velocityX = random.nextDouble() * 0.5 - 0.25; // Random horizontal movement
            this.velocityY = random.nextDouble() * 0.5 - 0.25; // Random vertical movement
            this.color = new Color(150, 200, 255, 100); // Semi-transparent blue
        }

        void update() {
            x += velocityX;
            y += velocityY;

            // Bounce off walls
            if (x < 0 || x > WIDTH)
                velocityX *= -1;
            if (y < 0 || y > HEIGHT)
                velocityY *= -1;

            // Keep particles within bounds
            x = Math.max(0, Math.min(WIDTH, x));
            y = Math.max(0, Math.min(HEIGHT, y));
        }

        void draw(Graphics2D g) {
            g.setColor(color);
            g.fillOval((int) (x - PARTICLE_SIZE / 2), (int) (y - PARTICLE_SIZE / 2),
                    (int) PARTICLE_SIZE, (int) PARTICLE_SIZE);
        }
    }

    public FluidResistanceSimulation() {
        setTitle("Fluid Resistance Simulation");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        objects = new ArrayList<>();
        fluidParticles = new ArrayList<>();
        random = new Random();
        currentFluid = FLUIDS.get("Air");

        // Initialize fluid particles
        for (int i = 0; i < NUM_PARTICLES; i++) {
            fluidParticles.add(new FluidParticle(
                    random.nextDouble() * WIDTH,
                    random.nextDouble() * HEIGHT));
        }

        // Create main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create control panel with grid layout
        JPanel controlPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        // Object controls
        JPanel objectControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addSphereButton = new JButton("Add Sphere");
        JButton addCubeButton = new JButton("Add Cube");
        JButton clearButton = new JButton("Clear");
        JButton pauseButton = new JButton("Pause");
        objectControls.add(addSphereButton);
        objectControls.add(addCubeButton);
        objectControls.add(clearButton);
        objectControls.add(pauseButton);

        // Fluid selection
        JPanel fluidControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> fluidSelector = new JComboBox<>(FLUIDS.keySet().toArray(new String[0]));
        JSlider temperatureSlider = new JSlider(0, 100, 25);
        JLabel tempLabel = new JLabel("Temperature: 25°C");
        fluidControls.add(new JLabel("Fluid:"));
        fluidControls.add(fluidSelector);
        fluidControls.add(tempLabel);
        fluidControls.add(temperatureSlider);

        // Object size control
        JPanel sizeControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JSlider sizeSlider = new JSlider(20, 100, 50);
        JLabel sizeLabel = new JLabel("Object Size: 50");
        sizeControls.add(sizeLabel);
        sizeControls.add(sizeSlider);

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Simulation Info"));
        infoLabel = new JLabel("Current Fluid: " + currentFluid.description);
        reynoldsLabel = new JLabel("Reynolds Number: --");
        velocityLabel = new JLabel("Velocity: -- m/s");
        dragForceLabel = new JLabel("Drag Force: -- N");
        infoPanel.add(infoLabel);
        infoPanel.add(reynoldsLabel);
        infoPanel.add(velocityLabel);
        infoPanel.add(dragForceLabel);

        // Add all panels to control panel
        controlPanel.add(objectControls);
        controlPanel.add(fluidControls);
        controlPanel.add(sizeControls);
        controlPanel.add(infoPanel);

        // Add simulation panel
        SimulationPanel simulationPanel = new SimulationPanel();
        simulationPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Add components to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(simulationPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Add event listeners
        addSphereButton.addActionListener(e -> addObject(new Sphere(sizeSlider.getValue())));
        addCubeButton.addActionListener(e -> addObject(new Cube(sizeSlider.getValue())));
        clearButton.addActionListener(e -> clearObjects());
        pauseButton.addActionListener(e -> togglePause());

        fluidSelector.addActionListener(e -> {
            String selectedFluid = (String) fluidSelector.getSelectedItem();
            currentFluid = FLUIDS.get(selectedFluid);
            infoLabel.setText("Current Fluid: " + currentFluid.description);
        });

        temperatureSlider.addChangeListener(e -> {
            int temp = temperatureSlider.getValue();
            tempLabel.setText("Temperature: " + temp + "°C");
            // Update fluid properties based on temperature (simplified)
            updateFluidProperties(temp);
        });

        sizeSlider.addChangeListener(e -> {
            sizeLabel.setText("Object Size: " + sizeSlider.getValue());
        });

        // Setup animation timer
        timer = new Timer(16, e -> {
            if (!isPaused) {
                updateSimulation();
                simulationPanel.repaint();
            }
        });
        timer.start();
    }

    private void updateFluidProperties(int temperature) {
        // Simplified temperature effect on fluid properties
        double tempFactor = 1.0 + (temperature - 25) * 0.01;
        currentFluid = new FluidProperties(
                currentFluid.density * tempFactor,
                currentFluid.viscosity / tempFactor,
                currentFluid.dragCoefficient,
                currentFluid.description + " at " + temperature + "°C");
    }

    private void addObject(FallingObject obj) {
        obj.setX(random.nextInt(WIDTH - 200) + 100);
        obj.setY(50);
        objects.add(obj);
    }

    private void clearObjects() {
        objects.clear();
    }

    private void togglePause() {
        isPaused = !isPaused;
    }

    private void updateSimulation() {
        // Update fluid particles
        for (FluidParticle particle : fluidParticles) {
            particle.update();
        }

        // Update falling objects
        for (FallingObject obj : objects) {
            // Calculate drag force (velocity is already in m/s)
            double velocity = obj.getVelocity();
            double area = obj.getCrossSectionalArea();
            double dragForce = 0.5 * currentFluid.density * velocity * velocity * currentFluid.dragCoefficient * area;

            // Calculate Reynolds number
            double reynoldsNumber = (currentFluid.density * velocity * obj.getCharacteristicLength())
                    / currentFluid.viscosity;

            // Update velocity and position
            double netForce = obj.getMass() * GRAVITY - dragForce;
            double acceleration = netForce / obj.getMass();
            obj.setVelocity(obj.getVelocity() + acceleration * 0.016);
            obj.setY(obj.getY() + obj.getVelocity() * 0.016);

            // Update info labels
            reynoldsLabel.setText(String.format("Reynolds Number: %.2f", reynoldsNumber));
            velocityLabel.setText(String.format("Velocity: %.2f m/s", velocity));
            dragForceLabel.setText(String.format("Drag Force: %.2f N", dragForce));

            // Remove objects that have fallen off screen
            if (obj.getY() > HEIGHT - 100) {
                objects.remove(obj);
            }
        }
    }

    private class SimulationPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw fluid medium (gradient background)
            GradientPaint gradient = new GradientPaint(0, 0, new Color(200, 230, 255),
                    0, HEIGHT, new Color(150, 200, 255));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            // Draw fluid particles
            for (FluidParticle particle : fluidParticles) {
                particle.draw(g2d);
            }

            // Draw objects
            for (FallingObject obj : objects) {
                obj.draw(g2d);
            }
        }
    }

    private abstract class FallingObject {
        protected double x, y;
        protected double velocity;
        protected double mass;
        protected double size;

        public FallingObject(double size) {
            this.size = size;
            this.velocity = 0;
            // Scale mass calculation to realistic values (assuming density of 1000 kg/m³)
            this.mass = (size * SCALE_FACTOR) * (size * SCALE_FACTOR) * (size * SCALE_FACTOR) * 1000;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setVelocity(double velocity) {
            this.velocity = velocity;
        }

        public double getVelocity() {
            return velocity;
        }

        public double getMass() {
            return mass;
        }

        public abstract double getCrossSectionalArea();

        public abstract double getCharacteristicLength();

        public abstract void draw(Graphics2D g);
    }

    private class Sphere extends FallingObject {
        public Sphere(double size) {
            super(size);
        }

        @Override
        public double getCrossSectionalArea() {
            // Convert pixel size to meters
            double radius = (size * SCALE_FACTOR) / 2;
            return Math.PI * radius * radius;
        }

        @Override
        public double getCharacteristicLength() {
            return size * SCALE_FACTOR;
        }

        @Override
        public void draw(Graphics2D g) {
            g.setColor(new Color(255, 100, 100));
            g.fillOval((int) (x - size / 2), (int) (y - size / 2), (int) size, (int) size);
        }
    }

    private class Cube extends FallingObject {
        public Cube(double size) {
            super(size);
        }

        @Override
        public double getCrossSectionalArea() {
            // Convert pixel size to meters
            double side = size * SCALE_FACTOR;
            return side * side;
        }

        @Override
        public double getCharacteristicLength() {
            return size * SCALE_FACTOR;
        }

        @Override
        public void draw(Graphics2D g) {
            g.setColor(new Color(100, 255, 100));
            g.fillRect((int) (x - size / 2), (int) (y - size / 2), (int) size, (int) size);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FluidResistanceSimulation().setVisible(true);
        });
    }
}