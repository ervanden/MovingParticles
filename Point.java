
import java.util.ArrayList;


public class Point {

public double x;
public double y;

// gravitate
String particleName;
Shape trajectory;
public double mass;
public double velocity;
public double angle;
public double x_init;
public double y_init;
public double xLastDrawn;
public double yLastDrawn;
public double xnew;
public double ynew;
public double xspeed;
public double yspeed;

// elastic
ArrayList<Point> neighbours;

public Point(double px, double py){
x=px;
y=py;
xspeed=0;
yspeed=0;
mass=1;
neighbours=new ArrayList<>();
}

double getZX(){
    return x;
}
double getZY(){
    return y;
}

void replaceXY(double px, double py){
x=px;
y=py;
}

}
