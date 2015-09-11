
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class Gravity implements Animation, ActionListener {

    JPanel pointPane;
    JPanel pane;

    ArrayList<Point> particles = new ArrayList<>();

    public Gravity() {

        MovingParticles.Drawing.setString(0, "string2");
        MovingParticles.Drawing.setString(1, "string3xyz");

        for (Shape s : MovingParticles.Drawing.getShapes()) {
            if (s.isPoint) {
                System.out.println("Shape " + s.label);
                for (Point p : s.points) {
                    System.out.println("  point " + s.label);
                    particles.add(p);
                    p.particleName = s.label;
                    p.x_init = p.x;
                    p.y_init = p.y;
                    p.xLastDrawn = p.x;
                    p.yLastDrawn = p.y;
                    p.trajectory = null;
                }
            }
        }

        pane = new JPanel();
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        for (int i = 0; i < particles.size(); i++) {
            Point p = particles.get(i);
            pointPane = new JPanel();
            pointPane.setLayout(new BoxLayout(pointPane, BoxLayout.LINE_AXIS));
            pointPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            pointPane.add(Box.createRigidArea(new Dimension(40, 0)));
            pointPane.add(new JLabel(p.particleName));
            addTextField(i, "Mass", p.mass);
            addTextField(i, "Velocity", p.velocity);
            addTextField(i, "Angle", p.angle);
            pane.add(pointPane);

        }

    }

    private void addTextField(int i, String label, double value) {
        pointPane.add(Box.createRigidArea(new Dimension(20, 0)));
        pointPane.add(new JLabel(label));
        pointPane.add(Box.createRigidArea(new Dimension(20, 0)));
        JTextField field = new JTextField(String.format("%.2f", value));
        field.addActionListener(this);
        field.setActionCommand(i + "|" + label);
        pointPane.add(field);
    }

    public JPanel getPane() {
        return pane;
    }

    public ArrayList<Point> getParticles() {
        return new ArrayList<Point>(particles);
    }

    public void actionPerformed(ActionEvent e) {
        {
            JTextField field = (JTextField) e.getSource();
            String action = e.getActionCommand();
            int particleNr = Integer.parseInt(action.split("[|]")[0]);
            String attribute = action.split("[|]")[1];
            boolean validValue = true;
            double value = 0d;
            try {
                value = Double.valueOf(field.getText());
            } catch (Exception ex) {
                field.setBackground(Color.yellow);
                validValue = false;
            }
            if (validValue) {
                field.setBackground(Color.white);
                System.out.println(particleNr + "." + attribute + "=" + value);
                if (attribute.equals("Mass")) {
                    particles.get(particleNr).mass = value;
                    field.setBackground(Color.green);
                }
                if (attribute.equals("Velocity")) {
                    particles.get(particleNr).velocity = value;
                    field.setBackground(Color.green);
                }
                if (attribute.equals("Angle")) {
                    particles.get(particleNr).angle = value;
                    field.setBackground(Color.green);
                }
                if (attribute.equals("Velocity") || attribute.equals("Angle")) {
                    for (Point p : particles) {
                        p.vx = p.velocity * Math.cos((p.angle / 180) * Math.PI);
                        p.vy = p.velocity * Math.sin((p.angle / 180) * Math.PI);
                    }
                }
            }
        }

    }

    public boolean step(double dt) {
        // This method calculates the new position of all particles after time step dt.
        // If no points are added to any trajectory, return false, true otherwise.
        // This to avoid redrawing the screen when nothing changed
        boolean redraw = false;

        double potentialEnergy = 0;
        double kineticEnergy = 0;

        for (Point p : particles) {
            kineticEnergy = kineticEnergy + 0.5 * p.mass * (p.vx * p.vx + p.vy * p.vy);
        }

        // new position of all particles
        for (Point p : particles) {
            p.xnew = p.x + p.vx * dt;
            p.ynew = p.y + p.vy * dt;
        }

        // new speed of all particles
        for (int i1 = 0; i1 < particles.size(); i1++) {
            for (int i2 = i1 + 1; i2 < particles.size(); i2++) {
                //               System.out.printf("points %d and %d\n", i1, i2);
                Point p1 = particles.get(i1);
                Point p2 = particles.get(i2);
                double mass1 = p1.mass;
                double mass2 = p2.mass;
                double rsquare = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
                double r = Math.sqrt(rsquare);
                //    System.out.println("distance "+s1.label+" "+s2.label+" "+r);
                double force = mass1 * mass2 / rsquare;

                potentialEnergy = potentialEnergy - mass1 * mass2 / r;

                double ux = (p2.x - p1.x) / r;  //unit vector from p1 to p2
                double uy = (p2.y - p1.y) / r;

                p1.vx = p1.vx + (ux * force / mass1) * dt;
                p1.vy = p1.vy + (uy * force / mass1) * dt;
                p2.vx = p2.vx - (ux * force / mass2) * dt;
                p2.vy = p2.vy - (uy * force / mass2) * dt;
            }
        }

        for (Point p : particles) {
            p.x = p.xnew;
            p.y = p.ynew;

            double x1, x2, y1, y2;
            x1 = MovingParticles.transform.xUserToScreen(p.x);
            y1 = MovingParticles.transform.yUserToScreen(p.y);
            x2 = MovingParticles.transform.xUserToScreen(p.xLastDrawn);
            y2 = MovingParticles.transform.yUserToScreen(p.yLastDrawn);
            double sqScreenDistance = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
            if (sqScreenDistance > 100) {
                if (p.trajectory != null) {
                    p.trajectory.addPoint(p.x, p.y);
                }
                redraw = true;
                p.xLastDrawn = p.x;
                p.yLastDrawn = p.y;
            }

        }

        MovingParticles.Drawing.setString(0, String.format("K=%.1f", kineticEnergy));
        MovingParticles.Drawing.setString(1, String.format("P=%.1f", potentialEnergy));
        MovingParticles.Drawing.setString(2, String.format("T=%.1f", kineticEnergy + potentialEnergy));

        return true;
    }

    public void cleanup() {

    }

}
