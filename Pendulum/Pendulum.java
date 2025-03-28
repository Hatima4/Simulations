package Pendulum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Pendulum extends JPanel implements ActionListener {
    // Physical parameters
    private double angle = Math.PI / 4; // Initial angle (45 degrees)
    private double angleVelocity = 0; // Angular velocity
    private double angleAcceleration; // Angular acceleration
    private final double length = 200; // Length of pendulum (pixels)
    private final double gravity = 9.81; // Acceleration due to gravity (m/s^2)
    private final double mass = 1.0; // Mass of bob (kg)
    private final double damping = 0.01; // Damping factor

    // Display parameters
    private final int pivotX; // X coordinate of pivot point
    private final int pivotY; // Y coordinate of pivot point
    private final int bobRadius = 20; // Radius of the bob
    private boolean isDragging = false; // Is user dragging the pendulum?
    private Timer timer; // Animation timer

    public Pendulum() {
        // Set up the panel
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        // Calculate pivot point (center-top of panel)
        pivotX = 400;
        pivotY = 50;

        // Add mouse listeners for interaction
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Check if click is near the bob
                double bobX = pivotX + length * Math.sin(angle);
                double bobY = pivotY + length * Math.cos(angle);
                if (Point.distance(e.getX(), e.getY(), bobX, bobY) < bobRadius) {
                    isDragging = true;
                    timer.stop();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    isDragging = false;
                    angleVelocity = 0; // Reset velocity when released
                    timer.start();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    // Calculate new angle based on mouse position
                    double dx = e.getX() - pivotX;
                    double dy = e.getY() - pivotY;
                    angle = Math.atan2(dx, dy);
                    repaint();
                }
            }
        });

        // Set up animation timer (60 FPS)
        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate bob position
        int bobX = pivotX + (int) (length * Math.sin(angle));
        int bobY = pivotY + (int) (length * Math.cos(angle));

        // Draw string
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.BLACK);
        g2d.drawLine(pivotX, pivotY, bobX, bobY);

        // Draw pivot
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillOval(pivotX - 5, pivotY - 5, 10, 10);

        // Draw bob
        g2d.setColor(Color.RED);
        g2d.fillOval(bobX - bobRadius, bobY - bobRadius, 2 * bobRadius, 2 * bobRadius);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isDragging) {
            // Calculate acceleration (using small angle approximation)
            angleAcceleration = -(gravity / length) * Math.sin(angle);

            // Apply damping
            angleAcceleration -= damping * angleVelocity;

            // Update velocity and position using Euler integration
            angleVelocity += angleAcceleration;
            angle += angleVelocity;
        }
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pendulum Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new Pendulum());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}