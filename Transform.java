
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.ArrayList;
import scalemarkers.*;

class Transform {

    public Graphics graphics;
    public double uxmin;
    public double uxmax;
    public double uymin;
    public double uymax;

// real screen coordinates (from setUserSpace)
    public double sxmin_real = 0;
    public double sxmax_real = 0;
    public double symin_real = 0;
    public double symax_real = 0;

// recalculated screen boundaries to preserve user space aspect ratio
    double sxmin = 0;
    double sxmax = 0;
    double symin = 0;
    double symax = 0;

    double xa, xb, ya, yb; // transformation parameters

    public Transform() {
    }

    public void setUserSpace(double x1, double x2, double y1, double y2) {

        uxmin = x1;
        uxmax = x2;
        uymin = y1;
        uymax = y2;

        xa = (sxmin - sxmax) / (uxmin - uxmax);
        ya = (symin - symax) / (uymin - uymax);
        xb = sxmin - uxmin * xa;
        yb = symin - uymin * ya;

    }

    public void setScreenSpace(double xmin, double xmax, double ymin, double ymax) {
        double uratio, sratio;
        double udx, udy, sdx, sdy, sxcenter, sycenter;

        sxmin_real = xmin;
        sxmax_real = xmax;
        symin_real = ymin;
        symax_real = ymax;
        sxmin = xmin;
        sxmax = xmax;
        symin = ymin;
        symax = ymax;

        boolean debug = false;

// in java graphics screen coordinates, sxmin < sxmax but symin > symax (y coordinates are inversed)
// so the screen width = -(symax-symin)
// then the new window boundaries are calculated in 'normal' y coordinates (same code as x coordinates)
// At the end symax and symin are swapped 
        sdx = (sxmax - sxmin);
        sdy = -(symax - symin);  // !!
        udx = (uxmax - uxmin);
        udy = (uymax - uymin);
        sycenter = (symax + symin) / 2;
        sxcenter = (sxmax + sxmin) / 2;

        uratio = udy / udx;
        sratio = sdy / sdx;

        if (debug) {
            System.out.println(" screen y/x ratio = " + sratio + " user y/x ratio = " + uratio);
        };
        if (debug) {
            System.out.println(" Screen from x1= " + sxmin + " x2= " + sxmax + " y1= " + symin + " y2= " + symax);
        };
        if (sratio > uratio) {
            // screen too high : adapt height
            sdy = sdx * uratio;
            // calculate boundaries on screen to map user space to
            symin = sycenter + sdy / 2;  // !!
            symax = sycenter - sdy / 2;  // !!
        };
        if (debug) {
            System.out.println(" Screen to   x1= " + sxmin + " x2= " + sxmax + " y1= " + symin + " y2= " + symax);
        };

        sdx = (sxmax - sxmin);
        sdy = -(symax - symin);  // !!
        udx = (uxmax - uxmin);
        udy = (uymax - uymin);
        sycenter = (symax + symin) / 2;
        sxcenter = (sxmax + sxmin) / 2;

        uratio = udy / udx;
        sratio = sdy / sdx;

        if (debug) {
            System.out.println(" screen y/x ratio = " + sratio + " user y/x ratio = " + uratio);
        };
        if (debug) {
            System.out.println(" Screen from x1= " + sxmin + " x2= " + sxmax + " y1= " + symin + " y2= " + symax);
        };
        if (sratio < uratio) {
            // screen too wide : adapt width
            sdx = sdy / uratio;
            sxmin = sxcenter - sdx / 2;
            sxmax = sxcenter + sdx / 2;
        };
        if (debug) {
            System.out.println(" Screen to   x1= " + sxmin + " x2= " + sxmax + " y1= " + symin + " y2= " + symax);
        };

        xa = (sxmin - sxmax) / (uxmin - uxmax);
        ya = (symin - symax) / (uymin - uymax);
        xb = sxmin - uxmin * xa;
        yb = symin - uymin * ya;

        if (debug) {
            System.out.println(" Transform" + " xa= " + xa + " xb= " + xb + " ya= " + ya + " yb= " + yb);
        };

    }

    public void line(double x1, double y1, double x2, double y2) {
        graphics.drawLine(
                xUserToScreen(x1),
                yUserToScreen(y1),
                xUserToScreen(x2),
                yUserToScreen(y2)
        );
    }

    public void circle(double x, double y, double r, boolean filled) {
        int xcenter = xUserToScreen(x);
        int ycenter = yUserToScreen(y);
        int radius = xUserToScreen(r) - xUserToScreen(0);
        if (filled) {
            graphics.fillOval(xcenter - radius, ycenter - radius, 2 * radius, 2 * radius);
        } else {
            graphics.drawOval(xcenter - radius, ycenter - radius, 2 * radius, 2 * radius);
        }
    }

    public void dot(double x, double y) {
        int xcenter, ycenter;
        xcenter = xUserToScreen(x);
        ycenter = yUserToScreen(y);
        graphics.drawOval(xcenter - 3, ycenter - 3, 6, 6);
    }

    public void label(String s, double x, double y) {
        int xcenter, ycenter;
        xcenter = xUserToScreen(x);
        ycenter = yUserToScreen(y);
        graphics.drawString(s, xcenter , ycenter );
    }

    public void complexPoint(String s, double x, double y) {
        int xcenter, ycenter;
        xcenter = xUserToScreen(x);
        ycenter = yUserToScreen(y);
        graphics.fillOval(xcenter - 4, ycenter - 4, 8, 8);
        graphics.drawString(s, xcenter + 4, ycenter - 4);
        //                  System.out.printf("point %s at %d,%d\n",s,xcenter + 4, ycenter - 4);
    }

    public void fix(double x, double y) {
        int xcenter, ycenter;
        xcenter = xUserToScreen(x);
        ycenter = yUserToScreen(y);
        int w = 4;
        graphics.drawLine(xcenter - w, ycenter - w, xcenter - w, ycenter + w);
        graphics.drawLine(xcenter - w, ycenter + w, xcenter + w, ycenter + w);
        graphics.drawLine(xcenter + w, ycenter + w, xcenter + w, ycenter - w);
        graphics.drawLine(xcenter + w, ycenter - w, xcenter - w, ycenter - w);
    }

    public void strings(ArrayList<String> l) {
        int xo, yo;
        xo = 10;
        yo = 20;
        int position = 0;
        for (String s : l) {
            if (s == null) {
                s = "null";
            }
            graphics.drawString(s, xo, yo + position * 15);
            position++;
        }
    }

    public void axes() {

        graphics.setColor(Color.black);
        BasicStroke stroke0 = new BasicStroke();     // default dunne lijn
        BasicStroke stroke3 = new BasicStroke(3);    // dikkere lijn voor x en y as
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setStroke(stroke3);

        graphics.drawLine((int) sxmin_real,
                yUserToScreen(0),
                (int) sxmax_real,
                yUserToScreen(0)
        );

        graphics.drawLine(xUserToScreen(0),
                (int) symin_real,
                xUserToScreen(0),
                (int) symax_real);

        g2.setStroke(stroke0);

        if (MovingParticles.Drawing.scaleVisible) {
            double cxmin = xScreenToUser((int) sxmin_real);
            double cxmax = xScreenToUser((int) sxmax_real);
            double cymin = yScreenToUser((int) symin_real);
            double cymax = yScreenToUser((int) symax_real);

            java.util.List<String> scaleList;

            scaleList = ScaleMarkers.createScale(cxmin, cxmax);

            for (String marker : scaleList) {
                double value = Double.parseDouble(marker);
                graphics.drawLine(xUserToScreen(value),
                        yUserToScreen(0) - 5,
                        xUserToScreen(value),
                        yUserToScreen(0) + 5);
                graphics.drawString(marker, xUserToScreen(value) + 4, yUserToScreen(0) - 4);
            };

            scaleList = ScaleMarkers.createScale(cymin, cymax);

            for (String marker : scaleList) {
                double value = Double.parseDouble(marker);
                graphics.drawLine(xUserToScreen(0) - 5,
                        yUserToScreen(value),
                        xUserToScreen(0) + 5,
                        yUserToScreen(value));
                graphics.drawString(marker, xUserToScreen(0) + 4, yUserToScreen(value) - 4);
            };
        };

    }

    public void gridLines() {

        graphics.setColor(Color.gray);

        for (int i = (int) Math.round(yScreenToUser((int) symin_real)) - 1; i < Math.round(yScreenToUser((int) symax_real) + 1); i++) {
            graphics.drawLine((int) sxmin_real,
                    yUserToScreen(i),
                    (int) sxmax_real,
                    yUserToScreen(i)
            );
        };

        for (int i = (int) Math.round(xScreenToUser((int) sxmin_real)) - 1; i < Math.round(xScreenToUser((int) sxmax_real) + 1); i++) {
            graphics.drawLine(xUserToScreen(i),
                    (int) symin_real,
                    xUserToScreen(i),
                    (int) symax_real
            );
        };

    }

    public int xUserToScreen(double xuser) {
        return (int) Math.round(xb + xa * xuser);
    }

    public int yUserToScreen(double yuser) {
        return (int) Math.round(yb + ya * yuser);
    }

    public double xScreenToUser(int xscreen) {
        return (xscreen - xb) / xa;
    }

    public double yScreenToUser(int yscreen) {
        return (yscreen - yb) / ya;
    }

}
