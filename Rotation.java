
import java.util.ArrayList;
import javax.swing.JPanel;

class Rotation implements Animation {

    double omega = 2 * Math.PI;    // 1 revolution/sec

    public Point updateCenterOfGravity() {
        return null;
    }

    public JPanel getPane() {
        return null;
    }

    public ArrayList<Point> getParticles() {
        return null;
    }

    public void cleanup() {
    }

    public boolean step(double dt, int resolution) {
        double alfa;

        alfa = omega * dt;
        double cos = Math.cos(alfa);
        double sin = Math.sin(alfa);


            for (Point p : MovingParticles.Drawing.getPoints()){
                double xnew = p.x * cos - p.y * sin;
                double ynew = p.y * cos + p.x * sin;
                p.x = xnew;
                p.y = ynew;
            }

        return true; // redraw
    }
}
