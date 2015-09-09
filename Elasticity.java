
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
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class Elasticity implements Animation, ActionListener, ChangeListener {

    Shape shape;
    double kineticEnergy;
    double potentialEnergy;

    JPanel pointPane;
    JPanel pane;

    ArrayList<Point> particles = new ArrayList<>();
    ArrayList<Point> extremities = new ArrayList<>();
    ArrayList<Link> links = new ArrayList<>();

    double k = 1;  // elasticity constant  F = k * delta(x)
    JSlider sliderK;
    JLabel kInfo;
    Shape centerOfGravity;

    class Link {

        public Point p1, p2;

        public Link(Point p_1, Point p_2) {
            p1 = p_1;
            p2 = p_2;
        }
    }

    public Elasticity() {

        // create a new shape with only particles that are sufficiently apart 
        ArrayList<Point> newPoints = new ArrayList<>();
        for (Shape s : MovingParticles.Drawing.getShapes()) {
            boolean first = true;
            double xprev = 0;
            double yprev = 0;
            for (Point p : s.points) {
                if (first || (((p.x - xprev) * (p.x - xprev) + (p.y - yprev) * (p.y - yprev)) > 10e-6)) {
                    newPoints.add(new Point(p.x, p.y));
                    xprev = p.x;
                    yprev = p.y;
                    first = false;
                }
            }
        }
        MovingParticles.Drawing.clear();

        if (newPoints.size() <= 1) {
            System.out.println("All points too close. Shape is removed");
            shape = null;
            return;
        } else {
            shape = MovingParticles.Drawing.addShape();
            for (Point p : newPoints) {
                MovingParticles.Drawing.addPointToShape(shape, p.x, p.y);
            }
            System.out.printf("\nShape %s with %d points\n\n", shape.label, shape.points.size());
        }

        shape.isSelected = true;

        // populate 'particles'
        int c = 1;
        for (Point p : shape.points) {
            particles.add(p);
            p.velocity = 0;
            p.angle = 0;
            p.particleName = shape.label + "-" + c;
            c++;
            p.x_init = p.x;
            p.y_init = p.y;
            p.xLastDrawn = p.x;
            p.yLastDrawn = p.y;
            p.trajectory = null;
        }

        // populate extremities

        Point pe;
        pe = shape.points.get(0);
        extremities.add(pe);
        pe.particleName = shape.label + "-begin";
        pe = shape.points.get(shape.points.size() - 1);
        extremities.add(pe);
        pe.particleName = shape.label + "-end";

        // populate 'neighbours' of all particles
        // assuming there is only 1 shape, all the particles belong to this shape
        for (int i = 0; i < particles.size(); i++) {
            
             Point p = particles.get(i);
             
            if (i < particles.size() - 1) {
                Point pn = particles.get(i + 1);
                links.add(new Link(p, pn));
//                System.out.printf("added link %s - %s\n",p.particleName ,pn.particleName);
            }
        }

        centerOfGravity = MovingParticles.Drawing.addPointShape();
        MovingParticles.Drawing.addPointToShape(centerOfGravity, 0, 0);
        centerOfGravity.color = Color.BLUE;
        centerOfGravity.label = "CoG";

        pane = new JPanel();
        pane.add(Box.createRigidArea(new Dimension(500, 20)));
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
                        p.vx = p.velocity * Math.cos((p.angle / 180) * Math.PI);
                        p.vy = p.velocity * Math.sin((p.angle / 180) * Math.PI);
                    }
                }
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

    public boolean step(double dt) {
        // This method calculates the new position of all particles after time step dt.
        // If no points are added to any trajectory, return false, true otherwise.
        // This to avoid redrawing the screen when nothing changed
        boolean redraw = false;
        double xCenterOfGravity = 0;
        double yCenterOfGravity = 0;
        double totalMass = 0;

        // CoG, potential and kinetic energy with positions and speeds at the beginning of this step
        // new position of all particles
        for (Point p : particles) {
            p.xnew = p.x + p.vx * dt;
            p.ynew = p.y + p.vy * dt;
            xCenterOfGravity = xCenterOfGravity + p.mass * p.x;
            yCenterOfGravity = yCenterOfGravity + p.mass * p.y;
            totalMass = totalMass + p.mass;
        }
        centerOfGravity.points.get(0).x = xCenterOfGravity / totalMass;
        centerOfGravity.points.get(0).y = yCenterOfGravity / totalMass;

        // new speed of all particles
        for (Point p1 : particles) {
            kineticEnergy = kineticEnergy + 0.5 * p1.mass * (p1.vx * p1.vx + p1.vy * p1.vy);  
        }
        

        for (Link link : links) {
            Point p1=link.p1;
            Point p2=link.p2;
 //               System.out.printf("points %s and %s\n", p1.particleName, p2.particleName);
                double mass1 = p1.mass;
                double mass2 = p2.mass;
                double rsquare = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
                double r = Math.sqrt(rsquare);
                double r0square = (p1.x_init - p2.x_init) * (p1.x_init - p2.x_init)
                        + (p1.y_init - p2.y_init) * (p1.y_init - p2.y_init);
                double r0 = Math.sqrt(r0square);

                Double force = k * (r - r0);

                potentialEnergy = potentialEnergy + 0.5 * k * (r - r0) * (r - r0);

                double ux = (p2.x - p1.x) / r;  //unit vector from p1 to p2
                double uy = (p2.y - p1.y) / r;

                p1.vx = p1.vx + (ux * force / mass1) * dt;
                p1.vy = p1.vy + (uy * force / mass1) * dt;
                p2.vx = p2.vx - (ux * force / mass2) * dt;
                p2.vy = p2.vy - (uy * force / mass2) * dt;
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

        return true; // return redraw;
    }

    public void cleanup() {
        if (centerOfGravity != null) {
            MovingParticles.Drawing.deleteShape(centerOfGravity);
        }
    }

}
