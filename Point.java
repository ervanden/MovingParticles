
class Point {

public double x;
public double y;

// gravitate
    String particleName;
public double mass;
public double velocity;
public double angle;
public double xnew;
public double ynew;
public double xspeed;
public double yspeed;

public Point(double px, double py){
x=px;
y=py;
xspeed=0;
yspeed=0;
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
