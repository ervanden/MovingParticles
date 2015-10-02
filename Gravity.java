
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;

class Gravity implements Animation {

    JPanel pointPane;
    JPanel pane;

    ArrayList<Point> particles = new ArrayList<>();

    public Gravity() {

        particles = MovingParticles.Drawing.getPoints();

        for (Point p : particles) {
            p.velocity = 0;
            p.angle = 0;
            p.x0 = p.x;
            p.y0 = p.y;
            p.xLastDrawn = p.x;
            p.yLastDrawn = p.y;
            p.trajectory = null;
        }

        pane = new JPanel();
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        Border blackline = BorderFactory.createLineBorder(Color.black);
        pane.setBorder(blackline);
    }

    public JPanel getPane() {
        return pane;
    }

    public ArrayList<Point> getParticles() {
        return new ArrayList<Point>(particles);
    }

    public boolean step(double dt, int resolution) {
        // This method calculates the new position of all particles after time step dt.
        // If no points are added to any trajectory, return false, true otherwise.
        // This to avoid redrawing the screen when nothing changed
        boolean redraw = false;

        double potentialEnergy = 0;
        double kineticEnergy = 0;

        for (Point p : particles) {
            kineticEnergy = kineticEnergy + 0.5 * p.mass * (p.vx * p.vx + p.vy * p.vy);
            p.vxnew = p.vx;
            p.vynew = p.vy;
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

                p1.vxnew = p1.vxnew + (ux * force / mass1) * dt;
                p1.vynew = p1.vynew + (uy * force / mass1) * dt;
                p2.vxnew = p2.vxnew - (ux * force / mass2) * dt;
                p2.vynew = p2.vynew - (uy * force / mass2) * dt;
            }
        }

        // new position and speed of all particles
        for (Point p : particles) {
            p.x = p.x + ((p.vx + p.vxnew) / 2) * dt;
            p.y = p.y + ((p.vy + p.vynew) / 2) * dt;
            //          p.x = p.x + p.vx * dt;
            //          p.y = p.y + p.vy * dt;
            p.vx = p.vxnew;
            p.vy = p.vynew;
        }

        for (Point p : particles) {

            double x1, x2, y1, y2;
            x1 = MovingParticles.transform.xUserToScreen(p.x);
            y1 = MovingParticles.transform.yUserToScreen(p.y);
            x2 = MovingParticles.transform.xUserToScreen(p.xLastDrawn);
            y2 = MovingParticles.transform.yUserToScreen(p.yLastDrawn);
            double sqScreenDistance = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
            if (sqScreenDistance > resolution * resolution) {
                if (p.trajectory != null) {
                    MovingParticles.Drawing.addPointToCurve(p.trajectory, p.x, p.y);
                }
                redraw = true;
                p.xLastDrawn = p.x;
                p.yLastDrawn = p.y;
            }

        }

        MovingParticles.Drawing.setString(0, String.format("K=%.1f", kineticEnergy));
        MovingParticles.Drawing.setString(1, String.format("P=%.1f", potentialEnergy));
        MovingParticles.Drawing.setString(2, String.format("T=%.1f", kineticEnergy + potentialEnergy));

        return redraw;
    }

    public void cleanup() {

    }

}
