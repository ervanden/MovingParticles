import java.util.*;




class Point {
public double x;
public double y;
public double xspeed;
public double yspeed;

public Point(double px, double py){
x=px;
y=py;
xspeed=0;
yspeed=1.0;
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
