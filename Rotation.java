
import java.util.ArrayList;
import javax.swing.JPanel;

class Rotation implements Animation {

    double omega = 2 * Math.PI;    // 1 revolution/sec

    public JPanel getPane(){
        return null;
    }
        
    public ArrayList<Point> getParticles() {
        return null;
    }

    public void trajectory(boolean on) {
    }

    public boolean step(double dt) {
        double alfa;

        alfa = omega * dt;
        double cos = Math.cos(alfa);
        double sin = Math.sin(alfa);

        for (Shape s : MovingParticles.Drawing.getShapes()) {
            for (Point p : s.points) {
                double xnew = p.x * cos - p.y * sin;
                double ynew = p.y * cos + p.x * sin;
                p.x = xnew;
                p.y = ynew;
            }
        }
        return true; // redraw
    }
}
