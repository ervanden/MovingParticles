
import java.util.ArrayList;
import javax.swing.JPanel;

public interface Animation {

    public ArrayList<Point> getParticles();
    public JPanel getPane();
    public boolean step(double dt);

}
