
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class Drawing {

    private java.util.List<Shape> shapes;

    double areaCursorX1, areaCursorY1, areaCursorX2, areaCursorY2;
    boolean areaCursorOn = false;

    public boolean dotsVisible = false;
    public boolean linesVisible = true;
    public boolean scaleVisible = true;
    public boolean snapToGrid = false;

    public double xmin, xmax, ymin, ymax; // calculated by method setMinMAx()

    int complexPointCounter; // used to construct names z1, z2, ...
    int shapeCounter;  // used to construct names shape1, shape2,...

    public Drawing() {
        shapes = new ArrayList<>();
        shapes.clear();
        complexPointCounter = 1;
        shapeCounter = 1;
    }

    public synchronized void clear() {
        shapes.clear();
        complexPointCounter = 1;
        shapeCounter = 1;
    }

    public synchronized void clearSelection() {

        Iterator<Shape> itr = shapes.iterator();

        itr = shapes.iterator();
        while (itr.hasNext()) {
            Shape s = itr.next();
            if (s.isSelected) {
                itr.remove();
            }

        }
    }

    public synchronized void deleteShape(Shape s) {
        shapes.remove(s);
    }

    public synchronized void addPointToShape(Shape s, double x, double y) {
        s.addPoint(x, y);
    }

    public synchronized void clearShape(Shape s) {
        s.clear();
    }

    public synchronized ArrayList<Shape> getShapes() {
        // makes a copy of 'shapes' to be used when risk of concurrent modification
        ArrayList<Shape> l = new ArrayList<>();
        for (Shape s : shapes) {
            l.add(s);
        }
        return l;
    }

    public synchronized void areaCursor(boolean cursorOn, double ax1, double ay1, double ax2, double ay2) {
        areaCursorOn = cursorOn;
        areaCursorX1 = ax1;
        areaCursorY1 = ay1;
        areaCursorX2 = ax2;
        areaCursorY2 = ay2;
    }

    public synchronized void draw(Transform t) {

        BasicStroke stroke0 = new BasicStroke();     // default dunne lijn
        BasicStroke stroke2 = new BasicStroke(2);    // dikkere lijn voor x en y as
        Graphics2D g2 = (Graphics2D) t.graphics;

        if (snapToGrid) {
            t.gridLines();
        }

        t.axes();

        if (areaCursorOn) {
            g2.setColor(Color.gray);
            t.line(areaCursorX1, areaCursorY1, areaCursorX2, areaCursorY1);
            t.line(areaCursorX2, areaCursorY1, areaCursorX2, areaCursorY2);
            t.line(areaCursorX2, areaCursorY2, areaCursorX1, areaCursorY2);
            t.line(areaCursorX1, areaCursorY2, areaCursorX1, areaCursorY1);
        }

        Point xyprev;
        for (Shape s : shapes) {
            if ((s.isSelected || s.isPreSelected) && !s.isPreUnselected) {
                t.graphics.setColor(Color.RED);
                g2.setStroke(stroke2);
            } else {
                t.graphics.setColor(s.color);
                g2.setStroke(stroke0);
            };

            if (s.getClass().getName().equals("PointShape")) {

                PointShape ps = (PointShape) s;
                t.complexPoint(ps.getName(), ps.getX(), ps.getY());

            } else {   // not a point shape

                if (linesVisible) {
                    xyprev = null;
                    for (Point xy : s.points) {
                        if (xyprev != null) {
                            t.line(xyprev.getZX(), xyprev.getZY(), xy.getZX(), xy.getZY());
                        }
                        xyprev = xy;
                    }
                }

                if (dotsVisible) {
                    for (Point xy : s.points) {
                        t.dot(xy.getZX(), xy.getZY());
                    }
                }
            }
        }
    }

    public synchronized void setMinMax() {

        xmax = Double.NEGATIVE_INFINITY;
        xmin = Double.POSITIVE_INFINITY;
        ymax = Double.NEGATIVE_INFINITY;
        ymin = Double.POSITIVE_INFINITY;

        for (Shape s : shapes) {

            for (Point xy : s.points) {
                if (xy.getZX() < xmin) {
                    xmin = xy.getZX();
                }
                if (xy.getZX() > xmax) {
                    xmax = xy.getZX();
                }
                if (xy.getZY() < ymin) {
                    ymin = xy.getZY();
                }
                if (xy.getZY() > ymax) {
                    ymax = xy.getZY();
                }

            };

        }
    }

    public synchronized PointShape closestPointShape(double x, double y) {
        PointShape psmin = null;
        double dmin = Double.POSITIVE_INFINITY;
        for (Shape s : shapes) {

            if (s.getClass().getName().equals("PointShape")) {
                PointShape ps = (PointShape) s;
                double x1 = ps.getX();
                double y1 = ps.getY();
                if ((x - x1) * (x - x1) + (y - y1) * (y - y1) < dmin) {
                    dmin = (x - x1) * (x - x1) + (y - y1) * (y - y1);
                    psmin = ps;
                };
            }

        }; // for
        return psmin;
    }

    public synchronized Shape closestShape(double x, double y) {
        Shape smin = null;
        double dmin = Double.POSITIVE_INFINITY;
        for (Shape s : shapes) {

            if (s.getClass().getName().equals("Shape")) {

                for (Point xy : s.points) {
                    double x1 = xy.getZX();
                    double y1 = xy.getZY();
                    if ((x - x1) * (x - x1) + (y - y1) * (y - y1) < dmin) {
                        dmin = (x - x1) * (x - x1) + (y - y1) * (y - y1);
                        smin = s;
                    };
                };

            }

        }; // for
        return smin;
    }

    public synchronized Shape addShape() {
        Shape s = new Shape();
        s.label = "shape" + shapeCounter;
        shapeCounter++;
        shapes.add(s);
        return s;
    }

    public synchronized PointShape addPointShape() {
        PointShape s = new PointShape("z" + complexPointCounter);
        complexPointCounter++;
        shapes.add(s);
        return s;
    }

    public synchronized void moveShapeRelative(Shape s, double dx, double dy) {
        for (Point xy : s.points) {
            xy.replaceXY(xy.getZX() + dx, xy.getZY() + dy);
        };
    }  // moveRelativeShape

    public synchronized void moveShapesRelative(String l, double dx, double dy) {

// move the shape or shapes with label l 
// if l == "all" then move everything except parameter points
// if l == "selection" then move only selected shapes
        for (Shape s : shapes) {
            if ((l.equals("all"))
                    || (l.equals("selection") && s.isSelected)
                    || (s.label == l)) {
                for (Point xy : s.points) {
                    xy.replaceXY(xy.getZX() + dx, xy.getZY() + dy);
                };
            };
        }
    }  // moveShapesRelative

    public synchronized int selectShapes(double x, double y, double minPixelDist) {
// find points close to x,y and return their number 
// It is also the number of shapes since very point belongs to exactly one shape
// Select the shapes

        int nrs;
        double minPixelDistSquare = minPixelDist * minPixelDist;

        nrs = 0;
        for (Shape s : shapes) {
            for (Point xy : s.points) {
                if (((xy.x - x) * (xy.x - x) + (xy.y - y) * (xy.y - y)) < minPixelDistSquare) {
                    nrs = nrs + 1;
                    s.isSelected = true;
                };
            };
        };

        if (nrs == 0) {
            System.out.println("No shapes selected");
            return 0;
        } else {
            System.out.println(nrs + " shapes selected ");
            return nrs;
        }

    }

    public synchronized int unselectShapes(double x, double y, double minPixelDist) {
// find points close to x,y and return their number 
// It is also the number of shapes since very point belongs to exactly one shape
// Unselect the shapes

        int nrs;
        double minPixelDistSquare = minPixelDist * minPixelDist;

        nrs = 0;
        for (Shape s : shapes) {
            for (Point xy : s.points) {
                if (((xy.x - x) * (xy.x - x) + (xy.y - y) * (xy.y - y)) < minPixelDistSquare) {
                    nrs = nrs + 1;
                    s.isSelected = false;
                }
            }
        }

        if (nrs == 0) {
            System.out.println("No shapes unselected");
            return 0;
        } else {
            System.out.println(nrs + " shapes unselected ");
            return nrs;
        }

    }

    public synchronized void selectAll() {
        for (Shape s : shapes) {
            s.isSelected = true;
        }
    }

    public synchronized void unselectAll() {
        for (Shape s : shapes) {
            s.isSelected = false;
        }
    }

    public synchronized void selectArea() {
// preSelect all shapes that have a point in the cursor area
// preSelection is reset each time this method is called due to cursor movement

        for (Shape s : shapes) {
            s.isPreSelected = false;
            for (Point xy : s.points) {
                if ((xy.x >= areaCursorX1) && (xy.x <= areaCursorX2)
                        && (xy.y >= areaCursorY1) && (xy.y <= areaCursorY2)) {
                    s.isPreSelected = true;
                }
            }
        }
    }

    public synchronized void unselectArea() {
// preUnselect all shapes that have a point in the cursor area
// preUnselection is reset each time this method is called due to cursor movement

        for (Shape s : shapes) {
            s.isPreUnselected = false;
            for (Point xy : s.points) {
                if ((xy.x >= areaCursorX1) && (xy.x <= areaCursorX2)
                        && (xy.y >= areaCursorY1) && (xy.y <= areaCursorY2)) {
                    s.isPreUnselected = true;
                }
            }
        }
    }

    public synchronized void commitSelectedArea() {
// Select all shapes that were preselected. Called when area selection is final

        for (Shape s : shapes) {
            if (s.isPreSelected) {
                s.isPreSelected = false;
                s.isSelected = true;
            }
        }
    }

    public synchronized void commitUnselectedArea() {
// Select all shapes that were preselected. Called when area selection is final

        for (Shape s : shapes) {
            if (s.isPreUnselected) {
                s.isPreUnselected = false;
                s.isSelected = false;
            }
        }
    }

    class Gravity extends JFrame implements ActionListener {

        JPanel pointPane;
        ArrayList<Point> particles = new ArrayList<>();

        public Gravity() {

            for (Shape s : MovingParticles.Drawing.getShapes()) {
                if (s.isPoint) {
                    for (Point p : s.points) {
                        particles.add(p);
                        p.particleName = s.label;
                        p.x_init = p.x;
                        p.y_init = p.y;
                        p.trajectory = MovingParticles.Drawing.addShape();
                        MovingParticles.Drawing.addPointToShape(p.trajectory, p.x, p.y);
                    }
                }
            }

            Container pane = getContentPane();
            pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
            pane.add(Box.createRigidArea(new Dimension(500, 20)));

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
            pack();
            setVisible(true);

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
                p.trajectory.clear();
                MovingParticles.Drawing.addPointToShape(p.trajectory, p.x, p.y);
            }
            MovingParticles.zPlane.blitPaint();

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
                            p.xspeed = p.velocity * Math.cos((p.angle / 180) * Math.PI);
                            p.yspeed = p.velocity * Math.sin((p.angle / 180) * Math.PI);
                        }
                    }
                }
            }

        }
    }

    class Elasticity extends JFrame implements ActionListener {

        JPanel pointPane;
        ArrayList<Point> particles = new ArrayList<>();
        ArrayList<Point> extremities = new ArrayList<>();

        public Elasticity() {

            for (Shape s : MovingParticles.Drawing.getShapes()) {

                s.color = Color.MAGENTA;
                s.isSelected = true;

                // populate 'particles'
                int c = 1;
                for (Point p : s.points) {
                    particles.add(p);
                    p.mass = 10.0;
                    p.velocity = 0;
                    p.angle = 0;
                    p.particleName = s.label + "-" + c;
                    c++;
                    p.x_init = p.x;
                    p.y_init = p.y;
                    p.xLastDrawn=p.x;
                    p.yLastDrawn=p.y;
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

            Container pane = getContentPane();
            pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
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

        private void addTextField(int i, String label, double value) {
            pointPane.add(Box.createRigidArea(new Dimension(20, 0)));
            pointPane.add(new JLabel(label));
            pointPane.add(Box.createRigidArea(new Dimension(20, 0)));
            JTextField field = new JTextField(String.format("%.2f", value));
            field.addActionListener(this);
            field.setActionCommand(i + "|" + label);
            pointPane.add(field);
        }

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
                p.trajectory.clear();
                MovingParticles.Drawing.addPointToShape(p.trajectory, p.x, p.y);
            }
            MovingParticles.zPlane.blitPaint();

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
                            System.out.println(p.particleName + ".xspeed=" + p.xspeed);
                            System.out.println(p.particleName + ".yspeed=" + p.yspeed);

                        }
                    }

                    System.out.println("==================");
                    for (Point p : particles) {
                        System.out.println(p.particleName + " mass " + p.mass + " xspeed " + p.xspeed + " yspeed " + p.yspeed);
                    }
                }
            }

        }
    }

    class Rotation {
    }

    Gravity gravity = null;
    Elasticity elasticity = null;
    Rotation rotation = null;

    public synchronized boolean rotate(double alfa) {
        double cos = Math.cos(alfa);
        double sin = Math.sin(alfa);

        for (Shape s : MovingParticles.Drawing.shapes) {
            for (Point p : s.points) {
                double xnew = p.x * cos - p.y * sin;
                double ynew = p.y * cos + p.x * sin;
                p.x = xnew;
                p.y = ynew;
            }
        }
        return true; // redraw
    }

    public synchronized boolean gravitate(double dt) {
        // This method calculates the new position of all particles after time step dt.
        // If no points are added to any trajectory, return false, true otherwise.
        // This to avoid redrawing the screen when nothing changed
        boolean redraw = false;
        ArrayList<Point> particles = gravity.particles;
        // new position of all particles
        for (Point p : particles) {
            p.xnew = p.x + p.xspeed * dt;
            p.ynew = p.y + p.yspeed * dt;
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
                //                force = r;
                double ux = (p2.x - p1.x) / r;  //unit vector from p1 to p2
                double uy = (p2.y - p1.y) / r;

                double vx = ux * force * dt;
                double vy = uy * force * dt;

                /*               
                 Shape vector=MovingParticles.Drawing.addShape();
                 vector.addPoint(p2.x, p2.y);
                 vector.addPoint(p2.x-vx, p2.y-vy);
                 vector.color=Color.BLUE;
                 */
                p1.xspeed = p1.xspeed + (ux * force / mass1) * dt;
                p1.yspeed = p1.yspeed + (uy * force / mass1) * dt;
                p2.xspeed = p2.xspeed - (ux * force / mass2) * dt;
                p2.yspeed = p2.yspeed - (uy * force / mass2) * dt;
            }
        }

        for (Point p : particles) {
            p.x = p.xnew;
            p.y = p.ynew;

            Point lastDrawnPoint = p.trajectory.lastPoint();
            double x1, x2, y1, y2;
            x1 = MovingParticles.transform.xUserToScreen(p.x);
            y1 = MovingParticles.transform.yUserToScreen(p.y);
            x2 = MovingParticles.transform.xUserToScreen(lastDrawnPoint.x);
            y2 = MovingParticles.transform.yUserToScreen(lastDrawnPoint.y);
            double sqScreenDistance = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
            if (sqScreenDistance > 100) {
                p.trajectory.addPoint(p.x, p.y);
                redraw = true;
            }

        }
        return redraw;
    }

    public synchronized boolean elastic(double dt) {
        // This method calculates the new position of all particles after time step dt.
        // If no points are added to any trajectory, return false, true otherwise.
        // This to avoid redrawing the screen when nothing changed
        boolean redraw = false;
        ArrayList<Point> particles = elasticity.particles;
 //       System.out.println("elastic moving particles " + particles.size());

        // new position of all particles
        for (Point p : particles) {
            p.xnew = p.x + p.xspeed * dt;
            p.ynew = p.y + p.yspeed * dt;
        }

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

                double force = 10 * (r - r0);
                //            System.out.println(p1.particleName + " " + p2.particleName + "  force " + force);

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
                p.xLastDrawn=p.x;
                p.yLastDrawn=p.y;
            }
        }
        return redraw;
    }

    public synchronized void initializeAnimation(String animation) {

        if (animation.equals("gravitate")) {
            gravity = new Gravity();

        }
        if (animation.equals("elastic")) {
            /*
             Shape s = MovingParticles.Drawing.addShape();         
             s.addPoint(1, 1);
             s.addPoint(2, 1);
             s.addPoint(3, 1);
             s.addPoint(4, 1);
             s.addPoint(5, 1);
             s.color=Color.MAGENTA;
             s.isSelected=true;
             */
            elasticity = new Elasticity();

        }
    }

    public synchronized void resetAnimation(String animation) {

        if (animation.equals("gravitate")) {
            gravity.reset();
        }
        if (animation.equals("elastic")) {
            elasticity.reset();
        }
    }

}
