
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;


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

    
    
 //   Gravity gravity = null;
 //   Elasticity elasticity = null;
 //   Rotation rotation = null;

    

}
