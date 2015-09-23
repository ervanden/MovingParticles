import java.awt.Color;
import java.util.*;

class Curve {

    Color color;
    
    java.util.List<Point> points;

    public boolean isPoint = false;
    public boolean isSelected = false;
    public boolean isPreSelected = false;
    public boolean isPreUnselected = false;

    public Curve() {
        points = new ArrayList<>();
    }


    public Point lastPoint() {
        return points.get(points.size() - 1);
    }

}

