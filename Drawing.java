
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Drawing {

    private List<Shape> shapes;
    private List<Link> links;
    private List<Point> points;
    private ArrayList<String> strings;

    int stringsMaxLength = 0;

    double areaCursorX1, areaCursorY1, areaCursorX2, areaCursorY2;
    boolean areaCursorOn = false;

    public boolean labelsVisible = false;
    public boolean linesVisible = true;
    public boolean scaleVisible = true;
    public boolean snapToGrid = false;

    public double xmin, xmax, ymin, ymax; // calculated by method setMinMAx()

    int pointCounter; // used to construct names z1, z2, ...
    int shapeCounter;  // used to construct names shape1, shape2,...

    public Drawing() {
        shapes = new ArrayList<>();
        links = new ArrayList<>();
        points = new ArrayList<>();
        strings = new ArrayList<>();
        shapes.clear();
        strings.clear();
        pointCounter = 1;
        shapeCounter = 1;
    }

    public synchronized void deleteDrawing() {
        shapes.clear();
        links.clear();
        points.clear();
        strings.clear();
        pointCounter = 1;
        shapeCounter = 1;
    }

    public synchronized void deleteSelectedPoints() {
        Iterator<Link> itr = links.iterator();

        itr = links.iterator();
        while (itr.hasNext()) {
            Link l = itr.next();
            if (l.p1.isSelected || l.p2.isSelected) {
                itr.remove();
            }
        }

        Iterator<Point> pitr = points.iterator();
        pitr = points.iterator();
        while (pitr.hasNext()) {
            Point p = pitr.next();
            System.out.println("iterating  " + p.particleName);
            if (p.isSelected) {
                System.out.println("deleting " + p.particleName);
                pitr.remove();
            }
        }

    }

    public synchronized void deleteSelectedLinks() {
        Iterator<Link> itr = links.iterator();

        itr = links.iterator();
        while (itr.hasNext()) {
            Link l = itr.next();
            if (l.isSelected) {
                itr.remove();
            }
        }
    }

    public void setString(int position, String s) {
        if (position > strings.size() - 1) {
            strings.add(s);
        }
        strings.set(position, s);
        if (s.length() > stringsMaxLength) {
            stringsMaxLength = s.length();
        }
    }

    public synchronized void deleteShape(Shape s) {
        shapes.remove(s);
    }

    public synchronized void deleteLink(Link l) {
        links.remove(l);
    }

    public synchronized void deletePoint(Point p) {
        Iterator<Link> itr = links.iterator();

        itr = links.iterator();
        while (itr.hasNext()) {
            Link l = itr.next();
            if ((l.p1 == p) || (l.p2 == p)) {
                itr.remove();
            }

        }
        points.remove(p);
    }

    public synchronized void addPointToShape(Shape s, double x, double y) {
        if (s.points.size() == 0) {
            s.addPoint(x, y);
            points.add(s.lastPoint());
        } else {
            Point pprev = s.lastPoint();
            double xprev = pprev.x;
            double yprev = pprev.y;
            if (((x - xprev) * (x - xprev) + (y - yprev) * (y - yprev)) > 10e-6) {
                s.addPoint(x, y);
                points.add(s.lastPoint());
                links.add(new Link(s.lastPoint(), pprev));
            } else {
                System.out.println("Point too close to previous in shape " + s.label + " : not added");
            }
        }
    }

    public synchronized void clearShape(Shape s) {
        for (Point p : s.points) {
            deletePoint(p);
        }
        s.clear();
    }

    public synchronized ArrayList<Point> getPoints() {
        // makes a copy of 'shapes' to be used when risk of concurrent modification
        ArrayList<Point> list = new ArrayList<>();
        for (Point p : points) {
            list.add(p);
        }
        return list;
    }
    
        public synchronized ArrayList<Point> getSelectedPoints() {
        // makes a copy of 'shapes' to be used when risk of concurrent modification
        ArrayList<Point> list = new ArrayList<>();
        for (Point p : points) {
            if (p.isSelected) list.add(p);
        }
        return list;
    }
    
        public synchronized ArrayList<Link> getLinks() {
        // makes a copy of 'shapes' to be used when risk of concurrent modification
        ArrayList<Link> list = new ArrayList<>();
        for (Link l : links) {
            list.add(l);
        }
        return list;
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

        t.strings(strings);

        for (Point p : points) {
            if ((p.isSelected || p.isPreSelected) && !p.isPreUnselected) {
                t.graphics.setColor(Color.RED);
                //                   g2.setStroke(stroke2);
            } else {
                t.graphics.setColor(Color.BLACK);
                //                   g2.setStroke(stroke0);
            };
            t.circle(p.x, p.y, p.radius);
            if (labelsVisible) {
                t.label(p.particleName, p.x, p.y);
            }
            if (p.fixed) {
                t.fix(p.x, p.y);
            }
        }

        if (linesVisible) {
            for (Link l : links) {
                if ((l.isSelected || l.isPreSelected) && !l.isPreUnselected) {
                    t.graphics.setColor(Color.RED);
                    //                   g2.setStroke(stroke2);
                } else {
                    t.graphics.setColor(Color.BLACK);
                    //                   g2.setStroke(stroke0);
                };
                t.line(l.p1.x, l.p1.y, l.p2.x, l.p2.y);
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

    public synchronized Point locatePoint(double x, double y) {
        Point psmin = null;
        for (Point p : points) {
            double x1 = p.x;
            double y1 = p.y;
            if ((x - x1) * (x - x1) + (y - y1) * (y - y1) < p.radius * p.radius) {
                psmin = p;
            }
        }
        return psmin;
    }

    public synchronized Point closestPoint(double x, double y) {
        Point pmin = null;
        double dmin = Double.POSITIVE_INFINITY;
        for (Point p : points) {
            //           System.out.println("link from "+l.p1.particleName+" - "+l.p2.particleName);
            double x1 = p.x;
            double y1 = p.y;
            if ((x - x1) * (x - x1) + (y - y1) * (y - y1) < dmin) {
                dmin = (x - x1) * (x - x1) + (y - y1) * (y - y1);
                pmin = p;
            }
        }
        return pmin;
    }

    public synchronized Link closestLink(double x, double y) {
        Link lmin = null;
        double dmin = Double.POSITIVE_INFINITY;
        for (Link l : links) {
            //           System.out.println("link from "+l.p1.particleName+" - "+l.p2.particleName);
            double x1 = (l.p1.x + l.p2.x) / 2;
            double y1 = (l.p1.y + l.p2.y) / 2;
            if ((x - x1) * (x - x1) + (y - y1) * (y - y1) < dmin) {
                dmin = (x - x1) * (x - x1) + (y - y1) * (y - y1);
                lmin = l;
            }
        }
        return lmin;
    }

    public synchronized Shape addShape() {
        Shape s = new Shape();
        s.label = "shape" + shapeCounter;
        shapeCounter++;
        shapes.add(s);
        return s;
    }

    public synchronized void moveShapeRelative(Shape s, double dx, double dy) {
        for (Point xy : s.points) {
            xy.replaceXY(xy.getZX() + dx, xy.getZY() + dy);
        };
    }

    public synchronized void movePointRelative(Point p, double dx, double dy) {
        p.x = p.x + dx;
        p.y = p.y + dy;
    }

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

    public synchronized boolean selectPoint(double x, double y) {

        boolean selected = false;
        for (Shape s : shapes) {
            for (Point p : s.points) {
                if (((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y)) < p.radius * p.radius) {
                    p.isSelected = true;
                    selected = true;
                }
            }
        }
        return selected;
    }

    public synchronized boolean unSelectPoint(double x, double y) {

        boolean unselected = false;
        for (Shape s : shapes) {
            for (Point p : s.points) {
                if (((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y)) < p.radius * p.radius) {
                    p.isSelected = false;
                    unselected = true;
                }
            }
        }
        return unselected;
    }

    public synchronized void selectAll() {

        for (Point p : points) {
            p.isSelected = true;
        }
        for (Link l : links) {
            l.isSelected = true;
        }
    }

    public synchronized void unselectAll() {

        for (Point p : points) {
            p.isSelected = false;
        }
        for (Link l : links) {
            l.isSelected = false;
        }
    }

    public synchronized void selectArea() {
// preSelect all shapes that have a point in the cursor area
// preSelection is reset each time this method is called due to cursor movement

        for (Point p : points) {
            p.isPreSelected = false;
            if ((p.x >= areaCursorX1) && (p.x <= areaCursorX2)
                    && (p.y >= areaCursorY1) && (p.y <= areaCursorY2)) {
                p.isPreSelected = true;
            }
        }

        for (Link l : links) {
            if (l.p1.isPreSelected) {
                l.isPreSelected = true;
            }
            if (l.p2.isPreSelected) {
                l.isPreSelected = true;
            }
        }

    }

    public synchronized void unselectArea() {
// preUnselect all shapes that have a point in the cursor area
// preUnselection is reset each time this method is called due to cursor movement

        for (Point p : points) {
            p.isPreUnselected = false;
            if ((p.x >= areaCursorX1) && (p.x <= areaCursorX2)
                    && (p.y >= areaCursorY1) && (p.y <= areaCursorY2)) {
                p.isPreUnselected = true;
            }
        }

        for (Link l : links) {
            if (l.p1.isPreUnselected) {
                l.isPreSelected = true;
            }
            if (l.p2.isPreUnselected) {
                l.isPreSelected = true;
            }
        }
    }

    public synchronized void commitSelectedArea() {
// Select all shapes that were preselected. Called when area selection is final

        for (Point p : points) {
            if (p.isPreSelected) {
                p.isPreSelected = false;
                p.isSelected = true;
                System.out.println("commit select " + p.particleName);
            }
        }
        for (Link l : links) {
            if (l.isPreSelected) {
                l.isPreSelected = false;
                l.isSelected = true;
            }
        }
    }

    public synchronized void commitUnselectedArea() {
        for (Point p : points) {
            if (p.isPreUnselected) {
                p.isPreUnselected = false;
                p.isSelected = false;
            }
        }
        for (Link l : links) {
            if (l.isPreUnselected) {
                l.isPreUnselected = false;
                l.isSelected = false;
            }
        }
    }
}
