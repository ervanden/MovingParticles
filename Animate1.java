

public class Animate1 implements Runnable {

    String animation;

    public Animate1(String animation) {
        this.animation = animation;
    }

    public void run() {
        double alfa = Math.PI / 200;
        while (true) {

            MovingParticles.Drawing.rotate(alfa);

            try {
                Thread.sleep(25);
            } catch (InterruptedException ie) {

            }
            
            MovingParticles.zPlane.blitPaint();

            while (MovingParticles.suspendAnimation) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException ie) {
                    }
            }

        }
    }
}
