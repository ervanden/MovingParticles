

public class Animate1 implements Runnable {

    String animation;

    public Animate1(String animation) {
        this.animation = animation;
    }

    public void run() {

        while (true) {

            if (animation.equals("rotate")) MovingParticles.Drawing.rotate(Math.PI / 200);
            if (animation.equals("gravitate")) MovingParticles.Drawing.gravitate(10.0);

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
