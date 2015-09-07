
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MovingParticles implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ItemListener {

    static Drawing Drawing = new Drawing();
    static Transform transform = new Transform();

    static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    static GraphicsDevice gd = ge.getDefaultScreenDevice();
    static GraphicsConfiguration gc = gd.getDefaultConfiguration();

    static JFrame zFrame = new JFrame();
    static JFrame dFrame = new JFrame("Settings");

    JCheckBox dotButton = new JCheckBox("dots");
    JCheckBox lineButton = new JCheckBox("lines");
    JCheckBox scaleButton = new JCheckBox("scale");
    JCheckBox gridButton = new JCheckBox("snap to grid ");
    JCheckBox animateButton = new JCheckBox("animate");

    static ComplexPlane zPlane = new ComplexPlane();

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
    boolean actionMoveShape = false;
    boolean actionMoveSelection = false;
    boolean actionMoveAll = false;

    boolean actionDeletePoint = false;
    boolean actionDeleteShape = false;
    boolean actionUnselect = false;
    boolean actionSelect = false;
    boolean actionSelectArea = false;
    boolean actionUnselectArea = false;

    boolean firstPoint = true; // tells if mouseDragged event is the first point
    Shape currentShape;

    int minPixelDist = 10;
    int minPixelDistSquare = minPixelDist * minPixelDist;

    double xCircle = 0, yCircle = 0; // center of circle
    double xRadius = 0, yRadius = 0; // clicked point to mark radius

    PointShape cps;
    Point cp;

    public static void repaintZplane() {
        zPlane.blitPaint();
    }

    public static void repaintBothWindows() {
        zPlane.blitPaint();
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
        ComplexPlane plane = (ComplexPlane) e.getComponent();
        x = e.getX();
        y = e.getY();

        if (plane.zPlane) {
            zFrame.setTitle(String.format("x=%.2f y=%.2f", transform.xScreenToUser(x),
                    transform.yScreenToUser(y)));
        };

    }

    public void mouseDragged(MouseEvent e) {
        ComplexPlane plane = (ComplexPlane) e.getComponent();
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

                Drawing.clearShape(currentShape);

                radius = Math.sqrt((xRadius - xCircle) * (xRadius - xCircle) + (yRadius - yCircle) * (yRadius - yCircle));
                gridAngle = Math.sqrt(transform.xScreenToUser(minPixelDist) - transform.xScreenToUser(0)) / radius;
                if (gridAngle > (Math.PI / 10)) {
                    gridAngle = Math.PI / 10;
                }

                for (double angle = 0; angle < 2 * Math.PI; angle = angle + gridAngle) {
                    Drawing.addPointToShape(currentShape, xCircle + radius * Math.sin(angle), yCircle + radius * Math.cos(angle));
                };

                Drawing.addPointToShape(currentShape, xCircle, yCircle + radius);

                repaintBothWindows();

            };

        };

        if (actionAddShape) {
            // add point only if sufficiently far from previous OR if it is the first point 
            if ((firstPoint) || (!firstPoint && ((x - xprev) * (x - xprev) + (y - yprev) * (y - yprev) > minPixelDistSquare))) {
                double xUser = transform.xScreenToUser(x);
                double yUser = transform.yScreenToUser(y);
                Drawing.addPointToShape(currentShape, xUser, yUser);

                repaintBothWindows();
                xprev = x;
                yprev = y;
                firstPoint = false;
            };
        };

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
                Drawing.moveShapesRelative(whatToMove,
                        transform.xScreenToUser(x) - transform.xScreenToUser(xprev),
                        transform.yScreenToUser(y) - transform.yScreenToUser(yprev));
                repaintBothWindows();
                xprev = x;
                yprev = y;
            };
        };

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

                    repaintBothWindows();
                }

                xuserprev = xusernew;
                yuserprev = yusernew;

            };
        }; // action move point

        if (actionMoveShape) {

            if (firstPoint) {
                xprev = x;
                yprev = y;
                double xuser = transform.xScreenToUser(x);
                double yuser = transform.yScreenToUser(y);
                currentShape = Drawing.closestShape(xuser, yuser);
                firstPoint = false;
            };

            if (!firstPoint && (currentShape != null)) {
                Drawing.moveShapeRelative(currentShape,
                        transform.xScreenToUser(x) - transform.xScreenToUser(xprev),
                        transform.yScreenToUser(y) - transform.yScreenToUser(yprev));
                xprev = x;
                yprev = y;
                repaintBothWindows();
            };
        }; // action move shape

        if (actionMoveView) {

            double userDeltaX, userDeltaY;
            Transform planeTransform = null;

            if (plane.zPlane) {
                planeTransform = transform;
            } else {  // impossible?
                actionMoveView = false;
                firstPoint = false;
            }

            if (firstPoint) {
                xprev = x;
                yprev = y;
                firstPoint = false;
            }

            if (!firstPoint) {
                userDeltaX = planeTransform.xScreenToUser(x) - planeTransform.xScreenToUser(xprev);
                userDeltaY = planeTransform.yScreenToUser(y) - planeTransform.yScreenToUser(yprev);
                xprev = x;
                yprev = y;

                double xmin = planeTransform.uxmin;
                double xmax = planeTransform.uxmax;
                double ymin = planeTransform.uymin;
                double ymax = planeTransform.uymax;

                planeTransform.setUserSpace(xmin - userDeltaX, xmax - userDeltaX, ymin - userDeltaY, ymax - userDeltaY);

                if (plane.zPlane) {
                    repaintZplane();
                }

            }

        } // Move View

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

                repaintBothWindows();

            };

        }
        ; // Select Area       

    }

    public void mouseClicked(MouseEvent e) {
        ComplexPlane plane = (ComplexPlane) e.getComponent();
        saySomething("Mouse clicked; # of clicks: "
                + e.getClickCount(), e);
    }

    public void mouseReleased(MouseEvent e) {
        ComplexPlane plane = (ComplexPlane) e.getComponent();
        saySomething("Mouse released; # of clicks: " + e.getClickCount(), e);

        if (actionAddShape) {
            actionAddShape = false;
            // add terminal point
            Drawing.addPointToShape(currentShape, transform.xScreenToUser(x),
                    transform.yScreenToUser(y));
            repaintBothWindows();
        }

        if (actionMoveAll || actionMoveSelection || actionMovePoint || actionMoveShape || actionMoveView) {
            firstPoint = true;  // ready to move next shape
        }

        if (actionAddCircle) {
            actionAddCircle = false;
            firstPoint = true;
        }

        if (actionSelectArea) {
            actionSelectArea = false;
            Drawing.commitSelectedArea();
            Drawing.areaCursor(false, 0, 0, 0, 0);
            repaintZplane();
        }

        if (actionUnselectArea) {
            actionUnselectArea = false;
            Drawing.commitUnselectedArea();
            Drawing.areaCursor(false, 0, 0, 0, 0);
            repaintZplane();
        }

    }

    public void mouseEntered(MouseEvent e) {
        ComplexPlane plane = (ComplexPlane) e.getComponent();
        saySomething("Mouse entered", e);
    }

    public void mouseExited(MouseEvent e) {
        ComplexPlane plane = (ComplexPlane) e.getComponent();
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
        actionMoveShape = false;
        actionMoveSelection = false;
        actionMoveAll = false;
        actionDeletePoint = false;
        actionDeleteShape = false;
        actionSelect = false;
        actionUnselect = false;

    }

    public void mousePressed(MouseEvent e) {
        ComplexPlane plane = (ComplexPlane) e.getComponent();
        x = e.getX();
        y = e.getY();
        saySomething("Mouse pressed (" + x + "," + y + ") (# of clicks: " + e.getClickCount() + ")", e);

        if (actionDeletePoint) {

            double xuser = transform.xScreenToUser(x);
            double yuser = transform.yScreenToUser(y);
            cps = Drawing.closestPointShape(xuser, yuser);
            Drawing.deleteShape(cps);
            repaintBothWindows();
        }

        if (actionDeleteShape) {

            double xuser = transform.xScreenToUser(x);
            double yuser = transform.yScreenToUser(y);
            currentShape = Drawing.closestShape(xuser, yuser);
            Drawing.deleteShape(currentShape);
            repaintBothWindows();
        }

        if (actionZoomIn) {
            // the clicked location becomes the new Center
            int zoomFactor = 2;
            double userx, usery;

            if (plane.zPlane) {
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

                repaintZplane();
            };

            firstPoint = false;  // from now on, exiting the window = terminate zoomIn

        }; // Move View

        if (actionSelect) {

            // actionSelect stays active until the cursor leaves the z plane
            int nrs;
            double userx, usery;
            userx = transform.xScreenToUser(e.getX());
            usery = transform.yScreenToUser(e.getY());
            nrs = Drawing.selectShapes(userx, usery, transform.xScreenToUser(2) - transform.xScreenToUser(0));

            repaintBothWindows();

        }
        ; // Select

        if (actionUnselect) {

            // actionUnselect stays active until the cursor leaves the z plane
            int nrs;
            double userx, usery;
            userx = transform.xScreenToUser(e.getX());
            usery = transform.yScreenToUser(e.getY());
            nrs = Drawing.unselectShapes(userx, usery, transform.xScreenToUser(2) - transform.xScreenToUser(0));

            repaintBothWindows();

        }
        ; // Unselect        

        if (actionAddLine) {
            if (firstPoint) {
                //              currentShapeDrawing.addPointToShape(zPlaneTransform.xScreenToUser(x),
                //                      zPlaneTransform.yScreenToUser(y));
                System.out.println(" add line first point " + transform.xScreenToUser(x) + " "
                        + transform.yScreenToUser(y));
                xprev = x;
                yprev = y;
                firstPoint = false;
            } else {
                int pixelDist, nsegments;
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
//                pixelDist = (x - xprev) * (x - xprev) + (y - yprev) * (y - yprev);
//                nsegments = Math.round((int) Math.sqrt((double) pixelDist) / minPixelDist);
                nsegments = 10;
                // create nsegments intermediate points
                for (int i = 0; i <= nsegments; i++) {
                    System.out.println(" segment point x= " + (xbegin + ((double) i / (double) nsegments) * (xend - xbegin))
                            + " y= " + (ybegin + ((double) i / (double) nsegments) * (yend - ybegin)));
                    Drawing.addPointToShape(currentShape, xbegin + ((double) i / (double) nsegments) * (xend - xbegin),
                            ybegin + ((double) i / (double) nsegments) * (yend - ybegin));
                };
                xprev = x;
                yprev = y;
                repaintBothWindows();
            };

            if (e.getClickCount() > 1) {
                actionAddLine = false;
            };
        }
        ; // Add Line

        if (actionAddPoint) {

            xprev = x;
            yprev = y;
            double xuser = transform.xScreenToUser(x);
            double yuser = transform.yScreenToUser(y);
            cps = Drawing.addPointShape();
            if (Drawing.snapToGrid) {
                xuser = Math.round(xuser);
                yuser = Math.round(yuser);
            };
            Drawing.addPointToShape(cps, xuser, yuser);

            repaintBothWindows();
            firstPoint = true;
            actionMovePoint = true;

        }
        ;  // Add Point

    }

    void saySomething(String eventDescription, MouseEvent e) {
//        System.out.println(eventDescription + " detected on " + e.getComponent().getClass().getName());
    }

    public void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();

        if (source == dotButton) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Drawing.dotsVisible = true;
            };
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                Drawing.dotsVisible = false;
            };
            repaintBothWindows();
        };

        if (source == lineButton) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Drawing.linesVisible = true;
            };
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                Drawing.linesVisible = false;
            };
            repaintBothWindows();
        };

        if (source == scaleButton) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Drawing.scaleVisible = true;
            };
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                Drawing.scaleVisible = false;
            };
            repaintBothWindows();
        };

        if (source == gridButton) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Drawing.snapToGrid = true;
            };
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                Drawing.snapToGrid = false;
            };
            repaintBothWindows();
        };

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
        };

    }

    public void actionPerformed(ActionEvent ae) {

        lastButtonClicked = ae.getActionCommand();
        //       System.out.println(lastButtonClicked + " pressed!");
        this.executeAction(lastButtonClicked);

    }

    public void executeAction(String buttonClicked) {

        firstPoint = true;

        if (buttonClicked == "Add Shape") {
            actionAddShape = true;
            firstPoint = true;
            currentShape = Drawing.addShape();
        };

        if (buttonClicked == "Add Line") {
            actionAddLine = true;
            firstPoint = true;
            currentShape = Drawing.addShape();
        };

        if (buttonClicked == "Add Circle") {
            actionAddCircle = true;
            firstPoint = true;
            currentShape = Drawing.addShape();
        };

        if (buttonClicked == "Add Point") {
            actionAddPoint = true;
        };

        if (buttonClicked == "Move Point") {
            actionMovePoint = true;
            firstPoint = true;
        };

        if (buttonClicked == "Move Shape") {
            actionMoveShape = true;
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

        if (buttonClicked == "Delete Point") {
            actionDeletePoint = true;
            firstPoint = true;
        };

        if (buttonClicked == "Delete Shape") {
            actionDeleteShape = true;
            firstPoint = true;
        };

        if (buttonClicked == "Delete Selection") {
            Drawing.clearSelection();
            repaintBothWindows();
        };

        if (buttonClicked == "Delete All") {
            Drawing.clear();
            repaintBothWindows();
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
            repaintBothWindows();
        };

        if (buttonClicked == "Unselect All") {
            Drawing.unselectAll();
            repaintBothWindows();
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
            repaintZplane();

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
            repaintZplane();

        };

        if (buttonClicked == "Add Grid") {

            double xmin = transform.uxmin;
            double xmax = transform.uxmax;
            double ymin = transform.uymin;
            double ymax = transform.uymax;

            GridDialog gd = new GridDialog();
            gd.popUp(zFrame, xmin, xmax, ymin, ymax, (xmax - xmin) / 20);

            if (gd.validValues) {
                xmin = gd.xmin;
                xmax = gd.xmax;
                ymin = gd.ymin;
                ymax = gd.ymax;
                double xgrid = gd.delta;
                double ygrid = gd.delta;

                for (double x = xmin; x <= xmax; x = x + xgrid) {
                    Shape s = Drawing.addShape();
                    for (double y = ymin; y <= ymax; y = y + ygrid) {
                        Drawing.addPointToShape(s, x, y);
                    };
                };

                for (double y = ymin; y <= ymax; y = y + ygrid) {
                    Shape s = Drawing.addShape();
                    for (double x = xmin; x <= xmax; x = x + xgrid) {
                        Drawing.addPointToShape(s, x, y);
                    };
                };

                repaintBothWindows();
            }

        }; // Add Grid

        if (buttonClicked == "Add Polar Grid") {
            double xmin = transform.uxmin;
            double xmax = transform.uxmax;
            double ymin = transform.uymin;
            double ymax = transform.uymax;

            double radius = Math.min((xmax - xmin), (ymax - ymin)) / 2;
            double xcenter = (xmax + xmin) / 2;
            double ycenter = (ymax + ymin) / 2;
            double gridAngle = Math.PI / 20;
            double gridRadius = radius / 50;

            for (double angle = 0; angle <= 2 * Math.PI; angle = angle + gridAngle) {
                Shape s = Drawing.addShape();
                for (double r = gridRadius; r <= radius; r = r + gridRadius) {
                    Drawing.addPointToShape(s, xcenter + r * Math.sin(angle), ycenter + r * Math.cos(angle));
                };
            };

            for (double r = gridRadius; r <= radius; r = r + gridRadius) {
                Shape s = Drawing.addShape();
                for (double angle = 0; angle <= 2 * Math.PI; angle = angle + gridAngle) {
                    Drawing.addPointToShape(s, xcenter + r * Math.sin(angle), ycenter + r * Math.cos(angle));
                };
            };

            repaintBothWindows();
        }; // Add Polar Grid

    }

    private void AddMenuItem(JMenu menu, String name, String action) {
        JMenuItem menuItem;
        menuItem = new JMenuItem(name);
        menuItem.addActionListener(this);
        menuItem.setActionCommand(action);
        menu.add(menuItem);
    }

    private void create() {   // Create and set up the windows.

        zPlane.t = transform;
        zPlane.zPlane = true;

        transform.setUserSpace(-10, 10, -10, 10);

//create zPlane and wPlane frames
//get device screen coordinates to position both frames
        Rectangle bounds = gc.getBounds(); // device coordinates of the screen (0,0) = upper left (w,h) = lo right

        zFrame.setLocation(bounds.width / 2, 0);  //bounds.height / 3);
        zFrame.setSize(bounds.width / 2 - 22, bounds.width / 2);

        zFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        zPlane.setBackground(Color.white);

        zPlane.addMouseListener(this);
        zPlane.addMouseMotionListener(this);
        zPlane.addKeyListener(this);

        zFrame.add(zPlane);
        zFrame.setTitle("z plane");
        zFrame.setVisible(true);
        zFrame.add(zPlane);

        zPlane.createBufferStrategy(2);
        zPlane.setIgnoreRepaint(true);

        zPlane.blitPaint();

// populate settings frame
        Container pane = dFrame.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        dotButton.setSelected(false);
        dotButton.addItemListener(this);

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
        editorPane.add(dotButton);
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

// menu bar for zFrame
        JMenuBar zMenuBar = new JMenuBar();

        JMenu menuAdd = new JMenu("Add");
        zMenuBar.add(menuAdd);
        AddMenuItem(menuAdd, "Line ...", "Add Line");
        AddMenuItem(menuAdd, "Shape ...", "Add Shape");
        AddMenuItem(menuAdd, "Circle ...", "Add Circle");
        AddMenuItem(menuAdd, "Point ...", "Add Point");
        AddMenuItem(menuAdd, "Grid", "Add Grid");
        AddMenuItem(menuAdd, "Polar Grid", "Add Polar Grid");

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
        AddMenuItem(menuMove, "Shape", "Move Shape");
        AddMenuItem(menuMove, "Selection", "Move Selection");
        AddMenuItem(menuMove, "All", "Move All");

        JMenu menuDelete = new JMenu("Delete");
        zMenuBar.add(menuDelete);
        AddMenuItem(menuDelete, "Point", "Delete Point");
        AddMenuItem(menuDelete, "Shape", "Delete Shape");
        AddMenuItem(menuDelete, "Selection", "Delete Selection");
        AddMenuItem(menuDelete, "All", "Delete All");

        JMenu menuZplane = new JMenu("View");
        zMenuBar.add(menuZplane);
        AddMenuItem(menuZplane, "Zoom Out", "Zplane Zoom Out");
        AddMenuItem(menuZplane, "Zoom In ...", "Zoom In");
        AddMenuItem(menuZplane, "Move ...", "Move View");
        AddMenuItem(menuZplane, "Fit", "Zplane Fit");

        zFrame.setJMenuBar(zMenuBar);

    }  // create

    public void display() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                create();
            }
        });

    }  // display

}

class MainVisualComplex {

    public static void main(String[] args) {

        MovingParticles vc = new MovingParticles();

        vc.display();

    } // main

}
