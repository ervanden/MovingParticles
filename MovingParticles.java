
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;

public class MovingParticles implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ItemListener {

    static Drawing Drawing = new Drawing();
    static Transform transform = new Transform();

    static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    static GraphicsDevice gd = ge.getDefaultScreenDevice();
    static GraphicsConfiguration gc = gd.getDefaultConfiguration();

    static JFrame zFrame = new JFrame();
    static JFrame dFrame = new JFrame("Settings");
    PropertyFrame propertyFrame = new PropertyFrame();

    JCheckBox labelButton = new JCheckBox("labels");
    JCheckBox lineButton = new JCheckBox("lines");
    JCheckBox scaleButton = new JCheckBox("scale");
    JCheckBox gridButton = new JCheckBox("snap to grid ");
    JCheckBox animateButton = new JCheckBox("animate");

    static DrawingPlane plane = new DrawingPlane();

    AnimationRunner animation = null;
    Thread animateThread = null;

    static int x, xprev;
    static int y, yprev;
    static double xuserprev, yuserprev;
    double areaCursorX1, areaCursorY1, areaCursorX2, areaCursorY2;

    String lastButtonClicked = "";

    boolean actionAddShape = false;
    boolean actionZoomIn = false;
    boolean actionMoveView = false;
    boolean actionAddLine = false;
    boolean actionAddCircle = false;
    boolean actionAddPoint = false;

    boolean actionMovePoint = false;
    boolean actionMoveSelection = false;
    boolean actionMoveAll = false;

    boolean actionDeletePoint = false;
    boolean actionPropertiesPoint = false;
    boolean actionFixPoint = false;
    boolean actionUnfixPoint = false;
    boolean actionDeleteLink = false;
    boolean actionUnselect = false;
    boolean actionSelect = false;
    boolean actionSelectArea = false;
    boolean actionUnselectArea = false;

    boolean firstPoint = true; // tells if mouseDragged event is the first point
    Shape currentShape;

    ShapeDialog shapeDialog = new ShapeDialog();
    double xCircle = 0, yCircle = 0; // center of circle
    double xRadius = 0, yRadius = 0; // clicked point to mark radius

    Shape cps;
    Point cp;

    public static void repaint() {
        plane.blitPaint();
    }

    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        System.out.println("KEY TYPED: " + c);
        if (c == 'r') {
            System.out.println(" > repeat " + lastButtonClicked);
            executeAction(lastButtonClicked);
        };
    }

    public void keyPressed(KeyEvent e) {
        System.out.println("KEY PRESSED: ");
    }

    public void keyReleased(KeyEvent e) {
        System.out.println("KEY RELEASED: ");
    }

    public void mouseMoved(MouseEvent e) {
        x = e.getX();
        y = e.getY();

        zFrame.setTitle(String.format("x=%.2f y=%.2f", transform.xScreenToUser(x),
                transform.yScreenToUser(y)));
    }

    public void mouseDragged(MouseEvent e) {
        x = e.getX();
        y = e.getY();

        zFrame.setTitle(String.format("x=%.2f y=%.2f", transform.xScreenToUser(x), transform.yScreenToUser(y)));

        if (actionAddCircle) {

            double radius, gridAngle;

            if (firstPoint) {
                xCircle = transform.xScreenToUser(x);
                yCircle = transform.yScreenToUser(y);
                if (Drawing.snapToGrid) {
                    xCircle = Math.round(xCircle);
                    yCircle = Math.round(yCircle);
                }
                firstPoint = false;

            } else {
                xRadius = transform.xScreenToUser(x);
                yRadius = transform.yScreenToUser(y);
                if (Drawing.snapToGrid) {
                    xRadius = Math.round(xRadius);
                    yRadius = Math.round(yRadius);
                }

                radius = Math.sqrt((xRadius - xCircle) * (xRadius - xCircle) + (yRadius - yCircle) * (yRadius - yCircle));
                Drawing.circleCursor(true, xCircle, yCircle, radius);
                repaint();
            }
        }

        if (actionAddShape) {
            // add point only if sufficiently far from previous OR if it is the first point 
            double xu = transform.xScreenToUser(x);
            double yu = transform.yScreenToUser(y);
            if (firstPoint) {
                Point p = Drawing.addPointToShape(currentShape, xu, yu);
                p.radius = shapeDialog.pointRadius;
                xprev = x;
                yprev = y;
            } else {
                double xpu = transform.xScreenToUser(xprev);
                double ypu = transform.yScreenToUser(yprev);
                double minSegmentLength = shapeDialog.segmentLength;
                if (((xu - xpu) * (xu - xpu) + (yu - ypu) * (yu - ypu))
                        > minSegmentLength * minSegmentLength) {
                    Point p = Drawing.addPointToShape(currentShape, xu, yu);
                    p.radius = shapeDialog.pointRadius;
                    xprev = x;
                    yprev = y;
                }
            }
            firstPoint = false;
            repaint();
        }

        if ((actionMoveAll || actionMoveSelection)) {
            if (firstPoint) {
                xprev = x;
                yprev = y;
                firstPoint = false;
            };
            //           if (!firstPoint && ((x - xprev) * (x - xprev) + (y - yprev) * (y - yprev) > minPixelDistSquare)) {
            if (!firstPoint) {
                String whatToMove = "";
                if (actionMoveAll) {
                    whatToMove = "all";
                }
                if (actionMoveSelection) {
                    whatToMove = "selection";
                }
                Drawing.moveDrawingRelative(whatToMove,
                        transform.xScreenToUser(x) - transform.xScreenToUser(xprev),
                        transform.yScreenToUser(y) - transform.yScreenToUser(yprev));
                repaint();
                xprev = x;
                yprev = y;
            }
        }

        if (actionMovePoint) {

            if (firstPoint) {
                double xuser = transform.xScreenToUser(x);
                double yuser = transform.yScreenToUser(y);
                cp = Drawing.closestPoint(xuser, yuser);
                xuserprev = cp.x;
                yuserprev = cp.y;
                if (Drawing.snapToGrid) {
                    // move point to closest grid point
                    double xusernew = Math.round(xuserprev);
                    double yusernew = Math.round(yuserprev);
                    Drawing.movePointRelative(cp, xusernew - xuserprev, yusernew - yuserprev);
                    xuserprev = xusernew;
                    yuserprev = yusernew;
                };
                firstPoint = false;
            } else {
                double xusernew = transform.xScreenToUser(x);
                double yusernew = transform.yScreenToUser(y);
                if (Drawing.snapToGrid) {
                    xusernew = Math.round(xusernew);
                    yusernew = Math.round(yusernew);
                };
                // if coordinates are snapped to grid, they increment with 0,1,2,...
                // Do not move the drawing if the mouse did not move enough to go to another grid point
                if (!Drawing.snapToGrid
                        || (Drawing.snapToGrid
                        && (Math.abs(xusernew - xuserprev) > 0.5 || Math.abs(yusernew - yuserprev) > 0.5))) {

                    Drawing.movePointRelative(cp, xusernew - xuserprev, yusernew - yuserprev);

                    repaint();
                }

                xuserprev = xusernew;
                yuserprev = yusernew;

            }
        }

        if (actionMoveView) {

            double userDeltaX, userDeltaY;

            if (firstPoint) {
                xprev = x;
                yprev = y;
                firstPoint = false;
            }

            if (!firstPoint) {
                userDeltaX = transform.xScreenToUser(x) - transform.xScreenToUser(xprev);
                userDeltaY = transform.yScreenToUser(y) - transform.yScreenToUser(yprev);
                xprev = x;
                yprev = y;

                double xmin = transform.uxmin;
                double xmax = transform.uxmax;
                double ymin = transform.uymin;
                double ymax = transform.uymax;

                transform.setUserSpace(xmin - userDeltaX, xmax - userDeltaX, ymin - userDeltaY, ymax - userDeltaY);

                repaint();

            }

        }

        if ((actionSelectArea || actionUnselectArea)) {

            if (firstPoint) {
                areaCursorX1 = transform.xScreenToUser(x);
                areaCursorY1 = transform.yScreenToUser(y);
                firstPoint = false;

            } else {

                areaCursorX2 = transform.xScreenToUser(x);
                areaCursorY2 = transform.yScreenToUser(y);

                Drawing.areaCursor(true,
                        Math.min(areaCursorX1, areaCursorX2),
                        Math.min(areaCursorY1, areaCursorY2),
                        Math.max(areaCursorX1, areaCursorX2),
                        Math.max(areaCursorY1, areaCursorY2));

                if (actionSelectArea) {
                    Drawing.selectArea();
                }
                if (actionUnselectArea) {
                    Drawing.unselectArea();
                }

                repaint();

            }
        }

    }

    public void mouseClicked(MouseEvent e) {
        saySomething("Mouse clicked; # of clicks: "
                + e.getClickCount(), e);
    }

    public void mouseReleased(MouseEvent e) {
        saySomething("Mouse released; # of clicks: " + e.getClickCount(), e);

        if (actionAddShape) {
            actionAddShape = false;
            /*           
             // add terminal point
             Drawing.addPointToShape(currentShape, transform.xScreenToUser(x),
             transform.yScreenToUser(y));
             repaint();
             */
        }

        if (actionMoveAll || actionMoveSelection || actionMovePoint || actionMoveView) {
            firstPoint = true;  // ready to move next shape
        }

        if (actionAddCircle) {
            Drawing.circleCursor(false, 0, 0, 0);

            double gridAngle;
            double radius;

            radius = Math.sqrt((xRadius - xCircle) * (xRadius - xCircle) + (yRadius - yCircle) * (yRadius - yCircle));
            gridAngle = Math.sqrt(transform.xScreenToUser(10) - transform.xScreenToUser(0)) / radius;
            if (gridAngle > (Math.PI / 10)) {
                gridAngle = Math.PI / 10;
            }
            currentShape = Drawing.addShape();
            for (double angle = 0; angle < 2 * Math.PI; angle = angle + gridAngle) {
                Drawing.addPointToShape(currentShape, xCircle + radius * Math.sin(angle), yCircle + radius * Math.cos(angle));
            }
            Drawing.addPointToShape(currentShape, xCircle, yCircle + radius);
            repaint();

            actionAddCircle = false;
            firstPoint = true;
        }

        if (actionSelectArea) {
            actionSelectArea = false;
            Drawing.commitSelectedArea();
            Drawing.areaCursor(false, 0, 0, 0, 0);
            repaint();
        }

        if (actionUnselectArea) {
            actionUnselectArea = false;
            Drawing.commitUnselectedArea();
            Drawing.areaCursor(false, 0, 0, 0, 0);
            repaint();
        }

    }

    public void mouseEntered(MouseEvent e) {
        saySomething("Mouse entered", e);
    }

    public void mouseExited(MouseEvent e) {
        saySomething("Mouse exited", e);

        if (actionZoomIn && !firstPoint) {
            actionZoomIn = false;
            firstPoint = true;
        };

        actionMoveView = false;
        actionAddPoint = false;
        actionAddShape = false;
        actionAddLine = false;
        actionMovePoint = false;
        actionMoveSelection = false;
        actionMoveAll = false;
        actionDeletePoint = false;
        actionPropertiesPoint = false;
        actionFixPoint = false;
        actionUnfixPoint = false;
        actionDeleteLink = false;
        actionSelect = false;
        actionUnselect = false;

    }

    public void mousePressed(MouseEvent e) {
        x = e.getX();
        y = e.getY();
        saySomething("Mouse pressed (" + x + "," + y + ") (# of clicks: " + e.getClickCount() + ")", e);

        if (actionFixPoint) {

            double xuser = transform.xScreenToUser(x);
            double yuser = transform.yScreenToUser(y);
            cp = Drawing.closestPoint(xuser, yuser);
            if (cp != null) {
                cp.fixed = true;
            }
            repaint();
        }
        if (actionUnfixPoint) {

            double xuser = transform.xScreenToUser(x);
            double yuser = transform.yScreenToUser(y);
            cp = Drawing.closestPoint(xuser, yuser);
            if (cp != null) {
                cp.fixed = false;
            }
            repaint();
        }

        if (actionDeletePoint) {

            double xuser = transform.xScreenToUser(x);
            double yuser = transform.yScreenToUser(y);
            Drawing.deletePoint(Drawing.closestPoint(xuser, yuser));
            repaint();
        }

        if (actionPropertiesPoint) {

            double xuser = transform.xScreenToUser(x);
            double yuser = transform.yScreenToUser(y);
            Point p = Drawing.closestPoint(xuser, yuser);
            if (p != null) {
                ArrayList<Point> pl = new ArrayList<>();
                pl.add(p);
                propertyFrame.display(pl);
            }
        }

        if (actionDeleteLink) {

            double xuser = transform.xScreenToUser(x);
            double yuser = transform.yScreenToUser(y);
            Drawing.deleteLink(Drawing.closestLink(xuser, yuser));
            repaint();
        }

        if (actionZoomIn) {
            // the clicked location becomes the new Center
            int zoomFactor = 2;
            double userx, usery;

            double xmin = transform.uxmin;
            double xmax = transform.uxmax;
            double ymin = transform.uymin;
            double ymax = transform.uymax;
            userx = transform.xScreenToUser(x);
            usery = transform.yScreenToUser(y);

            transform.setUserSpace(userx - (xmax - xmin) / (2 * zoomFactor),
                    userx + (xmax - xmin) / (2 * zoomFactor),
                    usery - (ymax - ymin) / (2 * zoomFactor),
                    usery + (ymax - ymin) / (2 * zoomFactor));

            repaint();

            firstPoint = false;  // from now on, exiting the window = terminate zoomIn

        };

        if (actionSelect) {

            // actionSelect stays active until the cursor leaves the plane
            double userx, usery;
            Point p;
            Link l;
            userx = transform.xScreenToUser(e.getX());
            usery = transform.yScreenToUser(e.getY());
            p = Drawing.locatePoint(userx, usery);
            if (p != null) {
                p.isSelected = true;
            } else {
                l = Drawing.closestLink(userx, usery);
                if (l != null) {
                    l.isSelected = true;
                }
            }

            repaint();

        }

        if (actionUnselect) {

            // actionUnselect stays active until the cursor leaves the z plane
            double userx, usery;
            userx = transform.xScreenToUser(e.getX());
            usery = transform.yScreenToUser(e.getY());
            if (Drawing.unSelectPoint(userx, usery)) {
                repaint();
            }

        }

        if (actionAddLine) {
            if (firstPoint) {
                Drawing.addPointToShape(currentShape, transform.xScreenToUser(x), transform.yScreenToUser(y));
                xprev = x;
                yprev = y;
                firstPoint = false;
            } else {
                int nsegments;
                double xbegin, ybegin, xend, yend, xi, yi;
                xend = transform.xScreenToUser(x);
                yend = transform.yScreenToUser(y);
                xbegin = transform.xScreenToUser(xprev);
                ybegin = transform.yScreenToUser(yprev);
                if (Drawing.snapToGrid) {
                    xend = Math.round(xend);
                    xbegin = Math.round(xbegin);
                    yend = Math.round(yend);
                    ybegin = Math.round(ybegin);
                };

                nsegments = 1;
                // create nsegments intermediate points
                for (int i = 1; i <= nsegments; i++) {
                    Drawing.addPointToShape(currentShape, xbegin + ((double) i / (double) nsegments) * (xend - xbegin),
                            ybegin + ((double) i / (double) nsegments) * (yend - ybegin));
                };

                xprev = x;
                yprev = y;
            }
            repaint();

            if (e.getClickCount() > 1) {
                actionAddLine = false;
            }
        }

        if (actionAddPoint) {

            xprev = x;
            yprev = y;
            double xuser = transform.xScreenToUser(x);
            double yuser = transform.yScreenToUser(y);
            cps = Drawing.addShape();
            if (Drawing.snapToGrid) {
                xuser = Math.round(xuser);
                yuser = Math.round(yuser);
            };
            Drawing.addPointToShape(cps, xuser, yuser);

            repaint();
            firstPoint = true;
            actionMovePoint = true;

        }

    }

    void saySomething(String eventDescription, MouseEvent e) {
//        System.out.println(eventDescription + " detected on " + e.getComponent().getClass().getName());
    }

    public void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();

        if (source == labelButton) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Drawing.labelsVisible = true;
            }
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                Drawing.labelsVisible = false;
            }
            repaint();
        }

        if (source == lineButton) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Drawing.linesVisible = true;
            }
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                Drawing.linesVisible = false;
            }
            repaint();
        }

        if (source == scaleButton) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Drawing.scaleVisible = true;
            };
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                Drawing.scaleVisible = false;
            }
            repaint();
        }

        if (source == gridButton) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Drawing.snapToGrid = true;
            }
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                Drawing.snapToGrid = false;
            }
            repaint();
        }

        if (source == animateButton) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (animateThread == null) {
                    animation = new AnimationRunner();
                    animateThread = new Thread(animation);
                    animateThread.start();
                    // thread starts, but suspended
                }
            }
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (animateThread != null) {
                    animation.exitRequested = true;
                    animateThread = null;
                }
            }
        }
    }

    public void actionPerformed(ActionEvent ae) {

        lastButtonClicked = ae.getActionCommand();
//       System.out.println(lastButtonClicked + " pressed!");
        this.executeAction(lastButtonClicked);

    }

    String AttributeToString(String name, Object value) {
        return name + "=" + value;
    }

    public void executeAction(String buttonClicked) {

        firstPoint = true;

        if (buttonClicked == "Add Line") {
            actionAddLine = true;
            firstPoint = true;
            currentShape = Drawing.addShape();
        }

        if (buttonClicked == "Add Shape") {
            shapeDialog.popUp(zFrame, 1.5, 0.5);
            if (shapeDialog.validValues) {
                actionAddShape = true;
                firstPoint = true;
                currentShape = Drawing.addShape();
            }
        }

        if (buttonClicked == "Add Circle") {
            actionAddCircle = true;
            firstPoint = true;
        };

        if (buttonClicked == "Add Point") {
            actionAddPoint = true;
        };

        if (buttonClicked == "Move Point") {
            actionMovePoint = true;
            firstPoint = true;
        };

        if (buttonClicked == "Move Selection") {
            actionMoveSelection = true;
            firstPoint = true;
        };

        if (buttonClicked == "Move All") {
            actionMoveAll = true;
            firstPoint = true;
        };

        if (buttonClicked == "Fix Point") {
            actionFixPoint = true;
            firstPoint = true;
        };

        if (buttonClicked == "Unfix Point") {
            actionUnfixPoint = true;
            firstPoint = true;
        };

        if (buttonClicked == "Delete Point") {
            actionDeletePoint = true;
            firstPoint = true;
        };

        if (buttonClicked == "Delete Link") {
            actionDeleteLink = true;
            firstPoint = true;
        };

        if (buttonClicked == "Delete Selected Points") {
            Drawing.deleteSelectedPoints();
            repaint();
        };

        if (buttonClicked == "Delete Selected Links") {
            Drawing.deleteSelectedLinks();
            repaint();
        };

        if (buttonClicked == "Delete All") {
            Drawing.deleteDrawing();
            repaint();
        };

        if (buttonClicked == "Properties Point") {
            actionPropertiesPoint = true;
            firstPoint = true;
        };

        if (buttonClicked == "Properties Link") {
            //           actionPropertiesLink=true;
        };

        if (buttonClicked == "Properties Selected Points") {
            propertyFrame.display(Drawing.getSelectedPoints());
        };

        if (buttonClicked == "Properties Selected Links") {
            //           propertyFrame.display(Drawing.selectedLinks();
        };

        if (buttonClicked == "Move View") {
            actionMoveView = true;
            firstPoint = true;
        };

        if (buttonClicked == "Select") {
            actionSelect = true;
        };

        if (buttonClicked == "Unselect") {
            actionUnselect = true;
        };

        if (buttonClicked == "Select Area") {
            actionSelectArea = true;
            firstPoint = true;
        };

        if (buttonClicked == "Unselect Area") {
            actionUnselectArea = true;
            firstPoint = true;
        };

        if (buttonClicked == "Select All") {
            Drawing.selectAll();
            repaint();
        };

        if (buttonClicked == "Unselect All") {
            Drawing.unselectAll();
            repaint();
        };

        if (buttonClicked == "Zoom In") {
            actionZoomIn = true;
            firstPoint = true;
        };

        if (buttonClicked == "Zplane Zoom Out") {
            double userx, usery;
            int zoomOutFactor = 2;

            double xmin = transform.uxmin;
            double xmax = transform.uxmax;
            double ymin = transform.uymin;
            double ymax = transform.uymax;
            userx = (xmax + xmin) / 2;
            usery = (ymax + ymin) / 2;

            transform.setUserSpace(userx - ((xmax - xmin) / 2) * zoomOutFactor,
                    userx + ((xmax - xmin) / 2) * zoomOutFactor,
                    usery - ((ymax - ymin) / 2) * zoomOutFactor,
                    usery + ((ymax - ymin) / 2) * zoomOutFactor);
            repaint();

        };

        if (buttonClicked == "Zplane Fit") {

            Drawing.setMinMax();

            double xmin = Drawing.xmin;
            double xmax = Drawing.xmax;
            double ymin = Drawing.ymin;
            double ymax = Drawing.ymax;
            double xcenter = (xmin + xmax) / 2;
            double ycenter = (ymin + ymax) / 2;
            double w = Math.max(xmax - xmin, ymax - ymin);

            transform.setUserSpace(xcenter - w / 2,
                    xcenter + w / 2,
                    ycenter - w / 2,
                    ycenter + w / 2);
            repaint();

        };

        if (buttonClicked == "Add Grid") {

            double xmin = transform.uxmin;
            double ymin = transform.uymin;

            GridDialog gd = new GridDialog();
            gd.popUp(zFrame, xmin, ymin, 1, 1, 10, 10);

            if (gd.validValues) {
                xmin = gd.xmin;
                double xgrid = gd.xdelta;
                ymin = gd.ymin;
                double ygrid = gd.ydelta;
                int nx = gd.xn;
                int ny = gd.yn;

                Point[][] mpoints = new Point[nx][ny];

                for (int ix = 0; ix < nx; ix++) {
                    for (int iy = 0; iy < ny; iy++) {
                        Point p = MovingParticles.Drawing.addPoint(xmin + ix * xgrid, ymin + iy * ygrid);
                        mpoints[ix][iy] = p;
                    }
                }
                // horizontal links
                for (int ix = 0; ix < nx - 1; ix++) {
                    for (int iy = 0; iy < ny; iy++) {
                        MovingParticles.Drawing.addLink(mpoints[ix][iy], mpoints[ix + 1][iy]);
                    }
                }
                //vertical links
                for (int ix = 0; ix < nx; ix++) {
                    for (int iy = 0; iy < ny - 1; iy++) {
                        MovingParticles.Drawing.addLink(mpoints[ix][iy], mpoints[ix][iy + 1]);
                    }
                }

                repaint();
            }
        }

        if (buttonClicked == "Read From File") {
            JFileChooser fileChooser = new JFileChooser();
            int retval = fileChooser.showOpenDialog(null);
            if (retval == JFileChooser.APPROVE_OPTION) {
                File fileIn = fileChooser.getSelectedFile();
                try {
                    InputStream is = new FileInputStream(fileIn);
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader in = new BufferedReader(isr);

                    HashMap<String, Point> nameToPoint = new HashMap<>();

                    String l;
                    String[] ls;
                    while ((l = in.readLine()) != null) {
                        ls = l.split("\\|");

                        if (ls[0].equals("point")) {
                            String name = "";
                            double x = 0;
                            double y = 0;
                            double radius = 1;
                            double mass = 1;
                            double velocity = 0;
                            double angle = 0;
                            boolean filled = false;
                            boolean fixed = false;

                            for (int k = 1; k < ls.length; k++) {
                                String[] als;
                                als = ls[k].split("=");
                                String attributeName = als[0];
                                String attributeValue = als[1];
                                if (attributeName.equals("name")) {
                                    name = attributeValue;
                                } else if (attributeName.equals("x")) {
                                    x = Double.valueOf(attributeValue);
                                } else if (attributeName.equals("y")) {
                                    y = Double.valueOf(attributeValue);
                                } else if (attributeName.equals("radius")) {
                                    radius = Double.valueOf(attributeValue);
                                } else if (attributeName.equals("mass")) {
                                    mass = Double.valueOf(attributeValue);
                                } else if (attributeName.equals("angle")) {
                                    angle = Double.valueOf(attributeValue);
                                } else if (attributeName.equals("velocity")) {
                                    velocity = Double.valueOf(attributeValue);
                                } else if (attributeName.equals("filled")) {
                                    filled = attributeValue.equals("true");
                                } else if (attributeName.equals("fixed")) {
                                    fixed = attributeValue.equals("true");
                                }
                            }

                            Point p = MovingParticles.Drawing.addPoint(x, y);
                            p.name = name;
                            p.x = x;
                            p.y = y;
                            p.velocity = velocity;
                            p.angle = angle;
                            p.mass = mass;
                            p.radius = radius;
                            p.fixed = fixed;
                            p.filled = filled;

                            nameToPoint.put(name, p);

                        } else if (ls[0].equals("link")) {
                            ls = l.split("\\|");
                            MovingParticles.Drawing.addLink(nameToPoint.get(ls[1]), nameToPoint.get(ls[2]));
                        }
                    }
                    in.close();
                    repaint();
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
        }

        if (buttonClicked == "Save To File") {

            ArrayList<Point> points = MovingParticles.Drawing.getPoints();
            ArrayList<Link> links = MovingParticles.Drawing.getLinks();

            JFileChooser fileChooser = new JFileChooser();
            int retval = fileChooser.showOpenDialog(null);
            if (retval == JFileChooser.APPROVE_OPTION) {
                File fileOut = fileChooser.getSelectedFile();
                try {
                    OutputStream is = new FileOutputStream(fileOut);
                    OutputStreamWriter isr = new OutputStreamWriter(is, "UTF-8");
                    BufferedWriter out = new BufferedWriter(isr);
                    for (Point p : points) {
                        out.write("point|"
                                + AttributeToString("name", p.name) + "|"
                                + AttributeToString("x", p.x) + "|"
                                + AttributeToString("y", p.y) + "|"
                                + AttributeToString("radius", p.radius) + "|"
                                + AttributeToString("filled", p.filled) + "|"
                                + AttributeToString("velocity", p.velocity) + "|"
                                + AttributeToString("angle", p.angle) + "|"
                                + AttributeToString("fixed", p.fixed) + "|"
                                + AttributeToString("mass", p.mass) + "|"
                        );
                        out.newLine();
                    };
                    for (Link l : links) {
                        out.write("link|"
                                + l.p1.name + "|"
                                + l.p2.name + "|"
                        );
                        out.newLine();
                    };
                    out.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }
        }

    }

    private void AddMenuItem(JMenu menu, String name, String action) {
        JMenuItem menuItem;
        menuItem = new JMenuItem(name);
        menuItem.addActionListener(this);
        menuItem.setActionCommand(action);
        menu.add(menuItem);
    }

    private void create() {   // Create and set up the windows.

        plane.t = transform;

        transform.setUserSpace(-10, 10, -10, 10);

        //get device screen coordinates to position the frame
        Rectangle bounds = gc.getBounds(); // device coordinates of the screen (0,0) = upper left (w,h) = lo right

        zFrame.setLocation(bounds.width / 2, 0);  //bounds.height / 3);
        zFrame.setSize(bounds.width / 2 - 22, bounds.width / 2);

        zFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        plane.setBackground(Color.white);

        plane.addMouseListener(this);
        plane.addMouseMotionListener(this);
        plane.addKeyListener(this);

        zFrame.add(plane);
        zFrame.setTitle("space");
        zFrame.setVisible(true);
        zFrame.add(plane);

        plane.createBufferStrategy(2);
        plane.setIgnoreRepaint(true);

        plane.blitPaint();

// populate settings frame
        Container pane = dFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        labelButton.setSelected(false);
        labelButton.addItemListener(this);

        lineButton.setSelected(true);
        lineButton.addItemListener(this);

        scaleButton.setSelected(true);
        scaleButton.addItemListener(this);

        gridButton.setSelected(false);
        gridButton.addItemListener(this);

        animateButton.setSelected(false);
        animateButton.addItemListener(this);

        JPanel editorPane = new JPanel();
        editorPane.setLayout(new BoxLayout(editorPane, BoxLayout.LINE_AXIS));
        editorPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        editorPane.add(Box.createRigidArea(new Dimension(40, 0)));
        editorPane.add(labelButton);
        editorPane.add(Box.createRigidArea(new Dimension(20, 0)));
        editorPane.add(lineButton);
        editorPane.add(Box.createRigidArea(new Dimension(20, 0)));
        editorPane.add(scaleButton);
        editorPane.add(Box.createRigidArea(new Dimension(20, 0)));
        editorPane.add(gridButton);
        editorPane.add(Box.createRigidArea(new Dimension(20, 0)));
        editorPane.add(animateButton);

        pane.add(editorPane);

        dFrame.pack();
        dFrame.setVisible(true);


        JMenuBar zMenuBar = new JMenuBar();

        JMenu menuAdd = new JMenu("Add");
        zMenuBar.add(menuAdd);
        AddMenuItem(menuAdd, "Line ...", "Add Line");
        AddMenuItem(menuAdd, "Shape ...", "Add Shape");
        AddMenuItem(menuAdd, "Circle ...", "Add Circle");
        AddMenuItem(menuAdd, "Point ...", "Add Point");
        AddMenuItem(menuAdd, "Grid", "Add Grid");
        AddMenuItem(menuAdd, "Read from File", "Read From File");
        AddMenuItem(menuAdd, "Save to File", "Save To File");

        JMenu menuSelect = new JMenu("Select");
        zMenuBar.add(menuSelect);
        AddMenuItem(menuSelect, "Select", "Select");
        AddMenuItem(menuSelect, "Unselect", "Unselect");
        AddMenuItem(menuSelect, "Select Area", "Select Area");
        AddMenuItem(menuSelect, "Unselect Area", "Unselect Area");
        AddMenuItem(menuSelect, "Select All", "Select All");
        AddMenuItem(menuSelect, "Unselect All", "Unselect All");

        JMenu menuMove = new JMenu("Move");
        zMenuBar.add(menuMove);
        AddMenuItem(menuMove, "Point", "Move Point");
        AddMenuItem(menuMove, "Selection", "Move Selection");
        AddMenuItem(menuMove, "All", "Move All");

        JMenu menuDelete = new JMenu("Delete");
        zMenuBar.add(menuDelete);
        AddMenuItem(menuDelete, "Point", "Delete Point");
        AddMenuItem(menuDelete, "Link", "Delete Link");
        AddMenuItem(menuDelete, "Selected Points", "Delete Selected Points");
        AddMenuItem(menuDelete, "Selected Links", "Delete Selected Links");
        AddMenuItem(menuDelete, "All", "Delete All");

        JMenu menuFix = new JMenu("Particles");
        zMenuBar.add(menuFix);
        AddMenuItem(menuFix, "Fix Point", "Fix Point");
        AddMenuItem(menuFix, "Unfix Point", "Unfix Point");

        JMenu menuProperties = new JMenu("Properties");
        zMenuBar.add(menuProperties);
        AddMenuItem(menuProperties, "Point", "Properties Point");
        AddMenuItem(menuProperties, "Link", "Properties Link");
        AddMenuItem(menuProperties, "Selected Points", "Properties Selected Points");
        AddMenuItem(menuProperties, "Selected Links", "Properties Selected Links");

        JMenu menuZplane = new JMenu("View");
        zMenuBar.add(menuZplane);
        AddMenuItem(menuZplane, "Zoom Out", "Zplane Zoom Out");
        AddMenuItem(menuZplane, "Zoom In ...", "Zoom In");
        AddMenuItem(menuZplane, "Move ...", "Move View");
        AddMenuItem(menuZplane, "Fit", "Zplane Fit");

        zFrame.setJMenuBar(zMenuBar);

 //       propertyFrame = new PropertyFrame();
        repaint();
        
    }

    public void display() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                create();
            }
        });
    }

}

class MainVisualComplex {

    public static void main(String[] args) {

        MovingParticles vc = new MovingParticles();

        vc.display();

    }

}
