
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

    double k1 = 1;  // elasticity constant  F = k * delta(x)
    double k2 = 1;
    double v = 0;  // viscosity
    boolean gravity = false;
    boolean viscosity = true;

    JSlider sliderK1;
    JSlider sliderK2;
    JSlider sliderViscosity;
    JLabel k1Info;
    JLabel k2Info;
    JLabel viscosityInfo;

    JCheckBox gBox;

    public Elasticity() {

        particles = MovingParticles.Drawing.getPoints();

        for (Point p : particles) {
            p.x_init = p.x;
            p.y_init = p.y;
            p.xLastDrawn = p.x;
            p.yLastDrawn = p.y;
            p.trajectory = null;
        }

        links = MovingParticles.Drawing.getLinks();

        pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        Border blackline = BorderFactory.createLineBorder(Color.black);
        pane.setBorder(blackline);
        gBox = new JCheckBox("gravity");
        gBox.addItemListener(this);
        gBox.setEnabled(true);

        k1Info = new JLabel("elasticity constant (stretch)", JLabel.CENTER);
        sliderK1 = new JSlider(-5000, 3000, 0);
        k2Info = new JLabel("elasticity constant (compress)", JLabel.CENTER);
        sliderK2 = new JSlider(-5000, 3000, 0);
        viscosityInfo = new JLabel("viscosity", JLabel.CENTER);
        sliderViscosity = new JSlider(-3000, 3000, 0);
        sliderK1.addChangeListener(this);
        sliderK2.addChangeListener(this);
        sliderViscosity.addChangeListener(this);

        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(gBox);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(k1Info);
        pane.add(sliderK1);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(k2Info);
        pane.add(sliderK2);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(viscosityInfo);
        pane.add(sliderViscosity);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(sliderK1)) {
            k1 = Math.pow(10.0, (double) sliderK1.getValue() / 1000);
            String kString = String.format("%f", k1);
            k1Info.setText("elasticity constant (stretch) = " + kString);
        }
        if (e.getSource().equals(sliderK2)) {
            k2 = Math.pow(10.0, (double) sliderK2.getValue() / 1000);
            String kString = String.format("%f", k2);
            k2Info.setText("elasticity constant (compress) = " + kString);
        }
        if (e.getSource().equals(sliderViscosity)) {
            v = Math.pow(10.0, (double) sliderViscosity.getValue() / 1000);
            String kString = String.format("%f", v);
            viscosityInfo.setText("viscosity = " + kString);
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
                    p.particleName,
                    p.mass,
                    p.x,
                    p.x_init,
                    p.y,
                    p.y_init,
                    p.vx,
                    p.vy
            );
        }
        if (p1 != null) {
            System.out.println("== p1 " + p1.particleName);
        }
        if (p2 != null) {
            System.out.println("== p2 " + p2.particleName);
        }
    }

    int steps = 0;

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
            double r0square = (p1.x_init - p2.x_init) * (p1.x_init - p2.x_init)
                    + (p1.y_init - p2.y_init) * (p1.y_init - p2.y_init);
            double r0 = Math.sqrt(r0square);

            double k;
            if (r > r0) {
                k = k1;
            } else {
                k = k2;
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
            }
        }

        for (Point p1 : particles) {
            p1.color = Color.BLACK;
        }
        for (Point p1 : particles) {
            for (Point p2 : particles) {
                if (p1 != p2) {
                    if (((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)) < (p1.radius + p2.radius) * (p1.radius + p2.radius)) {
                        p1.color = Color.ORANGE;
                        p2.color = Color.ORANGE;

                    double xm = p2.x - p1.x;
                    double ym = p2.y - p1.y;
                    double xt = ym;
                    double yt = -xm;
                    double rt = Math.sqrt(xt * xt + yt * yt);
                    double xt1 = xt / rt;
                    double yt1 = yt / rt;
                    if (p1.tangent == null) {
                        p1.tangent = MovingParticles.Drawing.addCurve();
                        MovingParticles.Drawing.addPointToCurve(p1.tangent, 0, 0);
                        MovingParticles.Drawing.addPointToCurve(p1.tangent, 1,1);
                    }
                    p1.tangent.points.get(0).x = p1.x;
                    p1.tangent.points.get(0).y = p1.y;
                    p1.tangent.points.get(1).x = p1.x + xt1;
                    p1.tangent.points.get(1).y = p1.y + yt1;
                    
                    //  xt1 + i yt1   = tangent
                    //  (vrx +i vry) = (vx + i vy)*(xt1 - i yt1) = speed relative to tangent
                    //               = (vx*xt1+vy*yt1)  + i (vy*xt1-vx*yt1)
                    //  (vrx - i vry) = bounced
                    //  (vrx - i vry)*(xt1 + i yt1) = absolute speed after bounce
                    //         = (vrx*xt1+vry*yt1) +i(vrx*yt1-vry*xt1)
                    
                    double vrx=p1.vx*xt1+p1.vy*yt1;
                    double vry=p1.vy*xt1-p1.vx*yt1;
                    p1.vx=vrx*xt1+vry*yt1;
                    p1.vy=vrx*yt1-vry*xt1;
                    
                    } 
                    
                }
            }
        }


    // bounce 
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

    for (Point p : particles

    
        ) {
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

    MovingParticles.Drawing.setString (

    0, String.format("K=%f", kineticEnergy));
    MovingParticles.Drawing.setString (

    1, String.format("P=%f", potentialEnergy));
    MovingParticles.Drawing.setString (
    2, String.format("T=%f", kineticEnergy + potentialEnergy));
        //        return redraw;
    return redraw ;
}

public void cleanup() {
    }

}
