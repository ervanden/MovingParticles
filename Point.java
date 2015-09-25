
import java.awt.Color;

public class Point {

    static int count = 0;

    public boolean isSelected = false;
    public boolean isPreSelected = false;
    public boolean isPreUnselected = false;

    public double x;
    public double y;
    public double vx;
    public double vy;
    public boolean fixed;
    public double radius;
    public boolean filled;

    String particleName;
    Color color = Color.BLACK;
Curve tangent=null;
Curve normal= null;
Curve w = null;
    Curve trajectory;
    public double mass;
    public double velocity;
    public double angle;
    public double x_init;
    public double y_init;
    public double xLastDrawn;
    public double yLastDrawn;
    public double xnew;
    public double ynew;
    public double vxnew;
    public double vynew;

    public Point(double px, double py) {
        x = px;
        y = py;
        vx = 0;
        vy = 0;
        fixed = false;
        mass = 1;
        radius = 0.1;
        filled = false;
        particleName = "P" + count++;
    }

    double getZX() {
        return x;
    }

    double getZY() {
        return y;
    }

    void replaceXY(double px, double py) {
        x = px;
        y = py;
    }

}
