
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class Elasticity implements Animation, ChangeListener, ItemListener {

    JPanel pointPane;
    JPanel pane;

    ArrayList<Point> particles = new ArrayList<>();
    ArrayList<Link> links = new ArrayList<>();

    double kStretch = 1;  // elasticity constant  F = k * delta(x)
    double kCompress = 1;
    double v = 0;  // viscosity
    boolean gravity = false;
    boolean viscosity = true;

    JSlider sliderKStretch;
    JSlider sliderKCompress;
    JSlider sliderViscosity;
    JLabel kStretchInfo;
    JLabel kCompressInfo;
    JLabel viscosityInfo;

    JCheckBox gBox;

    public Elasticity() {

        particles = MovingParticles.Drawing.getPoints();
        links = MovingParticles.Drawing.getLinks();

        for (Point p : particles) {
            p.x0 = p.x;
            p.y0 = p.y;
            p.xLastDrawn = p.x;
            p.yLastDrawn = p.y;
            p.trajectory = null;
            p.vx = p.velocity * Math.cos((p.angle / 180) * Math.PI);
            p.vy = p.velocity * Math.sin((p.angle / 180) * Math.PI);
        }

        for (Link l : links) {
            l.r0 = (l.p1.x0 - l.p2.x0) * (l.p1.x0 - l.p2.x0) + (l.p1.y0 - l.p2.y0) * (l.p1.y0 - l.p2.y0);
            l.r0 = Math.sqrt(l.r0);

        }

        pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        Border blackline = BorderFactory.createLineBorder(Color.black);
        pane.setBorder(blackline);
        gBox = new JCheckBox("gravity");
        gBox.addItemListener(this);
        gBox.setEnabled(true);

        kStretchInfo = new JLabel("elasticity constant (stretch)", JLabel.CENTER);
        sliderKStretch = new JSlider(-5000, 3000, 0);
        readKStretch();
        kCompressInfo = new JLabel("elasticity constant (compress)", JLabel.CENTER);
        sliderKCompress = new JSlider(-5000, 3000, 0);
        readKCompress();
        viscosityInfo = new JLabel("viscosity", JLabel.CENTER);
        sliderViscosity = new JSlider(-3000, 3000, -3000);
        readViscosity();

        sliderKStretch.addChangeListener(this);
        sliderKCompress.addChangeListener(this);
        sliderViscosity.addChangeListener(this);

        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(gBox);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(kStretchInfo);
        pane.add(sliderKStretch);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(kCompressInfo);
        pane.add(sliderKCompress);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(viscosityInfo);
        pane.add(sliderViscosity);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
    }

    private void readKStretch() {
        kStretch = Math.pow(10.0, (double) sliderKStretch.getValue() / 1000);
        String kString = String.format("%f", kStretch);
        kStretchInfo.setText("elasticity constant (stretch) = " + kString);
    }

    private void readKCompress() {
        kCompress = Math.pow(10.0, (double) sliderKCompress.getValue() / 1000);
        String kString = String.format("%f", kCompress);
        kCompressInfo.setText("elasticity constant (compress) = " + kString);
    }

    private void readViscosity() {
        if (sliderViscosity.getValue() == -3000) {
            v = 0;
        } else {
            v = Math.pow(10.0, (double) sliderViscosity.getValue() / 1000);
        }
        String kString = String.format("%f", v);
        viscosityInfo.setText("viscosity = " + kString);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(sliderKStretch)) {
            readKStretch();
        }
        if (e.getSource().equals(sliderKCompress)) {
            readKCompress();
        }
        if (e.getSource().equals(sliderViscosity)) {
            readViscosity();
        }
    }

    public void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();

        if (source == gBox) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                gravity = true;
            }
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                gravity = false;
            }
        }
    }

    public JPanel getPane() {
        return pane;
    }

    public ArrayList<Point> getParticles() {
        return new ArrayList<Point>(particles);
    }

    private void dump_state(Point p1, Point p2) {
        System.out.println("== state of all particles");
        for (Point p : particles) {
            System.out.printf("%s mass=%5f x=%5f x_init=%5f y=%5f y_init=%5f vx=%5f vy=%5f\n",
                    p.name,
                    p.mass,
                    p.x,
                    p.x0,
                    p.y,
                    p.y0,
                    p.vx,
                    p.vy
            );
        }
        if (p1 != null) {
            System.out.println("== p1 " + p1.name);
        }
        if (p2 != null) {
            System.out.println("== p2 " + p2.name);
        }
    }

    public static void collidingParticles(ArrayList<Point> particles) {
        ArrayList<Link> collisions = new ArrayList<>();
        for (int i = 0; i < particles.size(); i++) {
            for (int j = i + 1; j < particles.size(); j++) {
                Point p1 = particles.get(i);
                Point p2 = particles.get(j);
                if (((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)) < (p1.radius + p2.radius) * (p1.radius + p2.radius)) {
                    Link link = new Link(p1, p2);
                    collisions.add(link);
                }
            }
        }
        
        for (Link l : collisions) {

            Point p1 = l.p1;
            Point p2 = l.p2;

            if (l.p1.fixed) {
                p1 = l.p2;
                p2 = l.p1;
            }  // if a particle is stationary, make it P2

// M = unit vector from P1 to P2
// T = unit tangential vector = M*i
            double xm = p2.x - p1.x;
            double ym = p2.y - p1.y;
            double rm = Math.sqrt(xm * xm + ym * ym);
            double xm1 = xm / rm;
            double ym1 = ym / rm;
            double xt1 = -ym1;
            double yt1 = xm1;

// look at the collision as an observer moving with speed v2.
// Now P1 collides with P2 which is at rest
            double wx = p1.vx - p2.vx;
            double wy = p1.vy - p2.vy;

            double wt = wx * xt1 + wy * yt1; //  speed of P1 in direction of T
            double wm = wx * xm1 + wy * ym1; //  speed of P1 in direction of M

            if (wm > 0) { // if the particle is moving away, the collision has already happened
                // Perpendicular speeds are calculated independent from tangential speed
                // a1 / a2 = how much of the perpendicular speed of P1 is  kept / transferred to P2 
                double a1 = (p1.mass - p2.mass) / (p1.mass + p2.mass); // ratio speed of P1 after/before collision
                double a2 = 2 * p1.mass / (p1.mass + p2.mass);   //ratio of speed of P2 after collision / P1 before collision

                if (p2.fixed) {
                    a1 = -1;
                    a2 = 0;
                }

                p1.vx = wt * xt1 + a1 * wm * xm1 + p2.vx;
                p1.vy = wt * yt1 + a1 * wm * ym1 + p2.vy;
                p2.vx = a2 * wm * xm1 + p2.vx;
                p2.vy = a2 * wm * ym1 + p2.vy;
            }
        }
    }

 //   int steps = 0;
    public boolean step(double dt, int resolution) {
        // This method calculates the new position of all particles after time step dt.
        // If no points are added to any trajectory, return false, true otherwise.
        // This to avoid redrawing the screen when nothing changed
        boolean redraw = false;
        double potentialEnergy = 0;
        double kineticEnergy = 0;

        // CoG, potential and kinetic energy are calculated at the beginning of this step
        for (Point p : particles) {
            kineticEnergy = kineticEnergy + 0.5 * p.mass * (p.vx * p.vx + p.vy * p.vy);
            p.vxnew = p.vx;
            p.vynew = p.vy;
        }

        for (Link link : links) {
            Point p1 = link.p1;
            Point p2 = link.p2;
            // System.out.printf("points %s and %s\n", p1.particleName, p2.particleName);
            double mass1 = p1.mass;
            double mass2 = p2.mass;
            double rsquare = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
            double r = Math.sqrt(rsquare);
            double r0 = link.r0;

            double k;
            if (r > r0) {
                k = kStretch;
            } else {
                k = kCompress;
            }

            Double force = k * (r - r0);

            potentialEnergy = potentialEnergy + 0.5 * k * (r - r0) * (r - r0);

            double ux = (p2.x - p1.x) / r;  //unit vector from p1 to p2
            double uy = (p2.y - p1.y) / r;

            p1.vxnew = p1.vxnew + (ux * force / mass1) * dt;
            p1.vynew = p1.vynew + (uy * force / mass1) * dt;
            p2.vxnew = p2.vxnew - (ux * force / mass2) * dt;
            p2.vynew = p2.vynew - (uy * force / mass2) * dt;
        }

        if (gravity) {
            for (Point p : particles) {
                p.vynew = p.vynew - 9.8 * dt;
                potentialEnergy = potentialEnergy + p.mass * p.y * 9.8;
            }
        }

        if (viscosity) {
            for (Point p : particles) {
                p.vynew = p.vynew - v * p.vynew * dt;
                p.vxnew = p.vxnew - v * p.vxnew * dt;
            }
        }

        // new position and speed of all particles
        Transform t = MovingParticles.transform;

        for (Point p : particles) {
            if (!p.fixed) {
                p.x = p.x + ((p.vx + p.vxnew) / 2) * dt;
                p.y = p.y + ((p.vy + p.vynew) / 2) * dt;
                p.vx = p.vxnew;
                p.vy = p.vynew;
            } else {
                p.vx = 0;
                p.vy = 0;
            }
        }

        // calculate new speed after collision
        
        collidingParticles(particles);
        
        // bounce against the window walls
        
        for (Point p : particles) {
            double uxmax = t.xScreenToUser((int) t.sxmax_real) - p.radius;
            double uxmin = t.xScreenToUser((int) t.sxmin_real) + p.radius;
            double uymax = t.yScreenToUser((int) t.symax_real) - p.radius;
            double uymin = t.yScreenToUser((int) t.symin_real) + p.radius;

            if (p.x > uxmax) {
                p.x = uxmax - (p.x - uxmax);
                p.vx = -p.vx;
            }
            if (p.x < uxmin) {
                p.x = uxmin + (uxmin - p.x);
                p.vx = -p.vx;
            }
            if (p.y > uymax) {
                p.y = uymax - (p.y - uymax);
                p.vy = -p.vy;
            }
            if (p.y < uymin) {
                p.y = uymin + (uymin - p.y);
                p.vy = -p.vy;
            }
        }

        for (Point p : particles) {
            if (!p.fixed) {
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
        }

        MovingParticles.Drawing.setString(
                0, String.format("K=%f", kineticEnergy));
        MovingParticles.Drawing.setString(
                1, String.format("P=%f", potentialEnergy));
        MovingParticles.Drawing.setString(
                2, String.format("E=%f", kineticEnergy + potentialEnergy));
        //        return redraw;
        return redraw;
    }

    public void cleanup() {
    }

}
