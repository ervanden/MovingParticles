import java.util.*;

class Shape {

    public String label = "";
    public java.util.List<Point> points;

    public boolean isComplexPoint = false;
    public boolean isSelected = false;
    public boolean isPreSelected = false;
    public boolean isPreUnselected = false;

    public Shape() {
        points = new ArrayList<>();
        points.clear();
    }

    public void clear() {
        points.clear();
    }

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
