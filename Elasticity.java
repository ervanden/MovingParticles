
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class Elasticity extends JFrame implements Animation, ActionListener, ChangeListener {

    JPanel pointPane;
    ArrayList<Point> particles = new ArrayList<>();
    ArrayList<Point> extremities = new ArrayList<>();
    double k = 1;  // elasticity constant  F = k * delta(x)
    JSlider sliderK;
    JLabel kInfo;
    Shape centerOfGravity;

    public Elasticity() {

        for (Shape s : MovingParticles.Drawing.getShapes()) {

            s.color = Color.MAGENTA;
            s.isSelected = true;

            // populate 'particles'
            int c = 1;
            for (Point p : s.points) {
                particles.add(p);
                p.velocity = 0;
                p.angle = 0;
                p.particleName = s.label + "-" + c;
                c++;
                p.x_init = p.x;
                p.y_init = p.y;
                p.xLastDrawn = p.x;
                p.yLastDrawn = p.y;
                p.trajectory = null;
                /*
                 p.trajectory = MovingParticles.Drawing.addShape();
                 MovingParticles.Drawing.addPointToShape(p.trajectory, p.x, p.y);
                 p.trajectory.color=Color.ORANGE;
                 */
            }

            // populate extremities
            System.out.println("init shape " + s.label);

            Point pe;
            pe = s.points.get(0);
            extremities.add(pe);
            pe.particleName = s.label + "-begin";
            pe = s.points.get(s.points.size() - 1);
            extremities.add(pe);
            pe.particleName = s.label + "-end";

            // populate 'neighbours' of all particles
            // assuming there is only 1 shape, all the particles belong to this shape
            for (int i = 0; i < particles.size(); i++) {
                if (i > 0) {
                    Point p = particles.get(i);
                    p.neighbours.add(particles.get(i - 1));
                }
                if (i < particles.size() - 1) {
                    particles.get(i).neighbours.add(particles.get(i + 1));
                }
                System.out.println("particle " + particles.get(i).particleName + " has " + particles.get(i).neighbours.size() + " neighbours");
            }
        }

        centerOfGravity = MovingParticles.Drawing.addPointShape();
        MovingParticles.Drawing.addPointToShape(centerOfGravity, 0, 0);
        centerOfGravity.color = Color.BLUE;
        centerOfGravity.label="CoG";

        Container pane = getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        kInfo = new JLabel("elasticity constant", JLabel.CENTER);
        sliderK = new JSlider(-2000, 2000, 0);
        sliderK.addChangeListener(this);

        pane.add(Box.createRigidArea(new Dimension(500, 20)));
        pane.add(kInfo);
        pane.add(sliderK);
        pane.add(Box.createRigidArea(new Dimension(500, 20)));

        for (int i = 0; i < extremities.size(); i++) {
            Point p = extremities.get(i);
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
        pack();
        setVisible(true);

    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(sliderK)) {
            k = Math.pow(10.0, (double) sliderK.getValue() / 1000);
            String kString = String.format("%f", k);
            kInfo.setText("elasticity constant = " + kString);
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
                String pName = extremities.get(particleNr).particleName;
                field.setBackground(Color.white);

                if (attribute.equals("Mass")) {
                    extremities.get(particleNr).mass = value;
                    System.out.println(pName + "." + attribute + "=" + value);

                    field.setBackground(Color.green);
                }
                if (attribute.equals("Velocity")) {
                    extremities.get(particleNr).velocity = value;
                    System.out.println(pName + "." + attribute + "=" + value);
                    field.setBackground(Color.green);
                }
                if (attribute.equals("Angle")) {
                    extremities.get(particleNr).angle = value;
                    System.out.println(pName + "." + attribute + "=" + value);
                    field.setBackground(Color.green);
                }
                if (attribute.equals("Velocity") || attribute.equals("Angle")) {
                    for (Point p : particles) {
                        p.xspeed = p.velocity * Math.cos((p.angle / 180) * Math.PI);
                        p.yspeed = p.velocity * Math.sin((p.angle / 180) * Math.PI);
 //                       System.out.println(p.particleName + ".xspeed=" + p.xspeed);
                        //                       System.out.println(p.particleName + ".yspeed=" + p.yspeed);

                    }
                }

//                System.out.println("==================");
//                for (Point p : particles) {
//                    System.out.println(p.particleName + " mass " + p.mass + " xspeed " + p.xspeed + " yspeed " + p.yspeed);
//                }
            }
        }

    }
    
    public void trajectory(boolean on){}

    public void reset() {

        for (Point p : particles) {
            // reset initial position
            p.x = p.x_init;
            p.y = p.y_init;
            // reset initial speed
            p.xspeed = p.velocity * Math.cos((p.angle / 180) * Math.PI);
            p.yspeed = p.velocity * Math.sin((p.angle / 180) * Math.PI);
        }
        // remove trajectories and start new ones

        for (Point p : particles) {
            if (p.trajectory != null) {
                p.trajectory.clear();
                MovingParticles.Drawing.addPointToShape(p.trajectory, p.x, p.y);
            }
        }
        MovingParticles.zPlane.blitPaint();

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
                    p.xspeed,
                    p.yspeed
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

    public boolean step(double dt) {
        // This method calculates the new position of all particles after time step dt.
        // If no points are added to any trajectory, return false, true otherwise.
        // This to avoid redrawing the screen when nothing changed
        boolean redraw = false;
        double xCenterOfGravity = 0;
        double yCenterOfGravity = 0;
        double totalMass=0;

        if (steps == 0) {
            System.out.println("\n\nSTEP 0");
            dump_state(null, null);
            System.out.println();
        };
        steps++;

        // new position of all particles
        for (Point p : particles) {
            p.xnew = p.x + p.xspeed * dt;
            p.ynew = p.y + p.yspeed * dt;
            xCenterOfGravity = xCenterOfGravity + p.mass*p.xnew;
            yCenterOfGravity = yCenterOfGravity + p.mass*p.ynew;
            totalMass=totalMass+p.mass;
        }
        centerOfGravity.points.get(0).x = xCenterOfGravity / totalMass;
        centerOfGravity.points.get(0).y = yCenterOfGravity / totalMass;

        // new speed of all particles
        for (Point p1 : particles) {
            for (Point p2 : p1.neighbours) {
                //               System.out.printf("points %d and %d\n", i1, i2);
                double mass1 = p1.mass;
                double mass2 = p2.mass;
                double rsquare = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
                double r = Math.sqrt(rsquare);
                double r0square = (p1.x_init - p2.x_init) * (p1.x_init - p2.x_init)
                        + (p1.y_init - p2.y_init) * (p1.y_init - p2.y_init);
                double r0 = Math.sqrt(r0square);

                Double force = k * (r - r0);

                if (force.equals(Double.NaN)) {
                    System.out.printf("STEP=%d p1=%s p2=%s r=%5f r0=%5f force=%5f\n", steps, p1.particleName, p2.particleName, r, r0, force);
                    dump_state(p1, p2);

                };

                double ux = (p2.x - p1.x) / r;  //unit vector from p1 to p2
                double uy = (p2.y - p1.y) / r;

                p1.xspeed = p1.xspeed + (ux * force / mass1) * dt;
                p1.yspeed = p1.yspeed + (uy * force / mass1) * dt;
                p2.xspeed = p2.xspeed - (ux * force / mass2) * dt;
                p2.yspeed = p2.yspeed - (uy * force / mass2) * dt;
            }
        }
        //                   try{Thread.sleep(1000);}catch (Exception e){};

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
        return true; // return redraw;
    }

}
