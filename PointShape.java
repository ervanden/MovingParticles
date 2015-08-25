import java.util.*;



class PointShape extends Shape {  // shape that represents a complex point

    public PointShape(String itsname){
points = new ArrayList<Point>();
points.clear();
label=itsname;
isComplexPoint=true;
}

public double getX() { return this.points.get(0).getZX(); }
public double getY() { return this.points.get(0).getZY(); }
public Point getPoint() { return this.points.get(0); }
public String getName() { return label; }

}