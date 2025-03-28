import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.border.*;

public class HyperbolicSpace extends JFrame {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final Color BACKGROUND_COLOR = new Color(25, 25, 35);
    private static final Color LINE_COLOR = new Color(0, 150, 255);
    private static final Color POINT_COLOR = new Color(255, 100, 0);
    private static final Color CIRCLE_COLOR = new Color(100, 255, 100);

    // View parameters
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private Point lastMouse;
    private boolean isDragging = false;

    // Geometry elements
    private ArrayList<Line2D.Double> lines = new ArrayList<>();
    private ArrayList<Point2D.Double> points = new ArrayList<>();
    private ArrayList<Ellipse2D.Double> circles = new ArrayList<>();
    private Point2D.Double selectedPoint = null;
    private Line2D.Double selectedLine = null;
    private int mode = 0; // 0: points, 1: lines, 2: circles, 3: parallel lines demo

    public HyperbolicSpace() {
        setTitle("Hyperbolic Space - Poincaré Half-Plane Model");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Create visualization panel
        JPanel visualPanel = createVisualPanel();
        mainPanel.add(visualPanel, BorderLayout.CENTER);

        // Create control panel
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.EAST);

        add(mainPanel);

        // Mouse listeners
        visualPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastMouse = e.getPoint();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    handleLeftClick(e.getPoint());
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    isDragging = true;
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    isDragging = false;
                }
            }
        });

        visualPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    double dx = e.getX() - lastMouse.x;
                    double dy = e.getY() - lastMouse.y;
                    offsetX += dx;
                    offsetY += dy;
                    lastMouse = e.getPoint();
                    visualPanel.repaint();
                }
            }
        });

        // Mouse wheel for zoom
        visualPanel.addMouseWheelListener(e -> {
            double zoomFactor = (e.getWheelRotation() < 0) ? 1.1 : 0.9;
            scale *= zoomFactor;
            visualPanel.repaint();
        });

        // Create demo parallel lines
        createParallelLinesDemo();
    }

    private JPanel createVisualPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setBackground(BACKGROUND_COLOR);
                g2d.clearRect(0, 0, getWidth(), getHeight());

                // Apply transformations
                g2d.translate(offsetX, offsetY);
                g2d.scale(scale, scale);

                // Draw horizontal line (x-axis)
                g2d.setColor(Color.GRAY);
                g2d.draw(new Line2D.Double(0, HEIGHT / 2, WIDTH, HEIGHT / 2));

                // Draw geometry elements
                g2d.setColor(LINE_COLOR);
                g2d.setStroke(new BasicStroke(2.0f));
                for (Line2D.Double line : lines) {
                    drawHyperbolicLine(g2d, line);
                }

                g2d.setColor(CIRCLE_COLOR);
                for (Ellipse2D.Double circle : circles) {
                    drawHyperbolicCircle(g2d, circle);
                }

                g2d.setColor(POINT_COLOR);
                for (Point2D.Double point : points) {
                    drawPoint(g2d, point);
                }

                // Draw selected elements
                if (selectedPoint != null) {
                    g2d.setColor(Color.YELLOW);
                    drawPoint(g2d, selectedPoint);
                }
                if (selectedLine != null) {
                    g2d.setColor(Color.YELLOW);
                    drawHyperbolicLine(g2d, selectedLine);
                }
            }
        };
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(45, 45, 55));
        panel.setPreferredSize(new Dimension(200, HEIGHT));

        // Mode selection
        JPanel modePanel = new JPanel(new GridLayout(0, 1, 5, 5));
        modePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Mode",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                null,
                Color.WHITE));
        modePanel.setBackground(new Color(45, 45, 55));

        String[] modes = { "Points", "Lines", "Circles", "Parallel Lines Demo" };
        ButtonGroup modeGroup = new ButtonGroup();
        for (int i = 0; i < modes.length; i++) {
            JRadioButton button = new JRadioButton(modes[i]);
            button.setForeground(Color.WHITE);
            final int modeIndex = i;
            button.addActionListener(e -> mode = modeIndex);
            modeGroup.add(button);
            modePanel.add(button);
            if (i == 0)
                button.setSelected(true);
        }

        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(45, 45, 55));

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearAll());

        JButton resetViewButton = new JButton("Reset View");
        resetViewButton.addActionListener(e -> resetView());

        buttonPanel.add(clearButton);
        buttonPanel.add(resetViewButton);

        // Add all components
        panel.add(modePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(buttonPanel);
        panel.add(Box.createVerticalGlue());

        // Instructions
        JLabel instructions = new JLabel(
                "<html>" +
                        "Left click to add points/lines<br>" +
                        "Right click + drag to pan<br>" +
                        "Scroll to zoom<br>" +
                        "Select points to create lines/circles" +
                        "</html>");
        instructions.setForeground(Color.WHITE);
        instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(instructions);

        return panel;
    }

    private void handleLeftClick(Point p) {
        Point2D.Double hyperbolicPoint = screenToHyperbolic(p);

        switch (mode) {
            case 0: // Points
                points.add(hyperbolicPoint);
                break;
            case 1: // Lines
                if (selectedPoint == null) {
                    selectedPoint = hyperbolicPoint;
                } else {
                    lines.add(createHyperbolicLine(selectedPoint, hyperbolicPoint));
                    selectedPoint = null;
                }
                break;
            case 2: // Circles
                if (selectedPoint == null) {
                    selectedPoint = hyperbolicPoint;
                } else {
                    circles.add(createHyperbolicCircle(selectedPoint, hyperbolicPoint));
                    selectedPoint = null;
                }
                break;
        }
        repaint();
    }

    private void createParallelLinesDemo() {
        // Create three parallel lines
        double baseY = HEIGHT / 2 + 100;
        for (int i = 0; i < 3; i++) {
            Point2D.Double p1 = new Point2D.Double(100, baseY + i * 50);
            Point2D.Double p2 = new Point2D.Double(WIDTH - 100, baseY + i * 50);
            lines.add(createHyperbolicLine(p1, p2));
        }
    }

    private Point2D.Double screenToHyperbolic(Point p) {
        return new Point2D.Double(
                (p.x - offsetX) / scale,
                (p.y - offsetY) / scale);
    }

    private void drawPoint(Graphics2D g2d, Point2D.Double p) {
        int size = 6;
        g2d.fill(new Ellipse2D.Double(p.x - size / 2, p.y - size / 2, size, size));
    }

    private void drawHyperbolicLine(Graphics2D g2d, Line2D.Double line) {
        // In the Poincaré half-plane model, lines are either vertical lines or
        // semicircles
        // perpendicular to the x-axis
        double x1 = line.x1;
        double y1 = line.y1;
        double x2 = line.x2;
        double y2 = line.y2;

        if (Math.abs(x2 - x1) < 1) {
            // Vertical line
            g2d.draw(new Line2D.Double(x1, y1, x1, y2));
        } else {
            // Semicircle
            double centerX = (x1 + x2) / 2;
            double radius = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)) / 2;
            double startAngle = Math.atan2(y2 - y1, x2 - x1);
            double endAngle = startAngle + Math.PI;
            g2d.draw(new Arc2D.Double(
                    centerX - radius, y1 - radius,
                    radius * 2, radius * 2,
                    Math.toDegrees(startAngle), 180,
                    Arc2D.OPEN));
        }
    }

    private void drawHyperbolicCircle(Graphics2D g2d, Ellipse2D.Double circle) {
        // In the Poincaré model, circles are circles (but not centered at their
        // Euclidean center)
        g2d.draw(circle);
    }

    private Line2D.Double createHyperbolicLine(Point2D.Double p1, Point2D.Double p2) {
        return new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
    }

    private Ellipse2D.Double createHyperbolicCircle(Point2D.Double center, Point2D.Double point) {
        double radius = center.distance(point);
        return new Ellipse2D.Double(
                center.x - radius,
                center.y - radius,
                radius * 2,
                radius * 2);
    }

    private void clearAll() {
        points.clear();
        lines.clear();
        circles.clear();
        selectedPoint = null;
        selectedLine = null;
        repaint();
    }

    private void resetView() {
        scale = 1.0;
        offsetX = 0;
        offsetY = 0;
        repaint();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new HyperbolicSpace().setVisible(true);
        });
    }
}