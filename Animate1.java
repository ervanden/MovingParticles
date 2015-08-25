/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class Animate1 implements Runnable {

    String animation;

    public Animate1(String animation) {
        this.animation = animation;
    }

    public void run() {
        double alfa = Math.PI / 200;
        double cos = Math.cos(alfa);
        double sin = Math.sin(alfa);
        while (true) {
            // change drawing coordinates
            for (Shape s : MovingParticles.Drawing.shapes) {
                for (Point p : s.points) {
                    double xnew = p.x * cos - p.y * sin;
                    double ynew = p.y * cos + p.x * sin;
                    p.x = xnew;
                    p.y = ynew;
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException ie) {

            }
            // redraw
            MovingParticles.zPlane.blitPaint();

        }
    }
}
