package AtomSimulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AtomSimulation extends JFrame {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 800;
    private static final int NUCLEUS_RADIUS = 20;
    private static final int ORBIT_RADIUS = 150;
    private static final int NUM_ELECTRONS = 6;
    private static final double ELECTRON_SPEED = 2.0;
    private static final double QUANTUM_JUMP_PROBABILITY = 0.001;
    private static final double PLANCK_CONSTANT = 6.626e-34; // J⋅s
    private static final double SPEED_OF_LIGHT = 3e8; // m/s

    private SimulationPanel simulationPanel;
    private List<Electron> electrons;
    private List<Photon> photons;
    private Point nucleus;
    private boolean isSimulating;
    private Timer animationTimer;
    private Random random;
    private JLabel infoLabel;
    private JLabel energyLabel;
    private JLabel wavelengthLabel;
    private JPanel infoPanel;
    private JPanel controlPanel;
    private JSlider temperatureSlider;
    private JSlider excitationSlider;
    private double temperature = 300; // Kelvin
    private double excitationEnergy = 0; // eV

    public AtomSimulation() {
        // Initialize simulation variables
        nucleus = new Point(WIDTH / 2, HEIGHT / 2);
        electrons = new ArrayList<>();
        photons = new ArrayList<>();
        random = new Random();

        // Create electrons with different energy levels
        for (int i = 0; i < NUM_ELECTRONS; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = ORBIT_RADIUS * (0.5 + 0.5 * (i % 2));
            electrons.add(new Electron(
                    nucleus.x + (int) (radius * Math.cos(angle)),
                    nucleus.y + (int) (radius * Math.sin(angle)),
                    radius,
                    ELECTRON_SPEED * (1.0 + 0.5 * (i % 2)),
                    i % 2 // Energy level (0 or 1)
            ));
        }

        // Create main frame
        setTitle("Quantum Atom Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create simulation panel
        simulationPanel = new SimulationPanel();
        add(simulationPanel, BorderLayout.CENTER);

        // Create info panel
        createInfoPanel();

        // Create control panel
        createControlPanel();

        // Create animation timer
        animationTimer = new Timer(16, e -> {
            if (!isSimulating)
                return;

            // Update electron positions and quantum states
            updateElectrons();

            // Update photons
            updatePhotons();

            // Update temperature effects
            applyTemperatureEffects();

            simulationPanel.repaint();
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void createInfoPanel() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Quantum Information"));

        // Energy level distribution
        infoLabel = new JLabel("<html>Energy Level Distribution:<br>Ground State: 3<br>Excited State: 3</html>");
        infoPanel.add(infoLabel);

        // Energy information
        energyLabel = new JLabel("<html>Average Energy: 0.0 eV<br>Temperature: 300K</html>");
        infoPanel.add(energyLabel);

        // Wavelength information
        wavelengthLabel = new JLabel("<html>Emission Wavelength: -- nm<br>Absorption Wavelength: -- nm</html>");
        infoPanel.add(wavelengthLabel);

        // Add temperature control
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
            energyLabel.setText(String.format("<html>Average Energy: %.2f eV<br>Temperature: %.0fK</html>",
                    calculateAverageEnergy(), temperature));
        });

        tempPanel.add(new JLabel("Temperature (K):"));
        tempPanel.add(temperatureSlider);
        infoPanel.add(tempPanel);
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        JButton startStopBtn = new JButton("Start/Stop");
        JButton exciteBtn = new JButton("Excite Atom");
        JButton resetBtn = new JButton("Reset");
        JButton addPhotonBtn = new JButton("Add Photon");

        // Add excitation energy control
        JPanel excitationPanel = new JPanel();
        excitationPanel.setLayout(new BoxLayout(excitationPanel, BoxLayout.Y_AXIS));
        excitationPanel.setBorder(BorderFactory.createTitledBorder("Excitation Energy"));

        excitationSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        excitationSlider.setMajorTickSpacing(20);
        excitationSlider.setMinorTickSpacing(5);
        excitationSlider.setPaintTicks(true);
        excitationSlider.setPaintLabels(true);
        excitationSlider.addChangeListener(e -> {
            excitationEnergy = excitationSlider.getValue() / 10.0;
        });

        excitationPanel.add(new JLabel("Energy (eV):"));
        excitationPanel.add(excitationSlider);

        // Add control handlers
        startStopBtn.addActionListener(e -> {
            isSimulating = !isSimulating;
            if (isSimulating) {
                animationTimer.start();
            } else {
                animationTimer.stop();
            }
        });

        exciteBtn.addActionListener(e -> {
            if (!electrons.isEmpty()) {
                int index = random.nextInt(electrons.size());
                Electron electron = electrons.get(index);
                if (electron.getEnergyLevel() == 0) {
                    electron.setEnergyLevel(1);
                    electron.setRadius(ORBIT_RADIUS * 1.5);
                    electron.setSpeed(ELECTRON_SPEED * 1.5);
                    updateInfoLabel("Excited");
                    createPhoton(electron, false);
                }
            }
        });

        resetBtn.addActionListener(e -> {
            resetSimulation();
        });

        addPhotonBtn.addActionListener(e -> {
            if (!electrons.isEmpty()) {
                int index = random.nextInt(electrons.size());
                Electron electron = electrons.get(index);
                createPhoton(electron, true);
            }
        });

        controlPanel.add(new JLabel("Controls:"));
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(startStopBtn);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(exciteBtn);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(addPhotonBtn);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(resetBtn);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(excitationPanel);

        // Add info and control panels to EAST
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
        eastPanel.add(infoPanel);
        eastPanel.add(controlPanel);
        add(eastPanel, BorderLayout.EAST);
    }

    private void updateElectrons() {
        for (Electron electron : electrons) {
            // Move electron in circular orbit
            double angle = Math.atan2(electron.y - nucleus.y, electron.x - nucleus.x);
            angle += electron.getSpeed() / electron.getRadius();

            electron.x = nucleus.x + (int) (electron.getRadius() * Math.cos(angle));
            electron.y = nucleus.y + (int) (electron.getRadius() * Math.sin(angle));

            // Quantum jump possibility based on temperature
            if (random.nextDouble() < QUANTUM_JUMP_PROBABILITY * (temperature / 300.0)) {
                if (electron.getEnergyLevel() == 0) {
                    electron.setEnergyLevel(1);
                    electron.setRadius(ORBIT_RADIUS * 1.5);
                    electron.setSpeed(ELECTRON_SPEED * 1.5);
                    createPhoton(electron, false);
                } else {
                    electron.setEnergyLevel(0);
                    electron.setRadius(ORBIT_RADIUS * 0.5);
                    electron.setSpeed(ELECTRON_SPEED);
                    createPhoton(electron, true);
                }
                updateInfoLabel("Quantum Jump");
            }
        }
    }

    private void updatePhotons() {
        for (int i = photons.size() - 1; i >= 0; i--) {
            Photon photon = photons.get(i);
            photon.update();
            if (photon.isExpired()) {
                photons.remove(i);
            }
        }
    }

    private void applyTemperatureEffects() {
        // Temperature affects quantum jump probability and electron behavior
        double tempFactor = temperature / 300.0;
        for (Electron electron : electrons) {
            if (electron.getEnergyLevel() == 1) {
                // Higher temperature increases probability of de-excitation
                if (random.nextDouble() < QUANTUM_JUMP_PROBABILITY * tempFactor) {
                    electron.setEnergyLevel(0);
                    electron.setRadius(ORBIT_RADIUS * 0.5);
                    electron.setSpeed(ELECTRON_SPEED);
                    createPhoton(electron, true);
                }
            }
        }
    }

    private void createPhoton(Electron electron, boolean isEmission) {
        double wavelength = calculateWavelength(electron.getEnergyLevel());
        photons.add(new Photon(
                electron.x,
                electron.y,
                wavelength,
                isEmission));
        updateWavelengthLabel();
    }

    private double calculateWavelength(int energyLevel) {
        // Simplified calculation using E = hc/λ
        double energy = energyLevel * 1.6e-19; // Convert to Joules
        return (PLANCK_CONSTANT * SPEED_OF_LIGHT) / energy * 1e9; // Convert to nm
    }

    private double calculateAverageEnergy() {
        return electrons.stream()
                .mapToDouble(Electron::getEnergyLevel)
                .average()
                .orElse(0.0);
    }

    private void updateInfoLabel(String state) {
        long groundState = electrons.stream().filter(e -> e.getEnergyLevel() == 0).count();
        long excitedState = electrons.stream().filter(e -> e.getEnergyLevel() == 1).count();
        infoLabel.setText(
                String.format("<html>Energy Level Distribution:<br>Ground State: %d<br>Excited State: %d</html>",
                        groundState, excitedState));
    }

    private void updateWavelengthLabel() {
        double emissionWavelength = calculateWavelength(1);
        double absorptionWavelength = calculateWavelength(0);
        wavelengthLabel
                .setText(String.format("<html>Emission Wavelength: %.1f nm<br>Absorption Wavelength: %.1f nm</html>",
                        emissionWavelength, absorptionWavelength));
    }

    private void resetSimulation() {
        for (int i = 0; i < electrons.size(); i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = ORBIT_RADIUS * (0.5 + 0.5 * (i % 2));
            electrons.get(i).setPosition(
                    nucleus.x + (int) (radius * Math.cos(angle)),
                    nucleus.y + (int) (radius * Math.sin(angle)));
            electrons.get(i).setRadius(radius);
            electrons.get(i).setSpeed(ELECTRON_SPEED * (1.0 + 0.5 * (i % 2)));
            electrons.get(i).setEnergyLevel(i % 2);
        }
        photons.clear();
        updateInfoLabel("Stable");
        updateWavelengthLabel();
    }

    private class SimulationPanel extends JPanel {
        public SimulationPanel() {
            setPreferredSize(new Dimension(WIDTH - 150, HEIGHT));
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw nucleus
            g2d.setColor(Color.RED);
            g2d.fillOval(nucleus.x - NUCLEUS_RADIUS, nucleus.y - NUCLEUS_RADIUS,
                    NUCLEUS_RADIUS * 2, NUCLEUS_RADIUS * 2);

            // Draw electron orbits
            g2d.setColor(new Color(50, 50, 50));
            g2d.drawOval(nucleus.x - ORBIT_RADIUS, nucleus.y - ORBIT_RADIUS,
                    ORBIT_RADIUS * 2, ORBIT_RADIUS * 2);
            g2d.drawOval(nucleus.x - ORBIT_RADIUS / 2, nucleus.y - ORBIT_RADIUS / 2,
                    ORBIT_RADIUS, ORBIT_RADIUS);

            // Draw photons
            for (Photon photon : photons) {
                photon.draw(g2d);
            }

            // Draw electrons
            for (Electron electron : electrons) {
                // Draw electron trail
                g2d.setColor(new Color(0, 0, 255, 50));
                g2d.fillOval(electron.x - 8, electron.y - 8, 16, 16);

                // Draw electron
                g2d.setColor(electron.getEnergyLevel() == 1 ? Color.YELLOW : Color.BLUE);
                g2d.fillOval(electron.x - 5, electron.y - 5, 10, 10);
            }
        }
    }

    private static class Electron {
        int x, y;
        private double radius;
        private double speed;
        private int energyLevel;

        public Electron(int x, int y, double radius, double speed, int energyLevel) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.speed = speed;
            this.energyLevel = energyLevel;
        }

        public void setPosition(int x, int y) {
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

        public double getRadius() {
            return radius;
        }

        public double getSpeed() {
            return speed;
        }

        public int getEnergyLevel() {
            return energyLevel;
        }
    }

    private static class Photon {
        private int x, y;
        private double wavelength;
        private boolean isEmission;
        private int lifetime;
        private static final int MAX_LIFETIME = 30;

        public Photon(int x, int y, double wavelength, boolean isEmission) {
            this.x = x;
            this.y = y;
            this.wavelength = wavelength;
            this.isEmission = isEmission;
            this.lifetime = MAX_LIFETIME;
        }

        public void update() {
            lifetime--;
            // Move photon outward
            x += (isEmission ? 2 : -2);
        }

        public boolean isExpired() {
            return lifetime <= 0;
        }

        public void draw(Graphics2D g2d) {
            // Color based on wavelength (simplified)
            int red = (int) (wavelength > 600 ? 255 : wavelength * 0.425);
            int green = (int) (wavelength > 500 && wavelength < 600 ? 255 : wavelength * 0.85);
            int blue = (int) (wavelength < 500 ? 255 : wavelength * 1.7);

            g2d.setColor(new Color(red, green, blue, (int) (255 * lifetime / MAX_LIFETIME)));
            g2d.fillOval(x - 3, y - 3, 6, 6);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AtomSimulation().setVisible(true);
        });
    }
}