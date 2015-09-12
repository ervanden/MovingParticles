

public class Point {

public double x;
public double y;
public double vx;
public double vy;

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
public double vxnew;
public double vynew;

public Point(double px, double py){
x=px;
y=py;
vx=0;
vy=0;
mass=1;
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
