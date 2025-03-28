package AtomSimulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Atom3DSimulation extends JFrame {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    private static final float NUCLEUS_RADIUS = 20;
    private static final float ORBIT_RADIUS = 100;
    private static final int NUM_ELECTRONS = 6;
    private static final double ELECTRON_SPEED = 0.02;
    private static final double QUANTUM_JUMP_PROBABILITY = 0.001;
    private static final double PLANCK_CONSTANT = 6.626e-34;
    private static final double SPEED_OF_LIGHT = 3e8;

    private DrawingPanel canvas;
    private List<Electron> electrons;
    private List<Photon> photons;
    private Random random;
    private boolean isSimulating = true;
    private double temperature = 300;
    private double excitationEnergy = 0;
    private Timer animationTimer;

    // UI Components
    private JPanel controlPanel;
    private JLabel infoLabel;
    private JLabel energyLabel;
    private JLabel wavelengthLabel;
    private JSlider temperatureSlider;
    private JSlider excitationSlider;

    private double rotationX = 0.0;
    private double rotationY = 0.0;
    private double zoom = 1.0;

    public Atom3DSimulation() {
        // Initialize simulation variables
        electrons = new ArrayList<>();
        photons = new ArrayList<>();
        random = new Random();

        // Create canvas
        canvas = new DrawingPanel();
        canvas.setPreferredSize(new Dimension(WIDTH - 150, HEIGHT));

        // Create electrons with different orbital planes
        for (int i = 0; i < NUM_ELECTRONS; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = ORBIT_RADIUS * (0.5 + 0.5 * (i % 2));
            electrons.add(new Electron(
                    radius * Math.cos(angle),
                    radius * Math.sin(angle),
                    radius,
                    ELECTRON_SPEED * (1.0 + 0.5 * (i % 2)),
                    i % 2,
                    angle));
        }

        // Create main frame
        setTitle("Quantum Atom Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Add canvas
        add(canvas, BorderLayout.CENTER);

        // Create control panel
        createControlPanel();

        // Start animation
        startAnimation();

        // Add mouse listeners for rotation
        canvas.addMouseListener(new MouseAdapter() {
            private Point lastPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            private Point lastPoint;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    int dx = e.getX() - lastPoint.x;
                    int dy = e.getY() - lastPoint.y;
                    rotationX += dy * 0.01;
                    rotationY += dx * 0.01;
                    canvas.repaint();
                }
                lastPoint = e.getPoint();
            }
        });

        canvas.addMouseWheelListener(e -> {
            zoom *= (e.getWheelRotation() < 0) ? 1.1 : 0.9;
            zoom = Math.max(0.1, Math.min(zoom, 5.0));
            canvas.repaint();
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        // Info labels
        infoLabel = new JLabel("<html>Energy Level Distribution:<br>Ground State: 3<br>Excited State: 3</html>");
        energyLabel = new JLabel("<html>Average Energy: 0.0 eV<br>Temperature: 300K</html>");
        wavelengthLabel = new JLabel("<html>Emission Wavelength: -- nm<br>Absorption Wavelength: -- nm</html>");

        // Temperature control
        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
        tempPanel.setBorder(BorderFactory.createTitledBorder("Temperature Control"));
        temperatureSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 300);
        temperatureSlider.setMajorTickSpacing(200);
        temperatureSlider.setMinorTickSpacing(50);
        temperatureSlider.setPaintTicks(true);
        temperatureSlider.setPaintLabels(true);
        temperatureSlider.addChangeListener(e -> {
            temperature = temperatureSlider.getValue();
            updateEnergyLabel();
        });

        // Control buttons
        JButton startStopBtn = new JButton("Start/Stop");
        JButton exciteBtn = new JButton("Excite Atom");
        JButton resetBtn = new JButton("Reset");
        JButton addPhotonBtn = new JButton("Add Photon");

        // Add components
        controlPanel.add(infoLabel);
        controlPanel.add(energyLabel);
        controlPanel.add(wavelengthLabel);
        controlPanel.add(tempPanel);
        tempPanel.add(new JLabel("Temperature (K):"));
        tempPanel.add(temperatureSlider);
        controlPanel.add(startStopBtn);
        controlPanel.add(exciteBtn);
        controlPanel.add(addPhotonBtn);
        controlPanel.add(resetBtn);

        // Add control panel to frame
        add(controlPanel, BorderLayout.EAST);

        // Add button handlers
        startStopBtn.addActionListener(e -> {
            isSimulating = !isSimulating;
            if (isSimulating) {
                startAnimation();
            } else {
                stopAnimation();
            }
        });

        exciteBtn.addActionListener(e -> {
            if (!electrons.isEmpty()) {
                int index = random.nextInt(electrons.size());
                Electron electron = electrons.get(index);
                if (electron.getEnergyLevel() == 0) {
                    exciteElectron(electron);
                }
            }
        });

        resetBtn.addActionListener(e -> resetSimulation());
    }

    private void startAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        animationTimer = new Timer(16, e -> {
            updateSimulation();
            canvas.repaint();
        });
        animationTimer.start();
    }

    private void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    private void updateSimulation() {
        if (!isSimulating)
            return;

        // Update electrons
        for (Electron electron : electrons) {
            electron.update();

            // Quantum jump possibility
            if (random.nextDouble() < QUANTUM_JUMP_PROBABILITY * (temperature / 300.0)) {
                if (electron.getEnergyLevel() == 0) {
                    exciteElectron(electron);
                } else {
                    deexciteElectron(electron);
                }
            }
        }

        // Update photons
        for (int i = photons.size() - 1; i >= 0; i--) {
            Photon photon = photons.get(i);
            photon.update();
            if (photon.isExpired()) {
                photons.remove(i);
            }
        }
    }

    private void exciteElectron(Electron electron) {
        electron.setEnergyLevel(1);
        electron.setRadius(ORBIT_RADIUS * 1.5);
        electron.setSpeed(ELECTRON_SPEED * 1.5);
        createPhoton(electron, false);
        updateInfoLabel();
    }

    private void deexciteElectron(Electron electron) {
        electron.setEnergyLevel(0);
        electron.setRadius(ORBIT_RADIUS * 0.5);
        electron.setSpeed(ELECTRON_SPEED);
        createPhoton(electron, true);
        updateInfoLabel();
    }

    private void createPhoton(Electron electron, boolean isEmission) {
        double wavelength = calculateWavelength(electron.getEnergyLevel());
        photons.add(new Photon(
                electron.x, electron.y,
                wavelength,
                isEmission));
        updateWavelengthLabel();
    }

    private double calculateWavelength(int energyLevel) {
        double energy = energyLevel * 1.6e-19;
        return (PLANCK_CONSTANT * SPEED_OF_LIGHT) / energy * 1e9;
    }

    private void updateInfoLabel() {
        long groundState = electrons.stream().filter(e -> e.getEnergyLevel() == 0).count();
        long excitedState = electrons.stream().filter(e -> e.getEnergyLevel() == 1).count();
        infoLabel.setText(
                String.format("<html>Energy Level Distribution:<br>Ground State: %d<br>Excited State: %d</html>",
                        groundState, excitedState));
    }

    private void updateEnergyLabel() {
        energyLabel.setText(String.format("<html>Average Energy: %.2f eV<br>Temperature: %.0fK</html>",
                calculateAverageEnergy(), temperature));
    }

    private void updateWavelengthLabel() {
        double emissionWavelength = calculateWavelength(1);
        double absorptionWavelength = calculateWavelength(0);
        wavelengthLabel
                .setText(String.format("<html>Emission Wavelength: %.1f nm<br>Absorption Wavelength: %.1f nm</html>",
                        emissionWavelength, absorptionWavelength));
    }

    private double calculateAverageEnergy() {
        return electrons.stream()
                .mapToDouble(Electron::getEnergyLevel)
                .average()
                .orElse(0.0);
    }

    private void resetSimulation() {
        for (int i = 0; i < electrons.size(); i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = ORBIT_RADIUS * (0.5 + 0.5 * (i % 2));
            electrons.get(i).setPosition(
                    radius * Math.cos(angle),
                    radius * Math.sin(angle));
            electrons.get(i).setRadius(radius);
            electrons.get(i).setSpeed(ELECTRON_SPEED * (1.0 + 0.5 * (i % 2)));
            electrons.get(i).setEnergyLevel(i % 2);
            electrons.get(i).setAngle(angle);
        }
        photons.clear();
        updateInfoLabel();
        updateWavelengthLabel();
    }

    private class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setBackground(Color.BLACK);
            g2d.clearRect(0, 0, getWidth(), getHeight());

            // Set coordinate system to center of panel
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2d.translate(centerX, centerY);

            // Apply zoom
            g2d.scale(zoom, zoom);

            // Draw orbital circles with perspective
            g2d.setColor(new Color(50, 50, 50, 50));
            drawOrbitalEllipse(g2d, ORBIT_RADIUS * 0.5);
            drawOrbitalEllipse(g2d, ORBIT_RADIUS * 1.5);

            // Sort objects by Z-order for proper depth rendering
            List<Renderable> renderables = new ArrayList<>();

            // Add nucleus
            renderables.add(new Renderable(0, 0, 0, () -> {
                g2d.setColor(Color.RED);
                double nucleusSize = 20 * Math.cos(rotationX);
                nucleusSize = Math.max(5, Math.abs(nucleusSize));
                g2d.fillOval((int) (-nucleusSize / 2), (int) (-nucleusSize / 2),
                        (int) nucleusSize, (int) nucleusSize);
            }));

            // Add electrons
            for (Electron electron : electrons) {
                Point3D p = transform3D(electron.x, electron.y, 0);
                renderables.add(new Renderable(p.x, p.y, p.z, () -> {
                    g2d.setColor(electron.getEnergyLevel() == 1 ? Color.YELLOW : Color.BLUE);
                    double scale = (p.z + 200) / 400.0; // Scale based on Z position
                    int size = (int) (10 * scale);
                    size = Math.max(3, size);
                    Point screen = projectToScreen(p);
                    g2d.fillOval(screen.x - size / 2, screen.y - size / 2, size, size);
                }));
            }

            // Add photons
            for (Photon photon : photons) {
                Point3D p = transform3D(photon.x, photon.y, 0);
                renderables.add(new Renderable(p.x, p.y, p.z, () -> {
                    g2d.setColor(photon.getColor());
                    float alpha = photon.getAlpha();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    Point screen = projectToScreen(p);
                    g2d.fillOval(screen.x - 3, screen.y - 3, 6, 6);
                }));
            }

            // Sort by Z-order and render
            renderables.sort((a, b) -> Double.compare(b.z, a.z));
            for (Renderable r : renderables) {
                r.render.run();
            }

            // Reset composite for text
            g2d.setComposite(AlphaComposite.SrcOver);

            // Draw rotation info
            g2d.setColor(Color.WHITE);
            g2d.drawString(String.format("Rotation: X=%.1f° Y=%.1f° Zoom=%.1fx",
                    Math.toDegrees(rotationX), Math.toDegrees(rotationY), zoom), -centerX + 10, -centerY + 20);
        }

        private Point3D transform3D(double x, double y, double z) {
            // Apply rotation transformations
            double cosX = Math.cos(rotationX);
            double sinX = Math.sin(rotationX);
            double cosY = Math.cos(rotationY);
            double sinY = Math.sin(rotationY);

            // Rotate around Y axis
            double x2 = x * cosY - z * sinY;
            double z2 = x * sinY + z * cosY;

            // Rotate around X axis
            double y2 = y * cosX - z2 * sinX;
            z2 = y * sinX + z2 * cosX;

            return new Point3D(x2, y2, z2);
        }

        private Point projectToScreen(Point3D p3d) {
            // Simple perspective projection
            double scale = 400.0 / (400.0 + p3d.z);
            int x = (int) (p3d.x * scale);
            int y = (int) (p3d.y * scale);
            return new Point(x, y);
        }

        private void drawOrbitalEllipse(Graphics2D g2d, double radius) {
            // Draw orbital path with perspective
            double aspectRatio = Math.cos(rotationX);
            if (aspectRatio < 0)
                aspectRatio = -aspectRatio;
            int width = (int) (radius * 2);
            int height = (int) (radius * 2 * aspectRatio);
            g2d.drawOval(-width / 2, -height / 2, width, height);
        }
    }

    private static class Electron {
        double x, y;
        private double radius;
        private double speed;
        private int energyLevel;
        private double angle;

        public Electron(double x, double y, double radius, double speed,
                int energyLevel, double angle) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speed = speed;
            this.energyLevel = energyLevel;
            this.angle = angle;
        }

        public void update() {
            angle += speed;
            x = radius * Math.cos(angle);
            y = radius * Math.sin(angle);
        }

        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public void setEnergyLevel(int level) {
            this.energyLevel = level;
        }

        public void setAngle(double angle) {
            this.angle = angle;
        }

        public int getEnergyLevel() {
            return energyLevel;
        }
    }

    private static class Photon {
        double x, y;
        private double wavelength;
        private boolean isEmission;
        private int lifetime;
        private static final int MAX_LIFETIME = 30;

        public Photon(double x, double y, double wavelength, boolean isEmission) {
            this.x = x;
            this.y = y;
            this.wavelength = wavelength;
            this.isEmission = isEmission;
            this.lifetime = MAX_LIFETIME;
        }

        public void update() {
            lifetime--;
            // Move photon outward
            double direction = isEmission ? 1.0 : -1.0;
            x += direction * 5;
            y += direction * 5;
        }

        public boolean isExpired() {
            return lifetime <= 0;
        }

        public Color getColor() {
            // Color based on wavelength (simplified)
            // Map wavelength to visible spectrum (380-750nm)
            double normalizedWavelength = Math.min(Math.max(wavelength, 380), 750);
            float hue = (float) ((normalizedWavelength - 380) / (750 - 380));
            return Color.getHSBColor(hue, 1.0f, 1.0f);
        }

        public float getAlpha() {
            return (float) lifetime / MAX_LIFETIME;
        }
    }

    private static class Point3D {
        double x, y, z;

        Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class Renderable {
        double x, y, z;
        Runnable render;

        Renderable(double x, double y, double z, Runnable render) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.render = render;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Atom3DSimulation().setVisible(true);
        });
    }
}