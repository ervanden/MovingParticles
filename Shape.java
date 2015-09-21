import java.awt.Color;
import java.util.*;

class Shape {

    
    
    public String label = "";
    java.util.List<Point> points;
    Color color;

    public boolean isPoint = false;
    public boolean isSelected = false;
    public boolean isPreSelected = false;
    public boolean isPreUnselected = false;

    public Shape() {
        points = new ArrayList<>();
        points.clear();
        color=Color.BLACK;
    }

    // clear() should only be called from Drawing.clearShape,for synchronization
    public void clear() {
        points.clear();
    }

    // addPoint should only be called from Drawing.addPointToShape,for synchronization
    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    public int nrPoints() {
        return points.size();
    }

    public Point lastPoint() {
        return points.get(points.size() - 1);
    }

} // class Shape
